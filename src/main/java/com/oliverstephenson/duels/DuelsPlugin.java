package com.oliverstephenson.duels;

import com.oliverstephenson.duels.arena.ArenaManager;
import com.oliverstephenson.duels.commands.*;
import com.oliverstephenson.duels.duel.DuelManager;
import com.oliverstephenson.duels.listeners.CombatListener;
import com.oliverstephenson.duels.listeners.PlayerListener;
import com.oliverstephenson.duels.stats.StatsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DuelsPlugin extends JavaPlugin {

    private static DuelsPlugin instance;
    private ArenaManager arenaManager;
    private DuelManager  duelManager;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        arenaManager = new ArenaManager(this);
        statsManager = new StatsManager(this);
        duelManager  = new DuelManager(this, arenaManager, statsManager);

        getCommand("duel").setExecutor(new DuelCommand(duelManager));
        getCommand("stats").setExecutor(new StatsCommand(statsManager));
        getCommand("queue").setExecutor(new QueueCommand(duelManager));
        getCommand("leaderboard").setExecutor(new LeaderboardCommand(statsManager));
        getCommand("arenawizard").setExecutor(
                new ArenaWizardCommand(this, arenaManager));

        getServer().getPluginManager().registerEvents(
                new CombatListener(duelManager), this);
        getServer().getPluginManager().registerEvents(
                new PlayerListener(duelManager, statsManager), this);

        getLogger().info("OSDuels enabled — "
                + arenaManager.getArenaCount() + " arena(s) loaded.");
    }

    @Override
    public void onDisable() {
        if (duelManager != null) duelManager.shutdownAll();
        if (statsManager != null) statsManager.saveAll();
        getLogger().info("OSDuels disabled.");
    }

    public static DuelsPlugin getInstance() { return instance;     }
    public ArenaManager getArenaManager()   { return arenaManager; }
    public DuelManager  getDuelManager()    { return duelManager;  }
    public StatsManager getStatsManager()   { return statsManager; }
}