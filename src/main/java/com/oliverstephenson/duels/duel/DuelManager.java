package com.oliverstephenson.duels.duel;

import com.oliverstephenson.duels.DuelsPlugin;
import com.oliverstephenson.duels.arena.Arena;
import com.oliverstephenson.duels.arena.ArenaManager;
import com.oliverstephenson.duels.kit.Kit;
import com.oliverstephenson.duels.stats.StatsManager;
import com.oliverstephenson.duels.util.LocationUtil;
import com.oliverstephenson.duels.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DuelManager {

    private final DuelsPlugin  plugin;
    private final ArenaManager arenaManager;
    private final StatsManager statsManager;

    private final Map<UUID, DuelRequest> pendingRequests = new HashMap<>();
    private final Map<UUID, Duel>        activeDuels     = new HashMap<>();
    private final Queue<UUID>            matchmakeQueue  = new ArrayDeque<>();

    private final int requestTimeout;

    public DuelManager(DuelsPlugin plugin, ArenaManager arenaManager,
                       StatsManager statsManager) {
        this.plugin        = plugin;
        this.arenaManager  = arenaManager;
        this.statsManager  = statsManager;
        this.requestTimeout = plugin.getConfig()
                .getInt("duel-request-timeout", 30);
    }

    public void sendRequest(Player challenger, Player challenged) {
        if (isInDuel(challenger.getUniqueId())) {
            challenger.sendMessage(Msg.err("You are already in a duel."));
            return;
        }
        if (isInDuel(challenged.getUniqueId())) {
            challenger.sendMessage(Msg.err(challenged.getName()
                    + " is already in a duel."));
            return;
        }
        if (pendingRequests.containsKey(challenged.getUniqueId())) {
            challenger.sendMessage(Msg.err(challenged.getName()
                    + " already has a pending request."));
            return;
        }

        DuelRequest request = new DuelRequest(
                challenger.getUniqueId(), challenged.getUniqueId(), requestTimeout);

        int taskId = new BukkitRunnable() {
            @Override public void run() {
                if (pendingRequests.remove(challenged.getUniqueId()) != null) {
                    challenger.sendMessage(Msg.warn("Your duel request to "
                            + challenged.getName() + " expired."));
                    challenged.sendMessage(Msg.warn("Duel request from "
                            + challenger.getName() + " expired."));
                }
            }
        }.runTaskLater(plugin, requestTimeout * 20L).getTaskId();

        request.setExpiryTaskId(taskId);
        pendingRequests.put(challenged.getUniqueId(), request);

        challenger.sendMessage(Msg.info("Request sent to " + challenged.getName()
                + ". Expires in " + requestTimeout + "s."));
        challenged.sendMessage(Msg.info(challenger.getName()
                + " challenged you! Type /duel accept or /duel deny."));
    }

    public void acceptRequest(Player challenged) {
        DuelRequest request = pendingRequests.remove(challenged.getUniqueId());
        if (request == null) {
            challenged.sendMessage(Msg.err("You have no pending duel request."));
            return;
        }
        Bukkit.getScheduler().cancelTask(request.getExpiryTaskId());

        Player challenger = Bukkit.getPlayer(request.getChallengerUUID());
        if (challenger == null || !challenger.isOnline()) {
            challenged.sendMessage(Msg.err("That player is no longer online."));
            return;
        }
        startDuel(challenger, challenged, Kit.SWORD);
    }

    public void denyRequest(Player challenged) {
        DuelRequest request = pendingRequests.remove(challenged.getUniqueId());
        if (request == null) {
            challenged.sendMessage(Msg.err("You have no pending duel request."));
            return;
        }
        Bukkit.getScheduler().cancelTask(request.getExpiryTaskId());
        Player challenger = Bukkit.getPlayer(request.getChallengerUUID());
        challenged.sendMessage(Msg.info("Duel request denied."));
        if (challenger != null) {
            challenger.sendMessage(Msg.warn(challenged.getName()
                    + " denied your duel request."));
        }
    }

    public void startDuel(Player challenger, Player challenged, Kit kit) {
        Optional<Arena> arenaOpt = arenaManager.allocate();
        if (arenaOpt.isEmpty()) {
            challenger.sendMessage(Msg.err("No arenas available right now."));
            challenged.sendMessage(Msg.err("No arenas available right now."));
            return;
        }

        Arena arena = arenaOpt.get();
        Duel  duel  = new Duel(challenger, challenged, arena, kit);

        activeDuels.put(challenger.getUniqueId(), duel);
        activeDuels.put(challenged.getUniqueId(), duel);

        challenger.teleport(arena.getSpawn1());
        challenged.teleport(arena.getSpawn2());

        kit.applyTo(challenger);
        kit.applyTo(challenged);

        broadcastToBoth(duel, Msg.info("⚔ Duel starting in arena: "
                + arena.getName() + " | Kit: " + kit.getDisplayName()));

        runCountdown(duel);
    }

    private void runCountdown(Duel duel) {
        int taskId = new BukkitRunnable() {
            int count = 3;
            @Override public void run() {
                if (duel.getState() == DuelState.FINISHED) { cancel(); return; }
                if (count > 0) {
                    broadcastToBoth(duel,
                            Component.text(count + "...", NamedTextColor.YELLOW));
                    count--;
                } else {
                    broadcastToBoth(duel,
                            Component.text("FIGHT!", NamedTextColor.GREEN));
                    duel.setState(DuelState.ACTIVE);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
        duel.setCountdownTaskId(taskId);
    }

    public void endDuel(Player loser, boolean disconnected) {
        Duel duel = activeDuels.get(loser.getUniqueId());
        if (duel == null || duel.getState() == DuelState.FINISHED) return;

        duel.setState(DuelState.FINISHED);
        Bukkit.getScheduler().cancelTask(duel.getCountdownTaskId());

        UUID   winnerUUID = duel.getOpponentUUID(loser.getUniqueId());
        Player winner     = Bukkit.getPlayer(winnerUUID);

        activeDuels.remove(loser.getUniqueId());
        activeDuels.remove(winnerUUID);
        arenaManager.release(duel.getArena());

        statsManager.recordWin(winnerUUID);
        statsManager.recordLoss(loser.getUniqueId());

        String loserName = disconnected
                ? loser.getName() + " (disconnected)" : loser.getName();
        String winnerName = winner != null ? winner.getName() : "Unknown";

        Component result = Component.text(
                "🏆 " + winnerName + " defeated " + loserName + "!",
                NamedTextColor.GOLD);
        if (winner != null) winner.sendMessage(result);
        loser.sendMessage(result);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (winner != null && winner.isOnline()) {
                duel.restoreInventory(winner);
                winner.teleport(LocationUtil.getLobby(plugin.getConfig()));
            }
            if (loser.isOnline()) {
                duel.restoreInventory(loser);
                loser.teleport(LocationUtil.getLobby(plugin.getConfig()));
            }
        }, 40L);
    }

    public void toggleQueue(Player player) {
        UUID uuid = player.getUniqueId();
        if (isInDuel(uuid)) {
            player.sendMessage(Msg.err("You are already in a duel."));
            return;
        }
        if (matchmakeQueue.contains(uuid)) {
            matchmakeQueue.remove(uuid);
            player.sendMessage(Msg.info("You left the queue."));
            return;
        }
        matchmakeQueue.add(uuid);
        player.sendMessage(Msg.info("Joined the queue! Players waiting: "
                + matchmakeQueue.size()));

        if (matchmakeQueue.size() >= 2) {
            UUID   p1uuid = matchmakeQueue.poll();
            UUID   p2uuid = matchmakeQueue.poll();
            Player p1     = Bukkit.getPlayer(p1uuid);
            Player p2     = Bukkit.getPlayer(p2uuid);
            if (p1 != null && p2 != null) {
                startDuel(p1, p2, Kit.SWORD);
            } else {
                if (p1 != null) matchmakeQueue.add(p1uuid);
                if (p2 != null) matchmakeQueue.add(p2uuid);
            }
        }
    }

    public void shutdownAll() {
        new HashSet<>(activeDuels.keySet()).forEach(uuid -> {
            Player p    = Bukkit.getPlayer(uuid);
            Duel   duel = activeDuels.get(uuid);
            if (p != null && duel != null
                    && duel.getState() != DuelState.FINISHED) {
                duel.setState(DuelState.FINISHED);
                duel.restoreInventory(p);
                p.teleport(LocationUtil.getLobby(plugin.getConfig()));
                p.sendMessage(Msg.warn("Your duel was cancelled — server restarting."));
            }
        });
        activeDuels.clear();
    }

    public boolean isInDuel(UUID uuid)    { return activeDuels.containsKey(uuid); }
    public Duel    getDuel(UUID uuid)     { return activeDuels.get(uuid);         }
    public boolean isCountdown(UUID uuid) {
        Duel d = activeDuels.get(uuid);
        return d != null && d.getState() == DuelState.COUNTDOWN;
    }

    private void broadcastToBoth(Duel duel, Component message) {
        Player c = Bukkit.getPlayer(duel.getChallengerUUID());
        Player d = Bukkit.getPlayer(duel.getChallengedUUID());
        if (c != null) c.sendMessage(message);
        if (d != null) d.sendMessage(message);
    }
}