package com.yourname.bedwars.combat;

import com.yourname.bedwars.BedwarsPlugin;
import com.yourname.bedwars.arena.Arena;
import com.yourname.bedwars.arena.Team;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RespawnService implements Listener {
    private final Map<UUID, Long> spawnImmunity = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        Arena a = BedwarsPlugin.getInstance().getArenaManager().arenaForPlayer(p);
        if (a == null) return;
        Team t = a.getTeam(p);
        if (t == null) return;

        e.getDrops().clear();
        e.setDeathMessage(null);

        if (!t.hasBed()) {
            Bukkit.getScheduler().runTaskLater(BedwarsPlugin.getInstance(), () -> {
                p.setGameMode(GameMode.SPECTATOR);
                a.checkWin(); // Check if this elimination won the game
            }, 1L);
        } else {
            Bukkit.getScheduler().runTaskLater(BedwarsPlugin.getInstance(), () -> {
                p.spigot().respawn();
                Bukkit.getScheduler().runTaskLater(BedwarsPlugin.getInstance(), () -> {
                    p.teleport(t.getSpawn());
                    PlayerInventory inv = p.getInventory();
                    inv.clear();
                    inv.addItem(new ItemStack(Material.WOOD_SWORD));
                    inv.setHelmet(new ItemStack(Material.LEATHER_HELMET));
                    inv.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                    inv.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                    inv.setBoots(new ItemStack(Material.LEATHER_BOOTS));
                    
                    // Restore armor tier if they bought one
                    if (p.hasMetadata("armor_tier")) {
                        int tier = p.getMetadata("armor_tier").get(0).asInt();
                        if (tier == 1) { inv.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS)); inv.setBoots(new ItemStack(Material.CHAINMAIL_BOOTS)); }
                        else if (tier == 2) { inv.setLeggings(new ItemStack(Material.IRON_LEGGINGS)); inv.setBoots(new ItemStack(Material.IRON_BOOTS)); }
                        else if (tier == 3) { inv.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS)); inv.setBoots(new ItemStack(Material.DIAMOND_BOOTS)); }
                    }
                    
                    spawnImmunity.put(p.getUniqueId(), System.currentTimeMillis() + 2000L);
                }, 2L);
            }, 1L); 
        }
    }

    public Long spawnImmunityUntil(Player p) { return spawnImmunity.get(p.getUniqueId()); }
}
