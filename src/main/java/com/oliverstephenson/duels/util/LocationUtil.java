package com.oliverstephenson.duels.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class LocationUtil {

    private LocationUtil() {}

    public static Location fromConfig(ConfigurationSection section) {
        if (section == null) return null;

        String worldName = section.getString("world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Bukkit.getLogger().warning("[OSDuels] World '" + worldName + "' not found.");
            return null;
        }

        double x = section.getDouble("x", 0);
        double y = section.getDouble("y", 64);
        double z = section.getDouble("z", 0);
        float yaw = (float) section.getDouble("yaw", 0);
        float pitch = (float) section.getDouble("pitch", 0);

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static void toConfig(FileConfiguration config, String path, Location loc) {
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", (double) loc.getYaw());
        config.set(path + ".pitch", (double) loc.getPitch());
    }

    public static Location getLobby(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("lobby");
        if (section != null) {
            Location loc = fromConfig(section);
            if (loc != null) return loc;
        }
        return Bukkit.getWorlds().get(0).getSpawnLocation();
    }
}