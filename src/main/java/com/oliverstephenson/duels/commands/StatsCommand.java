package com.oliverstephenson.duels.commands;

import com.oliverstephenson.duels.stats.PlayerStats;
import com.oliverstephenson.duels.stats.StatsManager;
import com.oliverstephenson.duels.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class StatsCommand implements CommandExecutor {

    private final StatsManager statsManager;

    public StatsCommand(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        UUID targetUUID;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Specify a player name from console.");
                return true;
            }
            targetUUID = player.getUniqueId();
        } else {
            Player online = Bukkit.getPlayerExact(args[0]);
            if (online == null) {
                sender.sendMessage(Msg.warn("'" + args[0] + "' must be online."));
                return true;
            }
            targetUUID = online.getUniqueId();
        }

        PlayerStats stats = statsManager.getStats(targetUUID);
        sender.sendMessage(Component.text("--- " + stats.name() + "'s Stats ---",
                NamedTextColor.GOLD));
        sender.sendMessage(Msg.info("Wins:     " + stats.wins()));
        sender.sendMessage(Msg.info("Losses:   " + stats.losses()));
        sender.sendMessage(Msg.info("Win Rate: " + stats.formattedWinRate()));
        return true;
    }
}