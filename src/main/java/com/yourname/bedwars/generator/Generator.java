package com.yourname.bedwars.generator;

import com.yourname.bedwars.BedwarsPlugin;
import com.yourname.bedwars.arena.Arena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class Generator {
    private final Arena arena;
    private final GeneratorType type;
    private final Location location;
    private int liveCount = 0;
    private int cap = 0;
    private int intervalSeconds = 3;
    private int tier = 1;
    private ArmorStand hologram;

    public Generator(Arena arena, GeneratorType type, Location loc) { 
        this.arena = arena; 
        this.type = type; 
        this.location = loc; 
        applyTierRates();
        spawnHologram();
    }
    
    public void setTier(int tier) { this.tier = tier; applyTierRates(); }
    
    private void applyTierRates() {
        if (type == GeneratorType.IRON) {
            intervalSeconds = 3;
            if (arena.getTeamForGenerator(this) != null && arena.getTeamForGenerator(this).getForge() >= 1) intervalSeconds = 2;
        } else if (type == GeneratorType.GOLD) {
            intervalSeconds = 8;
            if (arena.getTeamForGenerator(this) != null && arena.getTeamForGenerator(this).getForge() >= 1) intervalSeconds = 6;
        } else if (type == GeneratorType.DIAMOND) {
            intervalSeconds = tier == 1 ? 30 : tier == 2 ? 20 : 15;
            cap = 4;
        } else if (type == GeneratorType.EMERALD) {
            intervalSeconds = tier == 1 ? 60 : tier == 2 ? 40 : 30;
            cap = 2;
        }
    }
    
    public void tick() {
        if (cap > 0 && liveCount >= cap) return;
        ItemStack stack = new ItemStack(materialFor(type), 1);
        Item dropped = location.getWorld().dropItem(location, stack);
        dropped.setMetadata("genId", new FixedMetadataValue(BedwarsPlugin.getInstance(), hashCode()));
        if (cap > 0) liveCount++;
    }
    
    public void onItemPickup() { if (cap > 0 && liveCount > 0) liveCount--; }

    private void spawnHologram() {
        Location holoLoc = location.clone().add(0, 2.5, 0);
        
        // Remove old hologram if it exists
        location.getWorld().getEntitiesByClass(ArmorStand.class).stream()
            .filter(a -> a.hasMetadata("gen_holo") && a.getLocation().distanceSquared(holoLoc) < 1)
            .forEach(org.bukkit.entity.Entity::remove);
            
        hologram = (ArmorStand) location.getWorld().spawnEntity(holoLoc, EntityType.ARMOR_STAND);
        hologram.setVisible(false);
        hologram.setGravity(false);
        hologram.setSmall(true);
        hologram.setMarker(true);
        hologram.setCustomNameVisible(true);
        hologram.setCustomName(getTypeName());
        hologram.setMetadata("gen_holo", new FixedMetadataValue(BedwarsPlugin.getInstance(), true));
    }

    public void updateHologram(int timeLeft) {
        if (hologram == null || hologram.isDead()) return;
        if (cap > 0 && liveCount >= cap) {
            hologram.setCustomName("\u00A7c" + getTypeName() + " \u00A77(Full)");
        } else {
            hologram.setCustomName("\u00A7e" + getTypeName() + " \u00A77- Next: \u00A7a" + timeLeft + "s");
        }
    }
    
    private String getTypeName() {
        switch (type) {
            case IRON: return "Iron";
            case GOLD: return "Gold";
            case DIAMOND: return "Diamond (Tier " + tier + ")";
            case EMERALD: return "Emerald (Tier " + tier + ")";
        }
        return "";
    }
    
    public int getIntervalTicks() { return intervalSeconds; }
    public Location getLocation() { return location; }
    public GeneratorType getType() { return type; }
    
    private static Material materialFor(GeneratorType t) {
        switch (t) { 
            case IRON: return Material.IRON_INGOT; 
            case GOLD: return Material.GOLD_INGOT; 
            case DIAMOND: return Material.DIAMOND; 
            case EMERALD: return Material.EMERALD; 
        }
        return Material.AIR;
    }
}
