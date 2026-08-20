package com.customcore.plugin.crates;

import com.customcore.plugin.CustomCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CrateKeyUtil {

    private final NamespacedKey key;

    public CrateKeyUtil(CustomCorePlugin plugin) {
        this.key = new NamespacedKey(plugin, "crate_key_id");
    }

    public ItemStack createKey(CrateType crateType) {
        ItemStack item = new ItemStack(crateType.getKeyMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(colorize("&e&l" + crateType.getDisplayName().replace("&", "").toUpperCase(Locale.ROOT) + " &7Key"));
        List<Component> lore = new ArrayList<>(previewLines(crateType));
        lore.add(colorize(""));
        lore.add(colorize("&e&lRechtsklick &7auf den Boden zum Öffnen"));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, crateType.getId());
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Baut die Vorschau-Zeilen im Stil "gruppiert nach Kategorie, mit
     * Prozentchance dahinter", wie z.B. bei bekannten Crate-Plugins üblich.
     * Rewards ohne 'category' landen in einer Sammel-Gruppe am Ende.
     * Öffentlich, damit der Store dieselbe Vorschau in seinen Icons zeigen kann.
     */
    public List<Component> previewLines(CrateType crateType) {
        List<Component> lore = new ArrayList<>();
        lore.add(colorize(""));

        // Nach Kategorie gruppieren, Reihenfolge = erstes Vorkommen in der Config
        Map<String, List<CrateReward>> grouped = new LinkedHashMap<>();
        for (CrateReward reward : crateType.getRewards()) {
            String cat = (reward.getCategory() == null || reward.getCategory().isBlank())
                    ? "&7&lWEITERE BELOHNUNGEN" : reward.getCategory();
            grouped.computeIfAbsent(cat, k -> new ArrayList<>()).add(reward);
        }

        boolean first = true;
        for (Map.Entry<String, List<CrateReward>> entry : grouped.entrySet()) {
            if (!first) lore.add(colorize(""));
            first = false;

            lore.add(colorize(entry.getKey()));

            // Farbcode des Kategorie-Headers als Balkenfarbe für die Zeilen darunter übernehmen
            String barColor = extractLeadingColor(entry.getKey());

            for (CrateReward reward : entry.getValue()) {
                double percent = crateType.getPercentChance(reward);
                String percentStr = String.format(Locale.GERMANY, "%.1f", percent);
                String line = barColor + "▎ " + reward.getDisplayName() + " &8(" + percentStr + "%)";
                lore.add(colorize(line));
            }
        }

        return lore;
    }

    private String extractLeadingColor(String s) {
        int amp = s.indexOf('&');
        if (amp >= 0 && amp + 1 < s.length()) {
            return "&" + s.charAt(amp + 1);
        }
        return "&7";
    }

    private Component colorize(String legacy) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(legacy);
    }

    /** Gibt die Crate-ID zurück, falls das Item ein gültiger Key ist, sonst null. */
    public String getCrateId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }
}
