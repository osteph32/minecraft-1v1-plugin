package com.oliverstephenson.duels.commands;

import com.oliverstephenson.duels.DuelsPlugin;
import com.oliverstephenson.duels.arena.ArenaManager;
import com.oliverstephenson.duels.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ArenaWizardCommand implements CommandExecutor {

    private final DuelsPlugin  plugin;
    private final ArenaManager arenaManager;

    public ArenaWizardCommand(DuelsPlugin plugin, ArenaManager arenaManager) {
        this.plugin       = plugin;
        this.arenaManager = arenaManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }
        if (!player.hasPermission("osduels.admin")) {
            player.sendMessage(Msg.err("You don't have permission."));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Msg.info("Usage: /arenawizard set <name> spawn1|spawn2"));
            player.sendMessage(Msg.info("       /arenawizard setlobby"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setlobby" -> {
                arenaManager.setLobby(player.getLocation());
                player.sendMessage(Msg.success("Lobby set to your location."));
            }
            case "set" -> {
                if (args.length < 3) {
                    player.sendMessage(Msg.err(
                            "Usage: /arenawizard set <name> spawn1|spawn2"));
                    return true;
                }
                String spawnKey = args[2].toLowerCase();
                if (!spawnKey.equals("spawn1") && !spawnKey.equals("spawn2")) {
                    player.sendMessage(Msg.err("Must be spawn1 or spawn2."));
                    return true;
                }
                int spawnIndex = spawnKey.equals("spawn1") ? 1 : 2;
                arenaManager.setSpawn(args[1].toLowerCase(), spawnIndex,
                        player.getLocation());
                player.sendMessage(Msg.success("Set " + spawnKey
                        + " for arena '" + args[1] + "'."));
            }
            default -> player.sendMessage(Msg.err("Unknown sub-command."));
        }
        return true;
    }
}