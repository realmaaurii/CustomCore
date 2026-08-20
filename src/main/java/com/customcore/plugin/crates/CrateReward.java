package com.customcore.plugin.crates;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Eine einzelne mögliche Belohnung innerhalb eines Crate-Pools.
 * type=ITEM: der Spieler bekommt das Item direkt ins Inventar.
 * type=CREDITS: der Spieler bekommt eine Anzahl Credits gutgeschrieben.
 * type=RANK: der Spieler bekommt einen Rang gesetzt (muss in ranks.yml existieren).
 * type=CRATE_KEY: der Spieler bekommt einen weiteren Crate-Key einer beliebigen Crate.
 * type=COMMAND: der angegebene Command wird als Konsole ausgeführt, %player%
 * wird durch den Spielernamen ersetzt.
 *
 * "Gute" Belohnungen (CREDITS, RANK, CRATE_KEY) lösen beim Ziehen einen
 * auffälligeren Sound/Effekt aus als normale ITEM-Belohnungen.
 */
public class CrateReward {

    public enum Type { ITEM, CREDITS, RANK, CRATE_KEY, COMMAND }

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

    // Für RANK
    private final String rankId;

    // Für CRATE_KEY
    private final String crateId;
    private final int crateAmount;

    // Für COMMAND
    private final String command;

    private CrateReward(String id, Type type, String displayName, int weight, String category,
                         Material material, int amount, long credits,
                         String rankId, String crateId, int crateAmount, String command) {
        this.id = id;
        this.type = type;
        this.displayName = displayName;
        this.weight = weight;
        this.category = category;
        this.material = material;
        this.amount = amount;
        this.credits = credits;
        this.rankId = rankId;
        this.crateId = crateId;
        this.crateAmount = crateAmount;
        this.command = command;
    }

    public static CrateReward item(String id, String displayName, int weight, String category, Material material, int amount) {
        return new CrateReward(id, Type.ITEM, displayName, weight, category, material, amount, 0, null, null, 0, null);
    }

    public static CrateReward credits(String id, String displayName, int weight, String category, long credits) {
        return new CrateReward(id, Type.CREDITS, displayName, weight, category, null, 0, credits, null, null, 0, null);
    }

    public static CrateReward rank(String id, String displayName, int weight, String category, String rankId) {
        return new CrateReward(id, Type.RANK, displayName, weight, category, null, 0, 0, rankId, null, 0, null);
    }

    public static CrateReward crateKey(String id, String displayName, int weight, String category, String crateId, int crateAmount) {
        return new CrateReward(id, Type.CRATE_KEY, displayName, weight, category, null, 0, 0, null, crateId, crateAmount, null);
    }

    public static CrateReward command(String id, String displayName, int weight, String category, String command) {
        return new CrateReward(id, Type.COMMAND, displayName, weight, category, null, 0, 0, null, null, 0, command);
    }

    public String getId() { return id; }
    public Type getType() { return type; }
    public String getDisplayName() { return displayName; }
    public int getWeight() { return weight; }
    public String getCategory() { return category; }
    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public long getCredits() { return credits; }
    public String getRankId() { return rankId; }
    public String getCrateId() { return crateId; }
    public int getCrateAmount() { return crateAmount; }
    public String getCommand() { return command; }

    /** "Gute" Belohnungen lösen einen auffälligeren Sound/Effekt beim Ziehen aus. */
    public boolean isSpecial() {
        return type == Type.CREDITS || type == Type.RANK || type == Type.CRATE_KEY;
    }

    public ItemStack toItemStack() {
        if (type != Type.ITEM || material == null) return null;
        return new ItemStack(material, Math.max(1, amount));
    }
}
