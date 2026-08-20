package com.customcore.plugin.store;

import com.customcore.plugin.CustomCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
    private final List<StoreItem> items = new ArrayList<>();

    public StoreManager(CustomCorePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "store.yml");
    }

    public void load() {
        if (!file.exists()) createDefaultFile();

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        items.clear();

        List<Map<?, ?>> entries = cfg.getMapList("items");
        for (Map<?, ?> e : entries) {
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

            items.add(new StoreItem(id, icon, display, description, price, action,
                    itemMat, itemAmount, rankId, crateId, crateAmount, command));
        }
    }

    private void createDefaultFile() {
        FileConfiguration cfg = new YamlConfiguration();
        List<Map<String, Object>> entries = new ArrayList<>();

        entries.add(entry("crate_common", "ENDER_CHEST", "&aCommon Crate Key",
                "&7Öffnet eine Common Crate", 500, "CRATE_KEY", null, 0, null, "common", 1, null));

        entries.add(entry("rank_vip", "DIAMOND", "&b&lVIP Rang",
                "&7Schaltet den VIP-Rang frei", 5000, "RANK", null, 0, "vip", null, 0, null));

        entries.add(entry("apple_pack", "GOLDEN_APPLE", "&6Goldene Äpfel (x8)",
                "&78 goldene Äpfel für dein Inventar", 300, "ITEM", "GOLDEN_APPLE", 8, null, null, 0, null));

        cfg.set("items", entries);

        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            cfg.save(file);
        } catch (IOException ex) {
            plugin.getLogger().warning("Konnte store.yml nicht erstellen: " + ex.getMessage());
        }
    }

    private Map<String, Object> entry(String id, String icon, String display, String description, long price,
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

    public List<StoreItem> getItems() { return items; }

    /** Versucht, den Kauf abzuwickeln. Gibt eine Nachricht für den Spieler zurück. */
    public String purchase(Player player, StoreItem item) {
        if (!plugin.economy().withdraw(player, item.getPrice())) {
            return "§cDu hast nicht genug Credits (" + plugin.economy().format(item.getPrice()) + " benötigt).";
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
                    plugin.crates().giveKey(player, crateType, Math.max(1, item.getCrateAmount()));
                }
            }
            case COMMAND -> {
                String cmd = item.getCommand().replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }

        return "§aDu hast §f" + colorize(item.getDisplayName()) + " §afür " + plugin.economy().format(item.getPrice()) + " gekauft!";
    }

    private String colorize(String s) {
        return s.replace('&', '§');
    }
}
