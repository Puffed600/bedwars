package com.yourname.bedwars.arena;

import com.yourname.bedwars.BedwarsPlugin;
import com.yourname.bedwars.generator.Generator;
import com.yourname.bedwars.generator.GeneratorManager;
import com.yourname.bedwars.generator.GeneratorType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Arena {
    private final String name;
    private GamePhase phase = GamePhase.WAITING;
    private Location lobby;
    private Location pos1;
    private Location pos2;
    private final List<Team> teams = new ArrayList<>();
    private final Map<UUID, Team> playerTeam = new HashMap<>();
    private final PlacedBlocksTracker placedBlocks = new PlacedBlocksTracker();
    private final GeneratorManager generatorManager;
    private int elapsedSeconds = 0;

    public Arena(String name) { 
        this.name = name; 
        this.generatorManager = new GeneratorManager(this);
    }
    
    public void addTeam(Team team) { this.teams.add(team); }
    public Team getTeam(String name) {
        for (Team t : teams) if (t.getName().equalsIgnoreCase(name)) return t;
        return null;
    }

    public Team teamForBed(Location loc) {
        for (Team t : teams) {
            if (t.getBedHead() != null && t.getBedHead().equals(loc)) return t;
            if (t.getBedFoot() != null && t.getBedFoot().equals(loc)) return t;
        }
        return null;
    }

    public Team getTeamForGenerator(Generator g) {
        for (Team t : teams) {
            if (t.getSpawn() != null && t.getSpawn().getWorld().equals(g.getLocation().getWorld())) {
                if (t.getSpawn().distanceSquared(g.getLocation()) < 2500) return t;
            }
        }
        return null;
    }

    public void joinPlayer(Player p) {
        Team smallest = null;
        for (Team t : teams) {
            if (smallest == null || t.getMembers().size() < smallest.getMembers().size()) {
                smallest = t;
            }
        }
        if (smallest == null) {
            p.sendMessage("\u00A7cNo teams available to join!");
            return;
        }
        
        playerTeam.put(p.getUniqueId(), smallest);
        smallest.getMembers().add(p);
        BedwarsPlugin.getInstance().getArenaManager().addPlayerToArena(p, this);
        
        p.sendMessage("\u00A7eYou joined team " + smallest.getColoredName() + "\u00A7e!");
        if (lobby != null) p.teleport(lobby);
    }

    public void removePlayer(Player p) {
        Team t = playerTeam.remove(p.getUniqueId());
        if (t != null) t.getMembers().remove(p);
        BedwarsPlugin.getInstance().getArenaManager().addPlayerToArena(p, null);
    }

    public void startGenerators() {
        if (!generatorManager.isRunning()) {
            generatorManager.runTaskTimer(BedwarsPlugin.getInstance(), 0L, 20L);
        }
    }

    public void tick() {
        if (phase == GamePhase.STARTING) {
            startGame();
            return;
        }
        if (phase != GamePhase.IN_PROGRESS) return;

        elapsedSeconds++;
        int t2D = BedwarsPlugin.getInstance().getConfig().getInt("timeline.diamond-tier-2", 300);
        int t2E = BedwarsPlugin.getInstance().getConfig().getInt("timeline.emerald-tier-2", 660);
        int t3D = BedwarsPlugin.getInstance().getConfig().getInt("timeline.diamond-tier-3", 1020);
        int t3E = BedwarsPlugin.getInstance().getConfig().getInt("timeline.emerald-tier-3", 1380);
        int tBed = BedwarsPlugin.getInstance().getConfig().getInt("timeline.bed-destruction", 1740);
        int tEnd = BedwarsPlugin.getInstance().getConfig().getInt("timeline.hard-end", 2940);

        if (elapsedSeconds == t2D) upgradeGens(GeneratorType.DIAMOND, 2);
        if (elapsedSeconds == t2E) upgradeGens(GeneratorType.EMERALD, 2);
        if (elapsedSeconds == t3D) upgradeGens(GeneratorType.DIAMOND, 3);
        if (elapsedSeconds == t3E) upgradeGens(GeneratorType.EMERALD, 3);
        if (elapsedSeconds == tBed) destroyAllBeds();
        if (elapsedSeconds == tEnd) endGame(null);
    }

    private void upgradeGens(GeneratorType type, int tier) {
        broadcast("\u00A7b" + type.name() + " generators upgraded to Tier " + tier + "!");
        for (Generator g : generatorManager.getGenerators()) {
            if (g.getType() == type) g.setTier(tier);
        }
    }

    private void destroyAllBeds() {
        broadcast("\u00A7c\u00A7lALL BEDS HAVE BEEN DESTROYED!");
        for (Team t : teams) {
            if (t.hasBed()) {
                t.breakBed(null);
                if (t.getBedHead() != null) t.getBedHead().getBlock().setType(Material.AIR);
                if (t.getBedFoot() != null) t.getBedFoot().getBlock().setType(Material.AIR);
            }
        }
    }

    public void checkWin() {
        if (phase != GamePhase.IN_PROGRESS) return;
        List<Team> alive = new ArrayList<>();
        for (Team t : teams) {
            boolean hasLiving = false;
            for (Player p : t.getMembers()) {
                if (p.getGameMode() != GameMode.SPECTATOR) { hasLiving = true; break; }
            }
            if (hasLiving) alive.add(t);
        }
        if (alive.size() == 1) endGame(alive.get(0));
        else if (alive.size() == 0) endGame(null);
    }

    public void forceStart() { phase = GamePhase.STARTING; }

    public void startGame() {
        phase = GamePhase.IN_PROGRESS;
        Bukkit.broadcastMessage("\u00A7a\u00A7lBEDWARS HAS STARTED!");
        
        for (Team t : teams) {
            for (Player p : t.getMembers()) {
                p.setGameMode(GameMode.SURVIVAL);
                p.teleport(t.getSpawn());
                
                PlayerInventory inv = p.getInventory();
                inv.clear();
                inv.addItem(new ItemStack(Material.WOOD_SWORD));
                inv.setHelmet(new ItemStack(Material.LEATHER_HELMET));
                inv.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                inv.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                inv.setBoots(new ItemStack(Material.LEATHER_BOOTS));
            }
        }
    }

    public void endGame(Team winner) {
        phase = GamePhase.ENDING;
        if (winner != null) broadcast("\u00A76\u00A7l" + winner.getColoredName() + " \u00A7r\u00A76wins!");
        else broadcast("\u00A7eGame ended in a draw.");
        
        generatorManager.cancel();
        
        Bukkit.getScheduler().runTaskLater(BedwarsPlugin.getInstance(), () -> {
            for (Player p : getPlayers()) {
                p.getInventory().clear();
                if (lobby != null) p.teleport(lobby);
                p.setGameMode(GameMode.SURVIVAL);
            }
            phase = GamePhase.WAITING;
            elapsedSeconds = 0;
            
            // Reset beds for next game
            for (Team t : teams) {
                t.breakBed(null); // temp set to false
                // We can't easily reset the bed block without world edit, so just set hasBed back to true
                t.setHasBed(true);
            }
        }, 200L);
    }

    public void stopGame() {
        broadcast("\u00A7cGame stopped by admin.");
        endGame(null);
    }

    public void broadcast(String msg) {
        for (Player p : getPlayers()) p.sendMessage(msg);
    }

    public List<Player> getPlayers() {
        List<Player> list = new ArrayList<>();
        for (UUID id : playerTeam.keySet()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) list.add(p);
        }
        return list;
    }

    public PlacedBlocksTracker getPlacedBlocks() { return placedBlocks; }
    public GeneratorManager getGeneratorManager() { return generatorManager; }
    public Location getSpawn() { return lobby; }
    public String getName() { return name; }
    public GamePhase getPhase() { return phase; }
    public boolean isPaused() { return false; }
    public boolean isFriendlyFire() { return false; }
    public boolean isLockTeams() { return false; }
    public List<Team> getTeams() { return teams; }
    public Team getTeam(Player p) { return playerTeam.get(p.getUniqueId()); }
    
    public Location getLobby() { return lobby; }
    public void setLobby(Location lobby) { this.lobby = lobby; }
    
    public Location getPos1() { return pos1; }
    public void setPos1(Location pos1) { this.pos1 = pos1; }
    
    public Location getPos2() { return pos2; }
    public void setPos2(Location pos2) { this.pos2 = pos2; }
}
