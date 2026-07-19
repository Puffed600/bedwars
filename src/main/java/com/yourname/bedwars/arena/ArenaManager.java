package com.yourname.bedwars.arena;

import com.yourname.bedwars.BedwarsPlugin;
import com.yourname.bedwars.generator.Generator;
import com.yourname.bedwars.generator.GeneratorType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ArenaManager {
    private final List<Arena> arenas = new ArrayList<>();
    private final Map<UUID, Arena> playerArenas = new HashMap<>();
    private File configFile;
    private FileConfiguration config;

    public ArenaManager() {
        configFile = new File(BedwarsPlugin.getInstance().getDataFolder(), "arenas.yml");
        if (!configFile.exists()) {
            BedwarsPlugin.getInstance().getDataFolder().mkdirs();
            try { configFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        loadArenas();
    }

    private void loadArenas() {
        if (!config.contains("arenas")) return;
        for (String arenaName : config.getConfigurationSection("arenas").getKeys(false)) {
            Arena arena = new Arena(arenaName);
            String base = "arenas." + arenaName;
            
            arena.setLobby(parseLocation(config.getString(base + ".lobby")));
            arena.setPos1(parseLocation(config.getString(base + ".pos1")));
            arena.setPos2(parseLocation(config.getString(base + ".pos2")));
            
            if (config.contains(base + ".teams")) {
                for (String colorName : config.getConfigurationSection(base + ".teams").getKeys(false)) {
                    String teamBase = base + ".teams." + colorName;
                    ChatColor color = ChatColor.valueOf(colorName.toUpperCase());
                    Team team = new Team(colorName.toUpperCase(), color);
                    team.setSpawn(parseLocation(config.getString(teamBase + ".spawn")));
                    team.setBedHead(parseLocation(config.getString(teamBase + ".bed-head")));
                    team.setBedFoot(parseLocation(config.getString(teamBase + ".bed-foot")));
                    team.setShop(parseLocation(config.getString(teamBase + ".shop")));
                    team.setUpgradeShop(parseLocation(config.getString(teamBase + ".upgrade-shop")));
                    arena.addTeam(team);
                    
                    if (team.getShop() != null) spawnVillager(team.getShop(), "\u00A7aItem Shop", "bw_shop");
                    if (team.getUpgradeShop() != null) spawnVillager(team.getUpgradeShop(), "\u00A7bUpgrades", "bw_upgrades");
                }
            }

            if (config.contains(base + ".generators")) {
                for (String genId : config.getConfigurationSection(base + ".generators").getKeys(false)) {
                    String genBase = base + ".generators." + genId;
                    GeneratorType type = GeneratorType.valueOf(config.getString(genBase + ".type"));
                    Location loc = parseLocation(config.getString(genBase + ".loc"));
                    Generator gen = new Generator(arena, type, loc);
                    arena.getGeneratorManager().register(gen);
                }
                arena.startGenerators();
            }
            arenas.add(arena);
        }
    }

    public void spawnVillager(Location loc, String name, String metaKey) {
        loc.getWorld().getEntitiesByClass(Villager.class).stream()
            .filter(v -> v.hasMetadata(metaKey) && v.getLocation().distanceSquared(loc) < 2)
            .forEach(org.bukkit.entity.Entity::remove);
            
        Villager v = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
        v.setCustomName(name);
        v.setCustomNameVisible(true);
        v.setProfession(Villager.Profession.FARMER);
        v.setMetadata(metaKey, new FixedMetadataValue(BedwarsPlugin.getInstance(), true));
        
        v.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 100, false, false));
        v.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, -100, false, false));
    }

    public void saveArena(Arena arena) {
        String base = "arenas." + arena.getName();
        config.set(base + ".lobby", formatLocation(arena.getLobby()));
        config.set(base + ".pos1", formatLocation(arena.getPos1()));
        config.set(base + ".pos2", formatLocation(arena.getPos2()));
        
        int i = 0;
        config.set(base + ".generators", null);
        for (Generator g : arena.getGeneratorManager().getGenerators()) {
            String genBase = base + ".generators." + i;
            config.set(genBase + ".type", g.getType().name());
            config.set(genBase + ".loc", formatLocation(g.getLocation()));
            i++;
        }

        for (Team t : arena.getTeams()) {
            String teamBase = base + ".teams." + t.getName();
            config.set(teamBase + ".spawn", formatLocation(t.getSpawn()));
            config.set(teamBase + ".bed-head", formatLocation(t.getBedHead()));
            config.set(teamBase + ".bed-foot", formatLocation(t.getBedFoot()));
            config.set(teamBase + ".shop", formatLocation(t.getShop()));
            config.set(teamBase + ".upgrade-shop", formatLocation(t.getUpgradeShop()));
        }
        
        try { config.save(configFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public void addPlayerToArena(Player p, Arena a) { 
        if (a == null) playerArenas.remove(p.getUniqueId());
        else playerArenas.put(p.getUniqueId(), a); 
    }
    
    public Arena arenaForPlayer(Player p) { return playerArenas.get(p.getUniqueId()); }

    private String formatLocation(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    private Location parseLocation(String str) {
        if (str == null || str.isEmpty()) return null;
        String[] parts = str.split(",");
        World w = Bukkit.getWorld(parts[0]);
        if (w == null) return null;
        return new Location(w, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));
    }

    public Arena getArena(String name) {
        for (Arena a : arenas) if (a.getName().equalsIgnoreCase(name)) return a;
        return null;
    }

    public Arena arenaAt(Location loc) {
        for (Arena a : arenas) {
            if (a.getLobby() != null && a.getLobby().getWorld().equals(loc.getWorld())) return a;
            if (a.getPos1() != null && a.getPos1().getWorld().equals(loc.getWorld())) return a;
            if (a.getPos2() != null && a.getPos2().getWorld().equals(loc.getWorld())) return a;
            for (Team t : a.getTeams()) {
                if (t.getSpawn() != null && t.getSpawn().getWorld().equals(loc.getWorld())) return a;
            }
        }
        return null;
    }

    public List<Arena> getArenas() { return arenas; }
}
