package com.customcore.plugin.managers;

import com.customcore.plugin.CustomCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;

public class TablistManager implements Listener {

    private final CustomCorePlugin plugin;
    private final File file;

    private boolean enabled;
    private String header;
    private String footer;

    public TablistManager(CustomCorePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "tablist.yml");
    }

    public void load() {
        FileConfiguration cfg = file.exists() ? YamlConfiguration.loadConfiguration(file) : plugin.getConfig();
        String prefix = file.exists() ? "" : "tablist.";
        this.enabled = cfg.getBoolean(prefix + "enabled", true);
        this.header = cfg.getString(prefix + "header", "&6SERVER");
        this.footer = cfg.getString(prefix + "footer", "&7Willkommen!");
        if (!file.exists()) save();
    }

    public void save() {
        FileConfiguration cfg = new YamlConfiguration();
        cfg.set("enabled", enabled);
        cfg.set("header", header);
        cfg.set("footer", footer);
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Konnte tablist.yml nicht speichern: " + e.getMessage());
        }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; save(); refreshAll(); }

    public String getHeader() { return header; }
    public void setHeader(String header) { this.header = header; save(); refreshAll(); }

    public String getFooter() { return footer; }
    public void setFooter(String footer) { this.footer = footer; save(); refreshAll(); }

    public void refreshAll() {
        for (Player p : Bukkit.getOnlinePlayers()) render(p);
    }

    public void render(Player player) {
        if (!enabled) return;

        String h = header.replace("\\n", "\n")
                .replace("%server_online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        String f = footer.replace("\\n", "\n")
                .replace("%server_online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%customcore_rank%", plugin.ranks().getPlayerRank(player).getDisplayName());

        Component headerComp = LegacyComponentSerializer.legacyAmpersand().deserialize(h);
        Component footerComp = LegacyComponentSerializer.legacyAmpersand().deserialize(f);
        player.sendPlayerListHeaderAndFooter(headerComp, footerComp);

        applyTabPrefix(player);
    }

    /**
     * Setzt Prefix/Suffix im Spielernamen der Tabliste über das
     * Haupt-Scoreboard-Team-System (funktioniert serverweit, unabhängig
     * von PacketEvents/ProtocolLib).
     */
    private void applyTabPrefix(Player player) {
        Scoreboard mainBoard = Bukkit.getScoreboardManager().getMainScoreboard();
        var rank = plugin.ranks().getPlayerRank(player);

        String teamName = "cc_" + String.format("%03d", 999 - rank.getWeight());
        Team team = mainBoard.getTeam(teamName);
        if (team == null) {
            team = mainBoard.registerNewTeam(teamName);
        }
        if (!team.hasEntry(player.getName())) {
            // Spieler aus alten Teams entfernen
            for (Team t : mainBoard.getTeams()) {
                if (t.hasEntry(player.getName()) && !t.getName().equals(teamName)) {
                    t.removeEntry(player.getName());
                }
            }
            team.addEntry(player.getName());
        }
        team.prefix(LegacyComponentSerializer.legacyAmpersand().deserialize(rank.getPrefix() + " "));
        team.suffix(LegacyComponentSerializer.legacyAmpersand().deserialize(" " + rank.getSuffix()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> render(event.getPlayer()), 5L);
    }
}
