package com.customcore.plugin.managers;

import com.customcore.plugin.CustomCorePlugin;
import com.customcore.plugin.data.Rank;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatManager implements Listener {

    private final CustomCorePlugin plugin;
    // Individuell vom Spieler gewählte Chatfarbe, falls in config erlaubt
    private final Map<UUID, String> customColors = new HashMap<>();

    public ChatManager(CustomCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void setPlayerColor(Player player, String colorCode) {
        customColors.put(player.getUniqueId(), colorCode);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Rank rank = plugin.ranks().getPlayerRank(player);
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        String colorCode = customColors.getOrDefault(player.getUniqueId(), rank.getChatColor());

        String format = plugin.getConfig().getString("chat.format",
                "%customcore_prefix% &f%player_name% &8» &f%message%");

        String result = format
                .replace("%customcore_prefix%", rank.getPrefix())
                .replace("%player_name%", player.getName())
                .replace("%message%", colorCode + message);

        Component finalComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(result);
        event.renderer((source, sourceDisplayName, msg, viewer) -> finalComponent);
    }
}
