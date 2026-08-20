package com.customcore.plugin.store;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class StoreCategory {

    private final String id;
    private final String displayName;
    private final Material icon;
    private final List<StoreItem> items = new ArrayList<>();

    public StoreCategory(String id, String displayName, Material icon) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getIcon() { return icon; }
    public List<StoreItem> getItems() { return items; }
    public void addItem(StoreItem item) { items.add(item); }
}
