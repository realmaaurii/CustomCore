package com.customcore.plugin.crates;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Eine einzelne mögliche Belohnung innerhalb eines Crate-Pools.
 * type=ITEM: der Spieler bekommt das Item direkt ins Inventar (oder es droppt,
 * falls das Inventar voll ist).
 * type=CREDITS: der Spieler bekommt eine Anzahl Credits gutgeschrieben.
 * type=COMMAND: der angegebene Command wird als Konsole ausgeführt, %player%
 * wird durch den Spielernamen ersetzt (z.B. für andere Plugins, Ränge usw.).
 */
public class CrateReward {

    public enum Type { ITEM, CREDITS, COMMAND }

    private final String id;
    private final Type type;
    private final String displayName;
    private final int weight; // höher = wahrscheinlicher
    private final String category; // z.B. "&c&lLEGENDARY ITEMS" - gruppiert die Anzeige im Key-Tooltip

    // Für ITEM
    private final Material material;
    private final int amount;

    // Für CREDITS
    private final long credits;

    // Für COMMAND
    private final String command;

    public CrateReward(String id, Type type, String displayName, int weight, String category,
                        Material material, int amount, long credits, String command) {
        this.id = id;
        this.type = type;
        this.displayName = displayName;
        this.weight = weight;
        this.category = category;
        this.material = material;
        this.amount = amount;
        this.credits = credits;
        this.command = command;
    }

    public static CrateReward item(String id, String displayName, int weight, String category, Material material, int amount) {
        return new CrateReward(id, Type.ITEM, displayName, weight, category, material, amount, 0, null);
    }

    public static CrateReward credits(String id, String displayName, int weight, String category, long credits) {
        return new CrateReward(id, Type.CREDITS, displayName, weight, category, null, 0, credits, null);
    }

    public static CrateReward command(String id, String displayName, int weight, String category, String command) {
        return new CrateReward(id, Type.COMMAND, displayName, weight, category, null, 0, 0, command);
    }

    public String getId() { return id; }
    public Type getType() { return type; }
    public String getDisplayName() { return displayName; }
    public int getWeight() { return weight; }
    public String getCategory() { return category; }
    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public long getCredits() { return credits; }
    public String getCommand() { return command; }

    public ItemStack toItemStack() {
        if (type != Type.ITEM || material == null) return null;
        return new ItemStack(material, Math.max(1, amount));
    }
}
