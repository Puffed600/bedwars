package com.yourname.bedwars.fireball;

import com.yourname.bedwars.BedwarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class FireballService implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.FIREBALL) return;
        
        Player p = e.getPlayer();
        Fireball fb = p.launchProjectile(Fireball.class);
        fb.setVelocity(p.getLocation().getDirection().multiply(2.0));
        fb.setShooter(p);
        fb.setYield(2.5f);
        
        // Fix: properly remove item when it reaches 0
        int newAmount = item.getAmount() - 1;
        if (newAmount <= 0) {
            p.getInventory().setItemInHand(null);
        } else {
            item.setAmount(newAmount);
        }
        p.updateInventory();
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent e) {
        boolean isFireball = e.getEntity() instanceof Fireball;
        boolean isTNT = e.getEntity() instanceof TNTPrimed;
        
        if (!isFireball && !isTNT) return;
        
        double radius = BedwarsPlugin.getInstance().getConfig().getDouble("fireball.jump-radius", 5.0);
        double power = BedwarsPlugin.getInstance().getConfig().getDouble("fireball.jump-power", 1.6);
        
        if (isTNT) {
            power = BedwarsPlugin.getInstance().getConfig().getDouble("tnt.jump-power", 1.4);
        }
        
        for (Player p : e.getLocation().getWorld().getPlayers()) {
            if (p.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;
            if (p.getLocation().distance(e.getLocation()) > radius) continue;
            
            Vector dir = p.getLocation().toVector().subtract(e.getLocation().toVector());
            if (dir.lengthSquared() < 0.001) dir = new Vector(0, 1, 0);
            dir.normalize().multiply(power);
            dir.setY(Math.max(dir.getY(), 0.8));
            p.setVelocity(dir);
            
            p.setMetadata("no_fall", new FixedMetadataValue(BedwarsPlugin.getInstance(), true));
            Bukkit.getScheduler().runTaskLater(BedwarsPlugin.getInstance(), () -> {
                if (p.hasMetadata("no_fall")) p.removeMetadata("no_fall", BedwarsPlugin.getInstance());
            }, 100L); 
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFireballDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Fireball)) return;
        if (!(e.getEntity() instanceof Player)) return;
        
        if (e.getDamage() > 3.0) {
            e.setDamage(3.0);
        }
        
        if (e.getCause() == EntityDamageEvent.DamageCause.FIRE || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
            e.setCancelled(true);
        }
    }
}
