package com.customcore.plugin.listeners;

import com.customcore.plugin.CustomCorePlugin;
import com.customcore.plugin.crates.CrateType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CrateListener implements Listener {

    private final CustomCorePlugin plugin;

    public CrateListener(CustomCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Nur Hauptklick zählen (verhindert doppelte Events durch Off-Hand)
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;
        Player player = event.getPlayer();

        // Fall 1: Spieler klickt auf eine bereits platzierte Session-Kiste
        if (clicked.getType() == Material.CHEST) {
            boolean handled = plugin.crates().handleChestClick(player, clicked);
            if (handled) {
                event.setCancelled(true); // verhindert, dass die normale (leere) Kisten-GUI aufgeht
            }
            return;
        }

        // Fall 2: Spieler hält einen Crate-Key und klickt auf den Boden
        ItemStack inHand = event.getItem();
        String crateId = plugin.crates().keys().getCrateId(inHand);
        if (crateId == null) return;

        CrateType type = plugin.crates().getCrateType(crateId);
        if (type == null) return;

        event.setCancelled(true);

        boolean opened = plugin.crates().openCrate(player, clicked, type);
        if (opened && player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            inHand.setAmount(inHand.getAmount() - 1); // Key verbrauchen
        }
    }
}
