package com.yourname.bedwars.integrity;

import com.yourname.bedwars.BedwarsPlugin;
import com.yourname.bedwars.arena.Arena;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

public class ExplosionListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        Arena a = BedwarsPlugin.getInstance().getArenaManager().arenaAt(e.getLocation());
        if (a == null) {
            BedwarsPlugin.getInstance().getLogger().info("[Debug] Explosion at " + e.getLocation() + " but no arena found! Map will be destroyed!");
            return;
        }
        
        BedwarsPlugin.getInstance().getLogger().info("[Debug] Explosion in arena " + a.getName() + ". Filtering blocks...");

        boolean fromFireball = e.getEntity() instanceof Fireball;
        Iterator<Block> it = e.blockList().iterator();
        while (it.hasNext()) {
            Block b = it.next();
            Material m = b.getType();
            
            if (!a.getPlacedBlocks().isPlaced(b.getLocation())) {
                it.remove();
                continue;
            }
            
            if (m == Material.GLASS || m == Material.STAINED_GLASS || m == Material.THIN_GLASS || m == Material.STAINED_GLASS_PANE) {
                it.remove();
                continue;
            }
            
            if (m == Material.ENDER_STONE && fromFireball) {
                it.remove();
                continue;
            }
            
            a.getPlacedBlocks().remove(b.getLocation());
        }
    }
}
