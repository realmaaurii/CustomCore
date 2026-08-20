package com.customcore.plugin.store;

import com.customcore.plugin.CustomCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

    public void open(Player player) {
        List<StoreItem> items = plugin.store().getItems();
        int size = Math.max(9, (int) (Math.ceil(items.size() / 9.0) * 9));
        size = Math.min(size, 54);

        Holder holder = new Holder();
        Inventory inv = Bukkit.createInventory(holder, size, Component.text("Store"));
        holder.inventory = inv;

        for (int i = 0; i < items.size() && i < size; i++) {
            inv.setItem(i, buildIcon(player, items.get(i)));
        }

        player.openInventory(inv);
    }

    private ItemStack buildIcon(Player player, StoreItem item) {
        ItemStack stack = new ItemStack(item.getIcon());
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(colorize(item.getDisplayName()));

        long balance = plugin.economy().getBalance(player);
        boolean canAfford = balance >= item.getPrice();

        List<Component> lore = new ArrayList<>();
        if (!item.getDescription().isBlank()) {
            lore.add(colorize(item.getDescription()));
            lore.add(colorize(""));
        }
        lore.add(colorize("&7Preis: &6" + plugin.economy().format(item.getPrice())));
        lore.add(colorize(canAfford ? "&a&lKlicken zum Kaufen" : "&c&lNicht genug Credits"));
        meta.lore(lore);

        stack.setItemMeta(meta);
        return stack;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Holder)) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        List<StoreItem> items = plugin.store().getItems();
        if (slot < 0 || slot >= items.size()) return;

        Player player = (Player) event.getWhoClicked();
        StoreItem item = items.get(slot);

        String result = plugin.store().purchase(player, item);
        player.sendMessage(result);

        // GUI aktualisieren, damit der neue Kontostand sofort sichtbar ist
        open(player);
    }

    private Component colorize(String legacy) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(legacy);
    }

    private static class Holder implements InventoryHolder {
        private Inventory inventory;
        @Override
        public Inventory getInventory() { return inventory; }
    }
}
