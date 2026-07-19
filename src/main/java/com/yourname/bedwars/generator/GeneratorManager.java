package com.yourname.bedwars.generator;

import com.yourname.bedwars.BedwarsPlugin;
import com.yourname.bedwars.arena.Arena;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneratorManager extends BukkitRunnable implements Listener {
    private final Arena arena;
    private final List<Generator> generators = new ArrayList<>();
    private final Map<Integer, Integer> counters = new HashMap<>();
    private boolean running = false;

    public GeneratorManager(Arena a) { this.arena = a; }

    public void register(Generator g) {
        generators.add(g);
        counters.put(g.hashCode(), g.getIntervalTicks());
    }

    @Override
    public void run() {
        running = true;
        for (Generator g : generators) {
            int left = counters.get(g.hashCode()) - 1;
            if (left <= 0) {
                g.tick();
                left = g.getIntervalTicks();
            }
            counters.put(g.hashCode(), left);
            
            // Update the floating text timer
            g.updateHologram(left);
        }
    }

    public void pickupItem(Item item, Player p) {
        if (!item.hasMetadata("genId")) return;
        int id = item.getMetadata("genId").get(0).asInt();
        for (Generator g : generators) {
            if (g.hashCode() == id) { g.onItemPickup(); break; }
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        Player p = e.getPlayer();
        Arena a = BedwarsPlugin.getInstance().getArenaManager().arenaForPlayer(p);
        if (a == null || a != arena) return;
        pickupItem(e.getItem(), p);
    }

    public List<Generator> getGenerators() { return generators; }
    public boolean isRunning() { return running; }
}
