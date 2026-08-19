package com.customcore.plugin.managers;

import com.customcore.plugin.CustomCorePlugin;
import com.customcore.plugin.data.Rank;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RankManager {

    private final CustomCorePlugin plugin;
    private final File file;
    private final Map<String, Rank> ranks = new LinkedHashMap<>();
    private final Map<UUID, String> playerRanks = new HashMap<>();

    public RankManager(CustomCorePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ranks.yml");
    }

    public void load() {
        if (!file.exists()) {
            // Standard-Rang anlegen, falls noch keine Datei existiert
            ranks.put("default", new Rank("default", "Spieler", "&7[Spieler]", "", "&f", 0));
            save();
            return;
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ranks.clear();

        ConfigurationSection ranksSection = cfg.getConfigurationSection("ranks");
        if (ranksSection != null) {
            for (String id : ranksSection.getKeys(false)) {
                ConfigurationSection s = ranksSection.getConfigurationSection(id);
                if (s == null) continue;
                Rank rank = new Rank(
                        id,
                        s.getString("displayName", id),
                        s.getString("prefix", ""),
                        s.getString("suffix", ""),
                        s.getString("chatColor", "&f"),
                        s.getInt("weight", 0)
                );
                ranks.put(id, rank);
            }
        }

        ConfigurationSection playersSection = cfg.getConfigurationSection("players");
        if (playersSection != null) {
            for (String uuidStr : playersSection.getKeys(false)) {
                try {
                    playerRanks.put(UUID.fromString(uuidStr), playersSection.getString(uuidStr));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        if (ranks.isEmpty()) {
            ranks.put("default", new Rank("default", "Spieler", "&7[Spieler]", "", "&f", 0));
        }
    }

    public void save() {
        FileConfiguration cfg = new YamlConfiguration();
        for (Rank r : ranks.values()) {
            String path = "ranks." + r.getId();
            cfg.set(path + ".displayName", r.getDisplayName());
            cfg.set(path + ".prefix", r.getPrefix());
            cfg.set(path + ".suffix", r.getSuffix());
            cfg.set(path + ".chatColor", r.getChatColor());
            cfg.set(path + ".weight", r.getWeight());
        }
        for (Map.Entry<UUID, String> entry : playerRanks.entrySet()) {
            cfg.set("players." + entry.getKey(), entry.getValue());
        }
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Konnte ranks.yml nicht speichern: " + e.getMessage());
        }
    }

    public Rank createRank(String id, String displayName) {
        Rank rank = new Rank(id, displayName, "&7[" + displayName + "]", "", "&f", 0);
        ranks.put(id, rank);
        save();
        return rank;
    }

    public boolean deleteRank(String id) {
        if (id.equals("default")) return false; // Default-Rang darf nicht gelöscht werden
        boolean removed = ranks.remove(id) != null;
        if (removed) save();
        return removed;
    }

    public Rank getRank(String id) {
        return ranks.get(id);
    }

    public Collection<Rank> getAllRanks() {
        return ranks.values();
    }

    public Rank getPlayerRank(Player player) {
        String rankId = playerRanks.getOrDefault(player.getUniqueId(), "default");
        return ranks.getOrDefault(rankId, ranks.get("default"));
    }

    public void setPlayerRank(Player player, String rankId) {
        if (!ranks.containsKey(rankId)) return;
        playerRanks.put(player.getUniqueId(), rankId);
        save();
    }
}
