package com.yourname.bedwars.shop;

import com.yourname.bedwars.BedwarsPlugin;
import com.yourname.bedwars.arena.Arena;
import com.yourname.bedwars.arena.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemShopGUI implements Listener {

    @EventHandler
    public void onVillagerClick(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Villager)) return;
        Villager v = (Villager) e.getRightClicked();
        Player p = e.getPlayer();
        
        if (v.hasMetadata("bw_shop")) {
            e.setCancelled(true);
            openItemShop(p);
        } else if (v.hasMetadata("bw_upgrades")) {
            e.setCancelled(true);
            openUpgradeShop(p);
        }
    }

    public void openItemShop(Player p) {
        org.bukkit.inventory.Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Item Shop");
        inv.setItem(0, createItem(Material.WOOL, ChatColor.WHITE + "Wool (16)", "7Cost: 4 Iron"));
        inv.setItem(1, createItem(Material.WOOD, ChatColor.WHITE + "Wood (16)", "7Cost: 4 Gold"));
        inv.setItem(2, createItem(Material.ENDER_STONE, ChatColor.WHITE + "End Stone (12)", "7Cost: 24 Iron"));
        inv.setItem(3, createItem(Material.GLASS, ChatColor.WHITE + "Glass (4)", "7Cost: 12 Iron"));
        inv.setItem(4, createItem(Material.OBSIDIAN, ChatColor.WHITE + "Obsidian (4)", "7Cost: 4 Emerald"));
        
        inv.setItem(9, createItem(Material.STONE_SWORD, ChatColor.WHITE + "Stone Sword", "7Cost: 10 Iron"));
        inv.setItem(10, createItem(Material.IRON_SWORD, ChatColor.WHITE + "Iron Sword", "7Cost: 7 Gold"));
        inv.setItem(11, createItem(Material.GOLDEN_APPLE, ChatColor.WHITE + "Golden Apple", "7Cost: 3 Gold"));
        inv.setItem(12, createItem(Material.TNT, ChatColor.WHITE + "TNT", "7Cost: 4 Gold"));
        inv.setItem(13, createItem(Material.FIREBALL, ChatColor.WHITE + "Fireball", "7Cost: 40 Iron"));
        
        inv.setItem(18, createItem(Material.CHAINMAIL_BOOTS, ChatColor.WHITE + "Chainmail Armor", "7Cost: 40 Iron"));
        inv.setItem(19, createItem(Material.IRON_BOOTS, ChatColor.WHITE + "Iron Armor", "7Cost: 12 Gold"));
        inv.setItem(20, createItem(Material.DIAMOND_BOOTS, ChatColor.WHITE + "Diamond Armor", "7Cost: 8 Emerald"));
        
        inv.setItem(27, createItem(Material.WOOD_PICKAXE, ChatColor.WHITE + "Wood Pickaxe", "7Cost: 10 Iron"));
        inv.setItem(28, createItem(Material.IRON_PICKAXE, ChatColor.WHITE + "Iron Pickaxe", "7Cost: 10 Iron"));
        inv.setItem(29, createItem(Material.DIAMOND_PICKAXE, ChatColor.WHITE + "Diamond Pickaxe", "7Cost: 6 Gold"));
        
        p.openInventory(inv);
    }

    public void openUpgradeShop(Player p) {
        org.bukkit.inventory.Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_BLUE + "Team Upgrades");
        inv.setItem(0, createItem(Material.IRON_SWORD, ChatColor.WHITE + "Sharpened Swords", "7Cost: 4 Diamond"));
        inv.setItem(1, createItem(Material.IRON_CHESTPLATE, ChatColor.WHITE + "Reinforced Armor I", "7Cost: 2 Diamond"));
        inv.setItem(2, createItem(Material.FURNACE, ChatColor.WHITE + "Forge I (Faster Gens)", "7Cost: 2 Diamond"));
        p.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(java.util.Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onShopClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (!title.equals(ChatColor.DARK_GREEN + "Item Shop") && !title.equals(ChatColor.DARK_BLUE + "Team Upgrades")) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        Arena a = BedwarsPlugin.getInstance().getArenaManager().arenaForPlayer(p);
        if (a == null) return;
        
        if (title.equals(ChatColor.DARK_GREEN + "Item Shop")) {
            handleItemPurchase(p, a, clicked.getType());
        } else {
            handleUpgradePurchase(p, a, clicked.getType());
        }
    }

    private void handleItemPurchase(Player p, Arena a, Material mat) {
        switch (mat) {
            case WOOL:
                if (charge(p, Material.IRON_INGOT, 4)) giveItem(p, new ItemStack(Material.WOOL, 16));
                break;
            case WOOD:
                if (charge(p, Material.GOLD_INGOT, 4)) giveItem(p, new ItemStack(Material.WOOD, 16));
                break;
            case ENDER_STONE:
                if (charge(p, Material.IRON_INGOT, 24)) giveItem(p, new ItemStack(Material.ENDER_STONE, 12));
                break;
            case GLASS:
                if (charge(p, Material.IRON_INGOT, 12)) giveItem(p, new ItemStack(Material.GLASS, 4));
                break;
            case OBSIDIAN:
                if (charge(p, Material.EMERALD, 4)) giveItem(p, new ItemStack(Material.OBSIDIAN, 4));
                break;
            case STONE_SWORD:
                if (charge(p, Material.IRON_INGOT, 10)) giveItem(p, new ItemStack(Material.STONE_SWORD, 1));
                break;
            case IRON_SWORD:
                if (charge(p, Material.GOLD_INGOT, 7)) giveItem(p, new ItemStack(Material.IRON_SWORD, 1));
                break;
            case GOLDEN_APPLE:
                if (charge(p, Material.GOLD_INGOT, 3)) giveItem(p, new ItemStack(Material.GOLDEN_APPLE, 1));
                break;
            case TNT:
                if (charge(p, Material.GOLD_INGOT, 4)) giveItem(p, new ItemStack(Material.TNT, 1));
                break;
            case FIREBALL:
                if (charge(p, Material.IRON_INGOT, 40)) giveItem(p, new ItemStack(Material.FIREBALL, 1));
                break;
            case CHAINMAIL_BOOTS:
                if (charge(p, Material.IRON_INGOT, 40)) equipArmor(p, 1, a.getTeam(p));
                break;
            case IRON_BOOTS:
                if (charge(p, Material.GOLD_INGOT, 12)) equipArmor(p, 2, a.getTeam(p));
                break;
            case DIAMOND_BOOTS:
                if (charge(p, Material.EMERALD, 8)) equipArmor(p, 3, a.getTeam(p));
                break;
            case WOOD_PICKAXE:
                if (charge(p, Material.IRON_INGOT, 10)) giveItem(p, new ItemStack(Material.WOOD_PICKAXE, 1));
                break;
            case IRON_PICKAXE:
                if (charge(p, Material.IRON_INGOT, 10)) giveItem(p, new ItemStack(Material.IRON_PICKAXE, 1));
                break;
            case DIAMOND_PICKAXE:
                if (charge(p, Material.GOLD_INGOT, 6)) giveItem(p, new ItemStack(Material.DIAMOND_PICKAXE, 1));
                break;
            default: break;
        }
    }

    private void handleUpgradePurchase(Player p, Arena a, Material mat) {
        Team t = a.getTeam(p);
        if (t == null) return;
        switch (mat) {
            case IRON_SWORD:
                if (charge(p, Material.DIAMOND, 4)) { t.setSharpness(1); p.sendMessage("\u00A7aSwords upgraded!"); }
                break;
            case IRON_CHESTPLATE:
                if (charge(p, Material.DIAMOND, 2)) { t.setProtection(t.getProtection()+1); p.sendMessage("\u00A7aArmor upgraded!"); }
                break;
            case FURNACE:
                if (charge(p, Material.DIAMOND, 2)) { 
                    t.setForge(t.getForge()+1); 
                    p.sendMessage("\u00A7aForge upgraded!"); 
                    // Reset gen intervals to apply forge speed
                    for (com.yourname.bedwars.generator.Generator g : a.getGeneratorManager().getGenerators()) {
                        if (g.getType() == com.yourname.bedwars.generator.GeneratorType.IRON || g.getType() == com.yourname.bedwars.generator.GeneratorType.GOLD) g.setTier(1);
                    }
                }
                break;
            default: break;
        }
    }

    private boolean charge(Player p, Material currency, int cost) {
        PlayerInventory inv = p.getInventory();
        int remaining = cost;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s == null || s.getType() != currency) continue;
            int take = Math.min(s.getAmount(), remaining);
            s.setAmount(s.getAmount() - take);
            remaining -= take;
            if (s.getAmount() == 0) inv.setItem(i, null); else inv.setItem(i, s);
            if (remaining <= 0) break;
        }
        if (remaining > 0) {
            p.sendMessage("\u00A7cNot enough " + currency.name() + "!");
            // Refund what we took
            giveItem(p, new ItemStack(currency, cost - remaining));
            return false;
        }
        p.playSound(p.getLocation(), org.bukkit.Sound.ORB_PICKUP, 0.5f, 1.4f);
        return true;
    }

    private void giveItem(Player p, ItemStack item) {
        p.getInventory().addItem(item).forEach((idx, leftover) -> p.getWorld().dropItemNaturally(p.getLocation(), leftover));
    }

    private void equipArmor(Player p, int tier, Team t) {
        int currentTier = p.hasMetadata("armor_tier") ? p.getMetadata("armor_tier").get(0).asInt() : 0;
        if (tier <= currentTier) {
            p.sendMessage("\u00A7cYou already have equal or better armor!");
            return;
        }
        p.setMetadata("armor_tier", new org.bukkit.metadata.FixedMetadataValue(BedwarsPlugin.getInstance(), tier));
        
        PlayerInventory inv = p.getInventory();
        if (tier == 1) {
            inv.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
            inv.setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
        } else if (tier == 2) {
            inv.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
            inv.setBoots(new ItemStack(Material.IRON_BOOTS));
        } else if (tier == 3) {
            inv.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
            inv.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        }
        p.sendMessage("\u00A7aArmor equipped!");
    }

    @EventHandler
    public void onArmorClick(InventoryClickEvent e) {
        if (e.getSlotType() == InventoryType.SlotType.ARMOR) e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Arena a = BedwarsPlugin.getInstance().getArenaManager().arenaForPlayer(e.getPlayer());
        if (a == null) return;
        ItemStack s = e.getItemDrop().getItemStack();
        if (s.getType() == Material.LEATHER_BOOTS || s.getType() == Material.LEATHER_LEGGINGS || s.getType() == Material.LEATHER_CHESTPLATE || s.getType() == Material.LEATHER_HELMET || s.getType() == Material.WOOD_SWORD) {
            e.setCancelled(true);
        }
    }
}
