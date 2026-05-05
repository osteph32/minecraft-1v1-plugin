package com.oliverstephenson.duels.duel;

import com.oliverstephenson.duels.arena.Arena;
import com.oliverstephenson.duels.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Duel {

    private final UUID challengerUUID;
    private final UUID challengedUUID;
    private final Arena arena;
    private final Kit  kit;

    private ItemStack[] challengerInventory, challengedInventory;
    private ItemStack challengerHelmet, challengedHelmet;
    private ItemStack challengerChestplate, challengedChestplate;
    private ItemStack challengerLeggings, challengedLeggings;
    private ItemStack challengerBoots, challengedBoots;
    private double challengerHealth, challengedHealth;

    private DuelState state = DuelState.COUNTDOWN;
    private int countdownTaskId = -1;

    public Duel(Player challenger, Player challenged, Arena arena, Kit kit) {
        this.challengerUUID = challenger.getUniqueId();
        this.challengedUUID = challenged.getUniqueId();
        this.arena = arena;
        this.kit = kit;
        snapshotInventory(challenger, true);
        snapshotInventory(challenged, false);
    }

    private void snapshotInventory(Player player, boolean isChallenger) {
        ItemStack[] contents = player.getInventory().getContents().clone();
        ItemStack helm = player.getInventory().getHelmet();
        ItemStack chest = player.getInventory().getChestplate();
        ItemStack legs = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();
        double health = player.getHealth();

        if (isChallenger) {
            challengerInventory  = contents;
            challengerHelmet     = helm;
            challengerChestplate = chest;
            challengerLeggings   = legs;
            challengerBoots      = boots;
            challengerHealth     = health;
        } else {
            challengedInventory  = contents;
            challengedHelmet     = helm;
            challengedChestplate = chest;
            challengedLeggings   = legs;
            challengedBoots      = boots;
            challengedHealth     = health;
        }
    }

    public void restoreInventory(Player player) {
        boolean isChallenger = player.getUniqueId().equals(challengerUUID);
        player.getInventory().clear();

        if (isChallenger) {
            player.getInventory().setContents(challengerInventory);
            player.getInventory().setHelmet(challengerHelmet);
            player.getInventory().setChestplate(challengerChestplate);
            player.getInventory().setLeggings(challengerLeggings);
            player.getInventory().setBoots(challengerBoots);
            player.setHealth(Math.min(challengerHealth, player.getMaxHealth()));
        } else {
            player.getInventory().setContents(challengedInventory);
            player.getInventory().setHelmet(challengedHelmet);
            player.getInventory().setChestplate(challengedChestplate);
            player.getInventory().setLeggings(challengedLeggings);
            player.getInventory().setBoots(challengedBoots);
            player.setHealth(Math.min(challengedHealth, player.getMaxHealth()));
        }

        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.getActivePotionEffects()
            .forEach(e -> player.removePotionEffect(e.getType()));
    }

    public boolean involves(UUID uuid) {
        return challengerUUID.equals(uuid) || challengedUUID.equals(uuid);
    }

    public UUID getOpponentUUID(UUID playerUUID) {
        return playerUUID.equals(challengerUUID) ? challengedUUID : challengerUUID;
    }

    public UUID      getChallengerUUID()        { return challengerUUID;   }
    public UUID      getChallengedUUID()         { return challengedUUID;   }
    public Arena     getArena()                  { return arena;            }
    public Kit       getKit()                    { return kit;              }
    public DuelState getState()                  { return state;            }
    public void      setState(DuelState s)       { this.state = s;          }
    public int       getCountdownTaskId()        { return countdownTaskId;  }
    public void      setCountdownTaskId(int id)  { this.countdownTaskId = id; }
}