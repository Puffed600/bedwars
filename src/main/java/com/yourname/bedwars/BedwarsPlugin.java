package com.yourname.bedwars;

import com.yourname.bedwars.arena.Arena;
import com.yourname.bedwars.arena.ArenaManager;
import com.yourname.bedwars.bed.BedService;
import com.yourname.bedwars.commands.AdminCommand;
import com.yourname.bedwars.combat.DamageListener;
import com.yourname.bedwars.combat.RespawnService;
import com.yourname.bedwars.combat.VoidListener;
import com.yourname.bedwars.fireball.FireballService;
import com.yourname.bedwars.integrity.BlockListener;
import com.yourname.bedwars.integrity.ExplosionListener;
import com.yourname.bedwars.shop.ItemShopGUI;
import com.yourname.bedwars.ui.TeamSelector;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BedwarsPlugin extends JavaPlugin {
    private static BedwarsPlugin instance;
    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        instance = this;
        this.arenaManager = new ArenaManager();
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new ItemShopGUI(), this);
        getServer().getPluginManager().registerEvents(new TeamSelector(), this);
        getServer().getPluginManager().registerEvents(new FireballService(), this);
        getServer().getPluginManager().registerEvents(new BedService(this), this);
        getServer().getPluginManager().registerEvents(new DamageListener(), this);
        getServer().getPluginManager().registerEvents(new RespawnService(), this);
        getServer().getPluginManager().registerEvents(new VoidListener(), this);

        getCommand("bw").setExecutor(new AdminCommand());

        // Start the global game ticker (runs every 1 second / 20 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Arena arena : arenaManager.getArenas()) {
                    arena.tick();
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    public static BedwarsPlugin getInstance() { return instance; }
    public ArenaManager getArenaManager() { return arenaManager; }
}
