package com.yourname.bedwars.ui;

import com.yourname.bedwars.BedwarsPlugin;
import com.yourname.bedwars.arena.Arena;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class TeamSelector implements Listener {
    @EventHandler
    public void onCompassClick(PlayerInteractEvent e) {
        if (e.getItem() == null || e.getItem().getType() != Material.COMPASS) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        // Using \u00A7 for the color code to prevent UTF-8 encoding errors
        p.sendMessage("\u00A7eOpening team selector...");
    }
}