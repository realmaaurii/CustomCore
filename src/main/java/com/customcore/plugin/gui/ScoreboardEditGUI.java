package com.customcore.plugin.gui;

import com.customcore.plugin.CustomCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Klickbarer Editor für das Scoreboard: linksklick = Zeile bearbeiten,
 * shift-linksklick = Zeile löschen, das "+"-Item = neue Zeile hinzufügen,
 * das Kompass-Item = Titel ändern. Alle Texteingaben laufen über den
 * ChatInputManager, damit man Farbcodes bequem per Chat eingeben kann.
 */
public class ScoreboardEditGUI implements Listener {

    private static final String TITLE = "§6Scoreboard-Editor";

    private final CustomCorePlugin plugin;

    public ScoreboardEditGUI(CustomCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Holder holder = new Holder();
        Inventory inv = Bukkit.createInventory(holder, 27, Component.text(TITLE.replace("§6", "")));
        holder.inventory = inv;

        List<String> lines = plugin.scoreboards().getLines();
        for (int i = 0; i < lines.size() && i < 18; i++) {
            inv.setItem(i, buildLineItem(i, lines.get(i)));
        }

        inv.setItem(22, buildControlItem(Material.COMPASS, "§eTitel ändern",
                "§7Aktuell: §f" + plugin.scoreboards().getTitle(), "TITLE"));
        inv.setItem(21, buildControlItem(Material.LIME_DYE, "§a+ Neue Zeile", "§7Klicken zum Hinzufügen", "ADD"));
        inv.setItem(23, buildControlItem(Material.BARRIER, "§cSchließen", "", "CLOSE"));

        player.openInventory(inv);
    }

    private ItemStack buildLineItem(int index, String text) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("&fZeile " + (index + 1)));
        meta.lore(List.of(
                LegacyComponentSerializer.legacyAmpersand().deserialize(text),
                Component.text("Links: bearbeiten", NamedTextColor.GRAY),
                Component.text("Shift+Links: löschen", NamedTextColor.GRAY)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildControlItem(Material mat, String name, String lore, String action) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
        if (!lore.isEmpty()) {
            meta.lore(List.of(LegacyComponentSerializer.legacyAmpersand().deserialize(lore)));
        }
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Holder)) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        if (slot == 23) { // Schließen
            player.closeInventory();
            return;
        }
        if (slot == 22) { // Titel ändern
            player.closeInventory();
            player.sendMessage("§eGib den neuen Titel in den Chat ein (Farbcodes mit &):");
            plugin.chatInput().awaitInput(player, input -> {
                plugin.scoreboards().setTitle(input);
                player.sendMessage("§aTitel aktualisiert.");
                open(player);
            });
            return;
        }
        if (slot == 21) { // Neue Zeile
            player.closeInventory();
            player.sendMessage("§eGib den Text für die neue Zeile ein:");
            plugin.chatInput().awaitInput(player, input -> {
                plugin.scoreboards().addLine(input);
                player.sendMessage("§aZeile hinzugefügt.");
                open(player);
            });
            return;
        }

        if (slot < 18 && slot < plugin.scoreboards().getLines().size()) {
            if (event.isShiftClick()) {
                plugin.scoreboards().removeLine(slot);
                open(player); // GUI neu aufbauen
            } else {
                player.closeInventory();
                player.sendMessage("§eGib den neuen Text für Zeile " + (slot + 1) + " ein:");
                final int index = slot;
                plugin.chatInput().awaitInput(player, input -> {
                    plugin.scoreboards().setLine(index, input);
                    player.sendMessage("§aZeile aktualisiert.");
                    open(player);
                });
            }
        }
    }

    /** Markiert das Inventar eindeutig als Scoreboard-Editor. */
    private static class Holder implements InventoryHolder {
        private Inventory inventory;
        @Override
        public Inventory getInventory() { return inventory; }
    }
}
