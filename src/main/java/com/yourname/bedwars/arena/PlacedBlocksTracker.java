package com.yourname.bedwars.arena;

import org.bukkit.Location;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PlacedBlocksTracker {
    private final Set<Long> placed = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public void add(Location l) { placed.add(encode(l)); }
    public boolean isPlaced(Location l) { return placed.contains(encode(l)); }
    public void remove(Location l) { placed.remove(encode(l)); }
    public void clear() { placed.clear(); }
    private static long encode(Location l) {
        long x = (long) l.getBlockX() & 0x3FFFFFFL;
        long z = (long) l.getBlockZ() & 0x3FFFFFFL;
        long y = (long) (l.getBlockY() & 0xFF);
        return (x << 38) | (z << 8) | y;
    }
}
