package com.oliverstephenson.duels.stats;

import com.oliverstephenson.duels.DuelsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StatsManager {

    private final DuelsPlugin plugin;
    private final File        statsFile;
    private YamlConfiguration statsConfig;

    public StatsManager(DuelsPlugin plugin) {
        this.plugin    = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "stats.yml");
        load();
    }

    private void load() {
        if (!statsFile.exists()) {
            try { statsFile.createNewFile(); }
            catch (IOException e) {
                plugin.getLogger().severe(
                        "Could not create stats.yml: " + e.getMessage());
            }
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
    }

    public void saveAll() {
        try { statsConfig.save(statsFile); }
        catch (IOException e) {
            plugin.getLogger().severe(
                    "Could not save stats.yml: " + e.getMessage());
        }
    }

    public void recordWin(UUID uuid) {
        String path = "players." + uuid;
        statsConfig.set(path + ".wins",
                statsConfig.getInt(path + ".wins", 0) + 1);
        saveAll();
    }

    public void recordLoss(UUID uuid) {
        String path = "players." + uuid;
        statsConfig.set(path + ".losses",
                statsConfig.getInt(path + ".losses", 0) + 1);
        saveAll();
    }

    public void cacheName(UUID uuid, String name) {
        statsConfig.set("players." + uuid + ".name", name);
    }

    public PlayerStats getStats(UUID uuid) {
        String path   = "players." + uuid;
        String name   = statsConfig.getString(path + ".name", uuid.toString());
        int    wins   = statsConfig.getInt(path + ".wins",    0);
        int    losses = statsConfig.getInt(path + ".losses",  0);
        return new PlayerStats(uuid, name, wins, losses);
    }

    public List<PlayerStats> getLeaderboard(int limit) {
        List<PlayerStats> list = new ArrayList<>();
        var section = statsConfig.getConfigurationSection("players");
        if (section == null) return list;

        for (String uuidStr : section.getKeys(false)) {
            try { list.add(getStats(UUID.fromString(uuidStr))); }
            catch (IllegalArgumentException ignored) {}
        }

        list.sort(Comparator.comparingInt(PlayerStats::wins).reversed());
        return list.subList(0, Math.min(limit, list.size()));
    }
}