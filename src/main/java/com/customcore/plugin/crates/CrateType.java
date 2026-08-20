package com.customcore.plugin.crates;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CrateType {

    private final String id;
    private String displayName;
    private Material keyMaterial;
    private final List<CrateReward> rewards = new ArrayList<>();
    private final Random random = new Random();

    public CrateType(String id, String displayName, Material keyMaterial) {
        this.id = id;
        this.displayName = displayName;
        this.keyMaterial = keyMaterial;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Material getKeyMaterial() { return keyMaterial; }
    public void setKeyMaterial(Material keyMaterial) { this.keyMaterial = keyMaterial; }
    public List<CrateReward> getRewards() { return rewards; }

    public void addReward(CrateReward reward) {
        rewards.add(reward);
    }

    /**
     * Wählt gewichtet-zufällig eine Belohnung aus dem Pool. Wird für jede
     * einzeln angeklickte Kiste neu aufgerufen - Belohnungen können sich
     * also wiederholen, das ist bei Crate-Systemen normal.
     */
    public CrateReward rollReward() {
        if (rewards.isEmpty()) return null;
        int totalWeight = rewards.stream().mapToInt(CrateReward::getWeight).sum();
        if (totalWeight <= 0) return rewards.get(random.nextInt(rewards.size()));

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (CrateReward reward : rewards) {
            cumulative += reward.getWeight();
            if (roll < cumulative) return reward;
        }
        return rewards.get(rewards.size() - 1);
    }

    /** Anzeige-Prozentchance einer einzelnen Belohnung relativ zum gesamten Pool. */
    public double getPercentChance(CrateReward reward) {
        int totalWeight = rewards.stream().mapToInt(CrateReward::getWeight).sum();
        if (totalWeight <= 0) return 0;
        return (reward.getWeight() * 100.0) / totalWeight;
    }
}
