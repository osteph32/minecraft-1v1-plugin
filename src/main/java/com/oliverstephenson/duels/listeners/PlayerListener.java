package com.oliverstephenson.duels.listeners;

import com.oliverstephenson.duels.duel.DuelManager;
import com.oliverstephenson.duels.stats.StatsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final DuelManager  duelManager;
    private final StatsManager statsManager;

    public PlayerListener(DuelManager duelManager, StatsManager statsManager) {
        this.duelManager  = duelManager;
        this.statsManager = statsManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (duelManager.isInDuel(player.getUniqueId())) {
            duelManager.endDuel(player, true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        statsManager.cacheName(player.getUniqueId(), player.getName());
    }
}