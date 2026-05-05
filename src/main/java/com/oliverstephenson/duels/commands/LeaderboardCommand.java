package com.oliverstephenson.duels.commands;

import com.oliverstephenson.duels.stats.PlayerStats;
import com.oliverstephenson.duels.stats.StatsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LeaderboardCommand implements CommandExecutor {

    private final StatsManager statsManager;

    public LeaderboardCommand(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        List<PlayerStats> board = statsManager.getLeaderboard(10);
        sender.sendMessage(Component.text("=== Duels Leaderboard ===",
                NamedTextColor.GOLD));

        if (board.isEmpty()) {
            sender.sendMessage(Component.text("No stats yet — play some duels!",
                    NamedTextColor.WHITE));
            return true;
        }

        for (int i = 0; i < board.size(); i++) {
            PlayerStats ps = board.get(i);
            NamedTextColor color = i == 0 ? NamedTextColor.GOLD
                                 : i == 1 ? NamedTextColor.GRAY
                                 : NamedTextColor.WHITE;
            sender.sendMessage(Component.text(
                    (i + 1) + ". " + ps.name() + " — "
                    + ps.wins() + "W / " + ps.losses() + "L"
                    + " (" + ps.formattedWinRate() + ")", color));
        }
        return true;
    }
}