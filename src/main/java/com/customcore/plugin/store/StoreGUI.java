package com.customcore.plugin.store;

import com.customcore.plugin.CustomCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class StoreGUI implements Listener {

    private final CustomCorePlugin plugin;

    public StoreGUI(CustomCorePlugin plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------
    // Hauptmenü: Kategorien
    // ------------------------------------------------------------------
    public void open(Player player) {
        List<StoreCategory> categories = plugin.store().getCategories();
        int size = Math.max(9, (int) (Math.ceil(categories.size() / 9.0) * 9));
        size = Math.min(size, 54);

        MainHolder holder = new MainHolder();
        Inventory inv = Bukkit.createInventory(holder, size, Component.text("Store"));
        holder.inventory = inv;

        for (int i = 0; i < categories.size() && i < size; i++) {
            inv.setItem(i, buildCategoryIcon(categories.get(i)));
        }

        player.openInventory(inv);
    }

    // ------------------------------------------------------------------
    // Kategorie-Ansicht: die kaufbaren Items dieser Kategorie
    // ------------------------------------------------------------------
    public void openCategory(Player player, StoreCategory category) {
        int itemCount = category.getItems().size();
        int size = Math.max(18, (int) (Math.ceil((itemCount + 9) / 9.0) * 9)); // + Platz für Zurück-Reihe
        size = Math.min(size, 54);

        CategoryHolder holder = new CategoryHolder(category.getId());
        Inventory inv = Bukkit.createInventory(holder, size, colorize(category.getDisplayName()));
        holder.inventory = inv;

        for (int i = 0; i < category.getItems().size() && i < size - 9; i++) {
            inv.setItem(i, buildItemIcon(player, category.getItems().get(i)));
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(colorize("&cZurück"));
        back.setItemMeta(backMeta);
        inv.setItem(size - 5, back);

        player.openInventory(inv);
    }

    private ItemStack buildCategoryIcon(StoreCategory category) {
        ItemStack stack = new ItemStack(category.getIcon());
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(colorize(category.getDisplayName()));
        meta.lore(List.of(colorize("&7" + category.getItems().size() + " Angebote"), colorize("&e&lKlicken zum Öffnen")));
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack buildItemIcon(Player player, StoreItem item) {
        ItemStack stack = new ItemStack(item.getIcon());
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(colorize(item.getDisplayName()));

        long balance = plugin.economy().getBalance(player);
        List<Component> lore = new ArrayList<>();

        if (!item.getDescription().isBlank()) {
            lore.add(colorize(item.getDescription()));
        }

        // Vorschau der Crate-Belohnungen, falls dieses Item eine Crate verkauft
        if (item.getAction() == StoreItem.Action.CRATE_KEY) {
            var crateType = plugin.crates().getCrateType(item.getCrateId());
            if (crateType != null) {
                lore.addAll(plugin.crates().keys().previewLines(crateType));
            }

            lore.add(colorize(""));
            long price1 = item.getPrice();
            long price3 = plugin.store().bundlePrice(item, 3);
            long price9 = plugin.store().bundlePrice(item, 9);
            int discount3 = (int) Math.round(plugin.store().getBundleDiscount3() * 100);
            int discount9 = (int) Math.round(plugin.store().getBundleDiscount9() * 100);

            lore.add(colorize((balance >= price1 ? "&a" : "&c") + "Linksklick: &f1x &7- &6" + plugin.economy().format(price1)));
            lore.add(colorize((balance >= price3 ? "&a" : "&c") + "Shift+Klick: &f3x &7- &6" + plugin.economy().format(price3) + " &8(-" + discount3 + "%)"));
            lore.add(colorize((balance >= price9 ? "&a" : "&c") + "Rechtsklick: &f9x &7- &6" + plugin.economy().format(price9) + " &8(-" + discount9 + "%)"));
        } else {
            lore.add(colorize(""));
            lore.add(colorize("&7Preis: &6" + plugin.economy().format(item.getPrice())));
            lore.add(colorize(balance >= item.getPrice() ? "&a&lKlicken zum Kaufen" : "&c&lNicht genug Credits"));
        }

        meta.lore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getInventory().getHolder() instanceof MainHolder) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            List<StoreCategory> categories = plugin.store().getCategories();
            if (slot < 0 || slot >= categories.size()) return;
            openCategory(player, categories.get(slot));
            return;
        }

        if (event.getInventory().getHolder() instanceof CategoryHolder holder) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            int size = event.getInventory().getSize();

            if (slot == size - 5) { // Zurück-Button
                open(player);
                return;
            }

            StoreCategory category = plugin.store().getCategory(holder.categoryId);
            if (category == null || slot < 0 || slot >= category.getItems().size()) return;

            StoreItem item = category.getItems().get(slot);
            int multiplier = 1;
            if (item.getAction() == StoreItem.Action.CRATE_KEY) {
                if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                    multiplier = 3;
                } else if (event.getClick() == ClickType.RIGHT) {
                    multiplier = 9;
                }
            }

            String result = plugin.store().purchase(player, item, multiplier);
            player.sendMessage(result);

            openCategory(player, category); // aktualisieren, damit neuer Kontostand sichtbar ist
        }
    }

    private Component colorize(String legacy) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(legacy);
    }

    private static class MainHolder implements InventoryHolder {
        private Inventory inventory;
        @Override
        public Inventory getInventory() { return inventory; }
    }

    private static class CategoryHolder implements InventoryHolder {
        private final String categoryId;
        private Inventory inventory;
        private CategoryHolder(String categoryId) { this.categoryId = categoryId; }
        @Override
        public Inventory getInventory() { return inventory; }
    }
}
