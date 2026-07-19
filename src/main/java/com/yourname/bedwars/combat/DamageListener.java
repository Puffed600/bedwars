package com.yourname.bedwars.combat;

import com.yourname.bedwars.BedwarsPlugin;
import com.yourname.bedwars.arena.Arena;
import com.yourname.bedwars.arena.GamePhase;
import com.yourname.bedwars.arena.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class DamageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (p.hasMetadata("no_fall")) {
                e.setCancelled(true);
                p.removeMetadata("no_fall", BedwarsPlugin.getInstance());
                return;
            }
            if (BedwarsPlugin.getInstance().getConfig().getBoolean("disable-fall-damage", true)) {
                e.setCancelled(true);
                return;
            }
        }

        Arena a = BedwarsPlugin.getInstance().getArenaManager().arenaForPlayer(p);
        if (a == null) return;

        if (a.getPhase() == GamePhase.WAITING || a.getPhase() == GamePhase.STARTING) { e.setCancelled(true); return; }
        if (a.isPaused()) { e.setCancelled(true); return; }

        if (e instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ed = (EntityDamageByEntityEvent) e;
            Entity damager = ed.getDamager();
            if (damager instanceof Player) {
                Player d = (Player) damager;
                Team t1 = a.getTeam(p), t2 = a.getTeam(d);
                if (t1 != null && t1 == t2 && !a.isFriendlyFire()) {
                    ed.setCancelled(true);
                    return;
                }
                
                // Apply Sharpness
                if (t2 != null && t2.getSharpness() > 0) {
                    ed.setDamage(ed.getDamage() + (1.0 * t2.getSharpness()));
                }
            }
        }

        // Apply Protection
        Team victimTeam = a.getTeam(p);
        if (victimTeam != null && victimTeam.getProtection() > 0) {
            double reduction = 0.04 * victimTeam.getProtection(); // 4% per level
            e.setDamage(e.getDamage() * (1.0 - reduction));
        }

        if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            p.setMetadata("no_fall", new FixedMetadataValue(BedwarsPlugin.getInstance(), true));
            Bukkit.getScheduler().runTaskLater(BedwarsPlugin.getInstance(), () -> {
                if (p.hasMetadata("no_fall")) p.removeMetadata("no_fall", BedwarsPlugin.getInstance());
            }, 100L);
        }
    }
}
