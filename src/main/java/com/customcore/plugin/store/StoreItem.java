package com.customcore.plugin.store;

import org.bukkit.Material;

public class StoreItem {

    public enum Action { ITEM, RANK, CRATE_KEY, COMMAND }

    private final String id;
    private final Material icon;
    private final String displayName;
    private final String description; // kurze Zeile, wird in der Lore gezeigt
    private final long price;
    private final Action action;

    // je nach Action genutzt:
    private final Material itemMaterial;
    private final int itemAmount;
    private final String rankId;
    private final String crateId;
    private final int crateAmount;
    private final String command;

    public StoreItem(String id, Material icon, String displayName, String description, long price, Action action,
                      Material itemMaterial, int itemAmount, String rankId, String crateId, int crateAmount, String command) {
        this.id = id;
        this.icon = icon;
        this.displayName = displayName;
        this.description = description;
        this.price = price;
        this.action = action;
        this.itemMaterial = itemMaterial;
        this.itemAmount = itemAmount;
        this.rankId = rankId;
        this.crateId = crateId;
        this.crateAmount = crateAmount;
        this.command = command;
    }

    public String getId() { return id; }
    public Material getIcon() { return icon; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public long getPrice() { return price; }
    public Action getAction() { return action; }
    public Material getItemMaterial() { return itemMaterial; }
    public int getItemAmount() { return itemAmount; }
    public String getRankId() { return rankId; }
    public String getCrateId() { return crateId; }
    public int getCrateAmount() { return crateAmount; }
    public String getCommand() { return command; }
}
