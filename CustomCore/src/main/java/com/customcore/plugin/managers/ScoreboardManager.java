package com.customcore.plugin.managers;

import com.customcore.plugin.CustomCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ScoreboardManager implements Listener {

    private final CustomCorePlugin plugin;
    private final File file;

    private boolean enabled;
    private String title;
    private final List<String> lines = new ArrayList<>();

    // Merkt sich pro Spieler, ob das Scoreboard manuell ausgeblendet wurde
    private final Map<UUID, Boolean> hidden = new ConcurrentHashMap<>();

    public ScoreboardManager(CustomCorePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "scoreboard.yml");
    }

    public void load() {
        FileConfiguration cfg;
        if (file.exists()) {
            cfg = YamlConfiguration.loadConfiguration(file);
        } else {
            cfg = plugin.getConfig();
        }

        String prefix = file.exists() ? "" : "scoreboard.";
        this.enabled = cfg.getBoolean(prefix + "enabled", true);
        this.title = cfg.getString(prefix + "title", "&6&lSERVER");
        this.lines.clear();
        List<String> loaded = cfg.getStringList(prefix + "lines");
        this.lines.addAll(loaded);

        if (!file.exists()) save();
    }

    public void save() {
        FileConfiguration cfg = new YamlConfiguration();
        cfg.set("enabled", enabled);
        cfg.set("title", title);
        cfg.set("lines", lines);
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Konnte scoreboard.yml nicht speichern: " + e.getMessage());
        }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; save(); }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; save(); refreshAll(); }

    public List<String> getLines() { return lines; }

    public void setLine(int index, String text) {
        while (lines.size() <= index) lines.add("");
        lines.set(index, text);
        save();
        refreshAll();
    }

    public void removeLine(int index) {
        if (index >= 0 && index < lines.size()) {
            lines.remove(index);
            save();
            refreshAll();
        }
    }

    public void addLine(String text) {
        lines.add(text);
        save();
        refreshAll();
    }

    public void toggle(Player player) {
        UUID id = player.getUniqueId();
        boolean nowHidden = !hidden.getOrDefault(id, false);
        hidden.put(id, nowHidden);
        if (nowHidden) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        } else {
            render(player);
        }
    }

    public void refreshAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            render(p);
        }
    }

    /**
     * Baut das Scoreboard für einen einzelnen Spieler neu auf.
     * Platzhalter wie %player_name% werden hier ersetzt; für PlaceholderAPI-
     * Support kannst du hier zusätzlich PlaceholderAPI#setPlaceholders aufrufen.
     */
    public void render(Player player) {
        if (!enabled || hidden.getOrDefault(player.getUniqueId(), false)) return;

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("customcore", "dummy",
                LegacyComponentSerializer.legacyAmpersand().deserialize(title));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int score = lines.size();
        for (String rawLine : lines) {
            String parsed = parsePlaceholders(rawLine, player);
            // Teams nutzen, um doppelte Zeilen/Farbcode-Längenlimits zu umgehen
            Team team = board.registerNewTeam("line" + score);
            team.addEntry(ChatColor.values()[score % ChatColor.values().length].toString() + ChatColor.RESET);
            Component comp = LegacyComponentSerializer.legacyAmpersand().deserialize(parsed);
            team.prefix(comp);
            objective.getScore(team.getEntries().iterator().next()).setScore(score);
            score--;
        }

        player.setScoreboard(board);
    }

    private String parsePlaceholders(String line, Player player) {
        String result = line
                .replace("%player_name%", player.getName())
                .replace("%server_online%", String.valueOf(Bukkit.getOnlinePlayers().size()));

        if (plugin.ranks() != null) {
            result = result.replace("%customcore_rank%", plugin.ranks().getPlayerRank(player).getDisplayName());
        }
        return result;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> render(event.getPlayer()), 5L);
    }
}
