package com.oliverstephenson.duels.stats;

import java.util.UUID;

public record PlayerStats(UUID uuid, String name, int wins, int losses) {

    public int totalGames() { return wins + losses; }

    public double winRate() {
        return totalGames() == 0 ? 0.0 : (double) wins / totalGames() * 100.0;
    }

    public String formattedWinRate() {
        return String.format("%.1f%%", winRate());
    }
}