package com.customcore.plugin.crates;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Eine laufende "Wähle X von Y"-Kisten-Session für einen Spieler.
 * Merkt sich, welche der platzierten Kisten schon angeklickt wurden,
 * und wie viele Klicks noch erlaubt sind.
 */
public class CrateSession {

    private final UUID owner;
    private final CrateType crateType;
    private final List<Location> chestLocations; // alle 8 Positionen
    private final Set<Location> clicked = new HashSet<>();
    private final int maxPicks; // z.B. 6
    private BukkitTask timeoutTask;

    public CrateSession(Player owner, CrateType crateType, List<Location> chestLocations, int maxPicks) {
        this.owner = owner.getUniqueId();
        this.crateType = crateType;
        this.chestLocations = chestLocations;
        this.maxPicks = maxPicks;
    }

    public UUID getOwner() { return owner; }
    public CrateType getCrateType() { return crateType; }
    public List<Location> getChestLocations() { return chestLocations; }

    public boolean isFinished() {
        return clicked.size() >= maxPicks;
    }

    public boolean isAlreadyClicked(Location loc) {
        return clicked.contains(normalize(loc));
    }

    public void markClicked(Location loc) {
        clicked.add(normalize(loc));
    }

    public int getRemainingPicks() {
        return maxPicks - clicked.size();
    }

    public void setTimeoutTask(BukkitTask task) {
        this.timeoutTask = task;
    }

    public void cancelTimeout() {
        if (timeoutTask != null) timeoutTask.cancel();
    }

    // Blockkoordinaten vergleichen statt exakter double-Werte
    private Location normalize(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
