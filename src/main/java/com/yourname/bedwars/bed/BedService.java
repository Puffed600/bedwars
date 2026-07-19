package com.yourname.bedwars.bed;

import com.yourname.bedwars.BedwarsPlugin;
import com.yourname.bedwars.arena.Arena;
import com.yourname.bedwars.arena.Team;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.material.Bed;

public class BedService implements Listener {
    public BedService(BedwarsPlugin plugin) {}

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBedBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (b.getType() != Material.BED_BLOCK) return;
        Arena arena = BedwarsPlugin.getInstance().getArenaManager().arenaAt(b.getLocation());
        if (arena == null) return;

        Team owner = arena.teamForBed(b.getLocation());
        if (owner == null) return; 

        Player breaker = e.getPlayer();
        
        if (breaker.getGameMode() == GameMode.CREATIVE) return;

        Team breakerTeam = arena.getTeam(breaker);
        
        if (breakerTeam == owner) {
            e.setCancelled(true);
            breaker.sendMessage("\u00A7cYou cannot break your own bed!");
            return;
        }

        e.setCancelled(true);
        Block partner = getPartnerBed(b);
        b.setType(Material.AIR);
        if (partner != null) partner.setType(Material.AIR);
        arena.getPlacedBlocks().remove(b.getLocation());
        if (partner != null) arena.getPlacedBlocks().remove(partner.getLocation());
        
        owner.breakBed(breaker);
        
        for (Player p : arena.getLobby().getWorld().getPlayers()) {
            p.sendMessage("\u00A7c\u00A7l" + owner.getColoredName() + " Bed\u00A7r\u00A77 was destroyed by " + breakerTeam.getColoredName() + " " + breaker.getName() + "\u00A77!");
            p.playSound(p.getLocation(), Sound.EXPLODE, 1f, 1f);
        }
    }

    private Block getPartnerBed(Block b) {
        if (!(b.getState().getData() instanceof Bed)) return null;
        Bed bedData = (Bed) b.getState().getData();
        BlockFace partnerFace = bedData.isHeadOfBed() ? bedData.getFacing().getOppositeFace() : bedData.getFacing();
        return b.getRelative(partnerFace);
    }
}
