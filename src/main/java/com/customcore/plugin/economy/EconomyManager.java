package com.customcore.plugin.economy;

import com.customcore.plugin.CustomCorePlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Einfaches, eigenständiges Credits-System (keine Abhängigkeit von Vault
 * o.ä. nötig). Guthaben wird als long gespeichert (ganze Credits, keine
 * Nachkommastellen - üblich für virtuelle Server-Währungen).
 */
public class EconomyManager {

    private final CustomCorePlugin plugin;
    private final File file;
    private final Map<UUID, Long> balances = new HashMap<>();
    private static final long DEFAULT_BALANCE = 0L;

    public EconomyManager(CustomCorePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "economy.yml");
    }

    public void load() {
        balances.clear();
        if (!file.exists()) return;

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        var section = cfg.getConfigurationSection("balances");
        if (section == null) return;

        for (String uuidStr : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                long balance = section.getLong(uuidStr);
                balances.put(uuid, balance);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void save() {
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, Long> entry : balances.entrySet()) {
            cfg.set("balances." + entry.getKey(), entry.getValue());
        }
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Konnte economy.yml nicht speichern: " + e.getMessage());
        }
    }

    public long getBalance(OfflinePlayer player) {
        return balances.getOrDefault(player.getUniqueId(), DEFAULT_BALANCE);
    }

    public boolean has(OfflinePlayer player, long amount) {
        return getBalance(player) >= amount;
    }

    public void deposit(OfflinePlayer player, long amount) {
        if (amount <= 0) return;
        balances.merge(player.getUniqueId(), amount, Long::sum);
        save();
    }

    /** Gibt false zurück, wenn nicht genug Guthaben vorhanden ist (nichts wird abgezogen). */
    public boolean withdraw(OfflinePlayer player, long amount) {
        if (amount <= 0) return true;
        if (!has(player, amount)) return false;
        balances.merge(player.getUniqueId(), -amount, Long::sum);
        save();
        return true;
    }

    public void setBalance(OfflinePlayer player, long amount) {
        balances.put(player.getUniqueId(), Math.max(0, amount));
        save();
    }

    public String format(long amount) {
        return String.format("%,d", amount).replace(",", ".") + " Credits";
    }
}
