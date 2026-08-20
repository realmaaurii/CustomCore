package com.customcore.plugin;

import com.customcore.plugin.commands.*;
import com.customcore.plugin.crates.CrateManager;
import com.customcore.plugin.economy.EconomyManager;
import com.customcore.plugin.gui.ScoreboardEditGUI;
import com.customcore.plugin.listeners.CrateListener;
import com.customcore.plugin.managers.*;
import com.customcore.plugin.script.ScriptManager;
import com.customcore.plugin.store.StoreGUI;
import com.customcore.plugin.store.StoreManager;
import com.customcore.plugin.util.ChatInputManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomCorePlugin extends JavaPlugin {

    private static CustomCorePlugin instance;

    private RankManager rankManager;
    private ScoreboardManager scoreboardManager;
    private TablistManager tablistManager;
    private ChatManager chatManager;
    private ScriptManager scriptManager;
    private ChatInputManager chatInputManager;
    private ScoreboardEditGUI scoreboardEditGUI;
    private CrateManager crateManager;
    private EconomyManager economyManager;
    private StoreManager storeManager;
    private StoreGUI storeGUI;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Manager initialisieren
        this.chatInputManager = new ChatInputManager(this);
        this.rankManager = new RankManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.tablistManager = new TablistManager(this);
        this.chatManager = new ChatManager(this);
        this.scriptManager = new ScriptManager(this);
        this.scoreboardEditGUI = new ScoreboardEditGUI(this);
        this.crateManager = new CrateManager(this);
        this.economyManager = new EconomyManager(this);
        this.storeManager = new StoreManager(this);
        this.storeGUI = new StoreGUI(this);

        rankManager.load();
        scoreboardManager.load();
        tablistManager.load();
        crateManager.load();
        economyManager.load();
        storeManager.load();
        scriptManager.setup();
        if (getConfig().getBoolean("scripts.auto-load", true)) {
            scriptManager.loadAllScripts();
        }

        // Listener
        getServer().getPluginManager().registerEvents(chatInputManager, this);
        getServer().getPluginManager().registerEvents(chatManager, this);
        getServer().getPluginManager().registerEvents(scoreboardManager, this);
        getServer().getPluginManager().registerEvents(tablistManager, this);
        getServer().getPluginManager().registerEvents(scoreboardEditGUI, this);
        getServer().getPluginManager().registerEvents(new CrateListener(this), this);
        getServer().getPluginManager().registerEvents(storeGUI, this);

        // Commands
        getCommand("scoreboard").setExecutor(new ScoreboardCommand(this));
        getCommand("scoreboard").setTabCompleter(new ScoreboardCommand(this));
        getCommand("rank").setExecutor(new RankCommand(this));
        getCommand("rank").setTabCompleter(new RankCommand(this));
        getCommand("tablist").setExecutor(new TablistCommand(this));
        getCommand("tablist").setTabCompleter(new TablistCommand(this));
        getCommand("chatcolor").setExecutor(new ChatColorCommand(this));
        getCommand("ccscript").setExecutor(new ScriptCommand(this));
        getCommand("ccscript").setTabCompleter(new ScriptCommand(this));
        getCommand("crate").setExecutor(new CrateCommand(this));
        getCommand("crate").setTabCompleter((CrateCommand) getCommand("crate").getExecutor());
        getCommand("credits").setExecutor(new CreditsCommand(this));
        getCommand("credits").setTabCompleter((CreditsCommand) getCommand("credits").getExecutor());
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("store").setExecutor(new StoreCommand(this));

        // Scoreboard automatisch alle 2 Sekunden neu aufbauen, damit sich
        // ändernde Werte wie Credits oder Spielzeit live sichtbar bleiben,
        // ohne dass jede einzelne Stelle im Code manuell ein Update auslösen muss.
        org.bukkit.Bukkit.getScheduler().runTaskTimer(this, () -> scoreboardManager.refreshAll(), 40L, 40L);

        getLogger().info("CustomCore wurde aktiviert.");
    }

    @Override
    public void onDisable() {
        if (scoreboardManager != null) scoreboardManager.save();
        if (rankManager != null) rankManager.save();
        if (economyManager != null) economyManager.save();
        if (scriptManager != null) scriptManager.shutdown();
        getLogger().info("CustomCore wurde deaktiviert.");
    }

    public static CustomCorePlugin get() {
        return instance;
    }

    public RankManager ranks() { return rankManager; }
    public ScoreboardManager scoreboards() { return scoreboardManager; }
    public TablistManager tablist() { return tablistManager; }
    public ChatManager chat() { return chatManager; }
    public ScriptManager scripts() { return scriptManager; }
    public ChatInputManager chatInput() { return chatInputManager; }
    public ScoreboardEditGUI scoreboardGUI() { return scoreboardEditGUI; }
    public CrateManager crates() { return crateManager; }
    public EconomyManager economy() { return economyManager; }
    public StoreManager store() { return storeManager; }
    public StoreGUI storeGUI() { return storeGUI; }
}
