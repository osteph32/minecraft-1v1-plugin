package com.oliverstephenson.duels.commands;

import com.oliverstephenson.duels.duel.DuelManager;
import com.oliverstephenson.duels.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QueueCommand implements CommandExecutor {

    private final DuelManager duelManager;

    public QueueCommand(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can queue.");
            return true;
        }
        duelManager.toggleQueue(player);
        return true;
    }
}