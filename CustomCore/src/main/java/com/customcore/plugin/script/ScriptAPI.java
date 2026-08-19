package com.customcore.plugin.script;

import com.customcore.plugin.CustomCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.graalvm.polyglot.HostAccess;

/**
 * Dies ist die einzige Brücke, die JS-Skripte zum Java-Code haben.
 * Jede Methode, die ein Skript aufrufen darf, muss hier explizit mit
 * @HostAccess.Export markiert sein - alles andere bleibt unsichtbar.
 * So verhinderst du, dass ein Skript beliebigen Server-Code ausführt.
 */
public class ScriptAPI {

    private final CustomCorePlugin plugin;

    public ScriptAPI(CustomCorePlugin plugin) {
        this.plugin = plugin;
    }

    @HostAccess.Export
    public void log(String message) {
        plugin.getLogger().info("[Script] " + message);
    }

    @HostAccess.Export
    public void broadcast(String legacyText) {
        Bukkit.broadcastMessage(legacyText.replace('&', '\u00A7'));
    }

    @HostAccess.Export
    public void setScoreboardLine(int index, String text) {
        plugin.scoreboards().setLine(index, text);
    }

    @HostAccess.Export
    public void setScoreboardTitle(String title) {
        plugin.scoreboards().setTitle(title);
    }

    @HostAccess.Export
    public void setTablistHeader(String header) {
        plugin.tablist().setHeader(header);
    }

    @HostAccess.Export
    public void setTablistFooter(String footer) {
        plugin.tablist().setFooter(footer);
    }

    @HostAccess.Export
    public String getPlayerRank(String playerName) {
        Player p = Bukkit.getPlayer(playerName);
        if (p == null) return null;
        return plugin.ranks().getPlayerRank(p).getId();
    }

    @HostAccess.Export
    public void setPlayerRank(String playerName, String rankId) {
        Player p = Bukkit.getPlayer(playerName);
        if (p != null) plugin.ranks().setPlayerRank(p, rankId);
    }

    @HostAccess.Export
    public void runOnMainThread(Runnable r) {
        Bukkit.getScheduler().runTask(plugin, r);
    }
}
