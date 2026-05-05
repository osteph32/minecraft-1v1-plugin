package com.oliverstephenson.duels.kit;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public enum Kit {

    SWORD("Sword") {
        @Override
        public ItemStack[] buildItems() {
            return flatten(
                new ItemStack[]{ enchanted(new ItemStack(Material.DIAMOND_SWORD), Enchantment.SHARPNESS, 3),
                    new ItemStack(Material.GOLDEN_APPLE, 3) },
                armor(Material.IRON_HELMET, Material.IRON_CHESTPLATE,
                    Material.IRON_LEGGINGS, Material.IRON_BOOTS)
            );
        }
    },

    BOW("Bow") {
        @Override
        public ItemStack[] buildItems() {
            return flatten(
                new ItemStack[]{ enchanted(new ItemStack(Material.BOW), Enchantment.POWER, 3),
                    new ItemStack(Material.ARROW, 32),
                    new ItemStack(Material.IRON_SWORD),
                    new ItemStack(Material.GOLDEN_APPLE, 2) },
                armor(Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE,
                    Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS)
            );
        }
    },

    UHC("UHC") {
        @Override
        public ItemStack[] buildItems() {
            return flatten(
                new ItemStack[]{ enchanted(new ItemStack(Material.DIAMOND_SWORD), Enchantment.SHARPNESS, 2),
                    enchanted(new ItemStack(Material.BOW), Enchantment.POWER, 2),
                    new ItemStack(Material.ARROW, 16),
                    new ItemStack(Material.GOLDEN_APPLE, 1),
                    new ItemStack(Material.COOKED_BEEF, 10) },
                armor(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE,
                    Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS)
            );
        }

        @Override
        public PotionEffect[] buildEffects() {
            return new PotionEffect[]{
                new PotionEffect(PotionEffectType.REGENERATION, 0, 0, false, false)
            };
        }
    };

    private final String displayName;

    Kit(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }

    public abstract ItemStack[] buildItems();

    public PotionEffect[] buildEffects() { return new PotionEffect[0]; }

    public void applyTo(Player player) {
        player.getInventory().clear();
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20f);

        for (ItemStack item : buildItems()) {
            if (item == null) continue;
            switch (item.getType()) {
                case DIAMOND_HELMET, IRON_HELMET, CHAINMAIL_HELMET,
                    GOLDEN_HELMET, LEATHER_HELMET ->
                        player.getInventory().setHelmet(item);
                case DIAMOND_CHESTPLATE, IRON_CHESTPLATE, CHAINMAIL_CHESTPLATE,
                    GOLDEN_CHESTPLATE, LEATHER_CHESTPLATE ->
                        player.getInventory().setChestplate(item);
                case DIAMOND_LEGGINGS, IRON_LEGGINGS, CHAINMAIL_LEGGINGS,
                    GOLDEN_LEGGINGS, LEATHER_LEGGINGS ->
                        player.getInventory().setLeggings(item);
                case DIAMOND_BOOTS, IRON_BOOTS, CHAINMAIL_BOOTS,
                    GOLDEN_BOOTS, LEATHER_BOOTS ->
                        player.getInventory().setBoots(item);
                default -> player.getInventory().addItem(item);
            }
        }

        player.getActivePotionEffects()
            .forEach(e -> player.removePotionEffect(e.getType()));
        for (PotionEffect effect : buildEffects()) {
            player.addPotionEffect(effect);
        }
    }

    private static ItemStack enchanted(ItemStack item, Enchantment ench, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(ench, level, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack[] armor(Material helmet, Material chest, Material legs, Material boots) {
        return new ItemStack[]{
            new ItemStack(helmet), new ItemStack(chest),
            new ItemStack(legs),   new ItemStack(boots)
        };
    }

    private static ItemStack[] flatten(ItemStack[]... arrays) {
        int total = 0;
        for (ItemStack[] a : arrays) total += a.length;
        ItemStack[] result = new ItemStack[total];
        int i = 0;
        for (ItemStack[] a : arrays)
            for (ItemStack item : a) result[i++] = item;
        return result;
    }
}