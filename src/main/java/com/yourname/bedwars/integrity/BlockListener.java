package com.yourname.bedwars.integrity;

import com.yourname.bedwars.BedwarsPlugin;
import com.yourname.bedwars.arena.Arena;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {
    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Arena a = BedwarsPlugin.getInstance().getArenaManager().arenaAt(e.getBlock().getLocation());
        if (a == null) return;
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        a.getPlacedBlocks().add(e.getBlock().getLocation());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Arena a = BedwarsPlugin.getInstance().getArenaManager().arenaAt(e.getBlock().getLocation());
        if (a == null) return;

        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            a.getPlacedBlocks().remove(e.getBlock().getLocation());
            return;
        }

        // Ignore beds so BedService can handle them!
        if (e.getBlock().getType() == Material.BED_BLOCK) return;

        if (a.getPlacedBlocks().isPlaced(e.getBlock().getLocation())) {
            a.getPlacedBlocks().remove(e.getBlock().getLocation());
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler public void onBurn(BlockBurnEvent e) { e.setCancelled(true); }
    @EventHandler public void onIgnite(BlockIgniteEvent e) { e.setCancelled(true); }
}
