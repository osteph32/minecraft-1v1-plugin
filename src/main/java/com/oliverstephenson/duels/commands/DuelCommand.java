package com.oliverstephenson.duels.commands;

import com.oliverstephenson.duels.duel.DuelManager;
import com.oliverstephenson.duels.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DuelCommand implements CommandExecutor {

    private final DuelManager duelManager;

    public DuelCommand(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Msg.info("Usage: /duel <player> | accept | deny"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "accept" -> duelManager.acceptRequest(player);
            case "deny"   -> duelManager.denyRequest(player);
            default -> {
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    player.sendMessage(Msg.err("Player '" + args[0] + "' is not online."));
                    return true;
                }
                if (target.equals(player)) {
                    player.sendMessage(Msg.err("You can't duel yourself."));
                    return true;
                }
                duelManager.sendRequest(player, target);
            }
        }
        return true;
    }
}