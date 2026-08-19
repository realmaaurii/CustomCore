package com.customcore.plugin.util;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Erlaubt es, die nächste Chatnachricht eines Spielers abzufangen und
 * als Callback zu verarbeiten, statt sie normal in den Chat zu senden.
 * Wird von allen Edit-Wizards (Scoreboard/Rank/Tablist) genutzt.
 */
public class ChatInputManager implements Listener {

    private final Map<UUID, Consumer<String>> pending = new ConcurrentHashMap<>();
    private final JavaPlugin plugin;

    public ChatInputManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void awaitInput(Player player, Consumer<String> callback) {
        pending.put(player.getUniqueId(), callback);
    }

    public void cancel(Player player) {
        pending.remove(player.getUniqueId());
    }

    public boolean isAwaiting(Player player) {
        return pending.containsKey(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Consumer<String> callback = pending.remove(uuid);
        if (callback == null) return;

        event.setCancelled(true);
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        // Callback auf dem Main-Thread ausführen, da Chat async ist
        plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(message));
    }
}
