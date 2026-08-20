package com.customcore.plugin.store;

import com.customcore.plugin.CustomCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StoreManager {

    private final CustomCorePlugin plugin;
    private final File file;
    private final List<StoreCategory> categories = new ArrayList<>();

    private double bundleDiscount3 = 0.10; // 10% Rabatt beim 3er-Bundle
    private double bundleDiscount9 = 0.20; // 20% Rabatt beim 9er-Bundle

    public StoreManager(CustomCorePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "store.yml");
    }

    public void load() {
        if (!file.exists()) createDefaultFile();

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        categories.clear();

        bundleDiscount3 = cfg.getDouble("settings.bundle-discount-3", 0.10);
        bundleDiscount9 = cfg.getDouble("settings.bundle-discount-9", 0.20);

        ConfigurationSection categoriesSection = cfg.getConfigurationSection("categories");
        if (categoriesSection == null) return;

        for (String catId : categoriesSection.getKeys(false)) {
            ConfigurationSection catSection = categoriesSection.getConfigurationSection(catId);
            if (catSection == null) continue;

            Material icon = Material.matchMaterial(catSection.getString("icon", "CHEST"));
            if (icon == null) icon = Material.CHEST;
            String catDisplay = catSection.getString("display-name", catId);

            StoreCategory category = new StoreCategory(catId, catDisplay, icon);

            List<Map<?, ?>> entries = catSection.getMapList("items");
            for (Map<?, ?> e : entries) {
                category.addItem(parseItem(e));
            }

            categories.add(category);
        }
    }

    private StoreItem parseItem(Map<?, ?> e) {
        String id = String.valueOf(e.get("id"));

        Object iconObj = e.get("icon");
        Material icon = Material.matchMaterial(iconObj != null ? String.valueOf(iconObj) : "STONE");
        if (icon == null) icon = Material.STONE;

        Object displayObj = e.get("display-name");
        String display = displayObj != null ? String.valueOf(displayObj) : id;

        String description = e.get("description") != null ? String.valueOf(e.get("description")) : "";
        long price = e.get("price") instanceof Number n ? n.longValue() : 0;

        Object actionObj = e.get("action");
        String actionStr = actionObj != null ? String.valueOf(actionObj) : "COMMAND";
        StoreItem.Action action;
        try {
            action = StoreItem.Action.valueOf(actionStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
            action = StoreItem.Action.COMMAND;
        }

        Material itemMat = null;
        int itemAmount = 1;
        if (e.get("item-material") != null) {
            itemMat = Material.matchMaterial(String.valueOf(e.get("item-material")));
            itemAmount = e.get("item-amount") instanceof Number n ? n.intValue() : 1;
        }
        String rankId = e.get("rank-id") != null ? String.valueOf(e.get("rank-id")) : null;
        String crateId = e.get("crate-id") != null ? String.valueOf(e.get("crate-id")) : null;
        int crateAmount = e.get("crate-amount") instanceof Number n ? n.intValue() : 1;
        String command = e.get("command") != null ? String.valueOf(e.get("command")) : null;

        return new StoreItem(id, icon, display, description, price, action,
                itemMat, itemAmount, rankId, crateId, crateAmount, command);
    }

    private void createDefaultFile() {
        FileConfiguration cfg = new YamlConfiguration();

        cfg.set("settings.bundle-discount-3", 0.10);
        cfg.set("settings.bundle-discount-9", 0.20);

        // Kategorie: Crates
        cfg.set("categories.crates.display-name", "&aCrates");
        cfg.set("categories.crates.icon", "ENDER_CHEST");
        List<Map<String, Object>> crateItems = new ArrayList<>();
        crateItems.add(itemMap("crate_common", "ENDER_CHEST", "&aCommon Crate",
                "&7Öffnet eine Common Crate", 500, "CRATE_KEY", null, 0, null, "common", 1, null));
        cfg.set("categories.crates.items", crateItems);

        // Kategorie: Ränge
        cfg.set("categories.ranks.display-name", "&bRänge");
        cfg.set("categories.ranks.icon", "DIAMOND");
        List<Map<String, Object>> rankItems = new ArrayList<>();
        rankItems.add(itemMap("rank_vip", "DIAMOND", "&b&lVIP Rang",
                "&7Schaltet den VIP-Rang frei", 5000, "RANK", null, 0, "vip", null, 0, null));
        cfg.set("categories.ranks.items", rankItems);

        // Kategorie: Items
        cfg.set("categories.items.display-name", "&eItems");
        cfg.set("categories.items.icon", "GOLDEN_APPLE");
        List<Map<String, Object>> plainItems = new ArrayList<>();
        plainItems.add(itemMap("apple_pack", "GOLDEN_APPLE", "&6Goldene Äpfel (x8)",
                "&78 goldene Äpfel für dein Inventar", 300, "ITEM", "GOLDEN_APPLE", 8, null, null, 0, null));
        cfg.set("categories.items.items", plainItems);

        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            cfg.save(file);
        } catch (IOException ex) {
            plugin.getLogger().warning("Konnte store.yml nicht erstellen: " + ex.getMessage());
        }
    }

    private Map<String, Object> itemMap(String id, String icon, String display, String description, long price,
                                         String action, String itemMaterial, int itemAmount, String rankId,
                                         String crateId, int crateAmount, String command) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("icon", icon);
        m.put("display-name", display);
        m.put("description", description);
        m.put("price", price);
        m.put("action", action);
        if (itemMaterial != null) { m.put("item-material", itemMaterial); m.put("item-amount", itemAmount); }
        if (rankId != null) m.put("rank-id", rankId);
        if (crateId != null) { m.put("crate-id", crateId); m.put("crate-amount", crateAmount); }
        if (command != null) m.put("command", command);
        return m;
    }

    public List<StoreCategory> getCategories() { return categories; }

    public StoreCategory getCategory(String id) {
        return categories.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    public double getBundleDiscount3() { return bundleDiscount3; }
    public double getBundleDiscount9() { return bundleDiscount9; }

    /** Preis für ein Bundle einer bestimmten Größe (1, 3 oder 9) unter Anwendung des Rabatts. */
    public long bundlePrice(StoreItem item, int multiplier) {
        double discount = switch (multiplier) {
            case 3 -> bundleDiscount3;
            case 9 -> bundleDiscount9;
            default -> 0.0;
        };
        return Math.round(item.getPrice() * multiplier * (1.0 - discount));
    }

    /** Versucht, den Kauf abzuwickeln (multiplier = 1, 3 oder 9 für Crate-Bundles). Gibt eine Nachricht zurück. */
    public String purchase(Player player, StoreItem item, int multiplier) {
        long price = item.getAction() == StoreItem.Action.CRATE_KEY ? bundlePrice(item, multiplier) : item.getPrice();

        if (!plugin.economy().withdraw(player, price)) {
            return "§cDu hast nicht genug Credits (" + plugin.economy().format(price) + " benötigt).";
        }

        switch (item.getAction()) {
            case ITEM -> {
                var stack = new org.bukkit.inventory.ItemStack(item.getItemMaterial(), Math.max(1, item.getItemAmount()));
                var leftover = player.getInventory().addItem(stack);
                leftover.values().forEach(over -> player.getWorld().dropItemNaturally(player.getLocation(), over));
            }
            case RANK -> {
                var rank = plugin.ranks().getRank(item.getRankId());
                if (rank != null) {
                    plugin.ranks().setPlayerRank(player, item.getRankId());
                    plugin.tablist().render(player);
                    plugin.scoreboards().render(player);
                }
            }
            case CRATE_KEY -> {
                var crateType = plugin.crates().getCrateType(item.getCrateId());
                if (crateType != null) {
                    int amount = Math.max(1, item.getCrateAmount()) * multiplier;
                    plugin.crates().giveKey(player, crateType, amount);
                }
            }
            case COMMAND -> {
                String cmd = item.getCommand().replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }

        String amountLabel = (item.getAction() == StoreItem.Action.CRATE_KEY && multiplier > 1) ? multiplier + "x " : "";
        return "§aDu hast §f" + amountLabel + colorize(item.getDisplayName()) + " §afür " + plugin.economy().format(price) + " gekauft!";
    }

    private String colorize(String s) {
        return s.replace('&', '§');
    }
}
