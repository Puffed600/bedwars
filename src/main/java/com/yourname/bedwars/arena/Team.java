package com.yourname.bedwars.arena;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class Team {
    private final String name;
    private final ChatColor chatColor;
    private Location spawn;
    private Location bedHead;
    private Location bedFoot;
    private Location shop;
    private Location upgradeShop;
    private boolean hasBed = true;
    private final List<Player> members = new ArrayList<>();

    private int sharpness = 0;
    private int protection = 0;
    private int forge = 0;

    public Team(String name, ChatColor chatColor) { this.name = name; this.chatColor = chatColor; }
    public void breakBed(Player breaker) { this.hasBed = false; }
    public void setHasBed(boolean hasBed) { this.hasBed = hasBed; }
    
    public String getName() { return name; }
    public String getColoredName() { return chatColor + name; }
    public ChatColor getChatColor() { return chatColor; }
    
    public Location getSpawn() { return spawn; }
    public void setSpawn(Location spawn) { this.spawn = spawn; }
    
    public Location getBedHead() { return bedHead; }
    public void setBedHead(Location bedHead) { this.bedHead = bedHead; }
    
    public Location getBedFoot() { return bedFoot; }
    public void setBedFoot(Location bedFoot) { this.bedFoot = bedFoot; }

    public Location getShop() { return shop; }
    public void setShop(Location shop) { this.shop = shop; }

    public Location getUpgradeShop() { return upgradeShop; }
    public void setUpgradeShop(Location upgradeShop) { this.upgradeShop = upgradeShop; }
    
    public boolean hasBed() { return hasBed; }
    public List<Player> getMembers() { return members; }

    public int getSharpness() { return sharpness; }
    public void setSharpness(int sharpness) { this.sharpness = sharpness; }
    public int getProtection() { return protection; }
    public void setProtection(int protection) { this.protection = protection; }
    public int getForge() { return forge; }
    public void setForge(int forge) { this.forge = forge; }
}
