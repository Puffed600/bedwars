package com.yourname.bedwars.combat;

import com.yourname.bedwars.BedwarsPlugin;
import com.yourname.bedwars.arena.Arena;
import com.yourname.bedwars.arena.GamePhase;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class VoidListener implements Listener {
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Arena a = BedwarsPlugin.getInstance().getArenaManager().arenaForPlayer(p);
        if (a == null || a.getPhase() != GamePhase.IN_PROGRESS) return;
        if (p.getLocation().getBlockY() < 0) {
            p.setHealth(0);
            p.setFallDistance(0f);
        }
    }
}
