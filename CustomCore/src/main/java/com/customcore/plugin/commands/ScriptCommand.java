package com.customcore.plugin.commands;

import com.customcore.plugin.CustomCorePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class ScriptCommand implements CommandExecutor, TabCompleter {

    private final CustomCorePlugin plugin;

    public ScriptCommand(CustomCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customcore.script.admin")) {
            sender.sendMessage("§cDazu hast du keine Berechtigung.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§7Nutzung: /ccscript <reload|list|run|unload> [datei]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.scripts().reloadAll();
                sender.sendMessage("§aAlle Skripte neu geladen.");
            }
            case "list" -> {
                sender.sendMessage("§6Geladene Skripte:");
                for (String s : plugin.scripts().listLoaded()) sender.sendMessage(" §7- " + s);
            }
            case "run" -> {
                if (args.length < 2) { sender.sendMessage("§cNutzung: /ccscript run <datei.js>"); return true; }
                boolean ok = plugin.scripts().loadScript(args[1]);
                sender.sendMessage(ok ? "§aSkript ausgeführt." : "§cDatei nicht gefunden.");
            }
            case "unload" -> {
                if (args.length < 2) { sender.sendMessage("§cNutzung: /ccscript unload <datei.js>"); return true; }
                boolean ok = plugin.scripts().unloadScript(args[1]);
                sender.sendMessage(ok ? "§aSkript entladen." : "§cSkript war nicht geladen.");
            }
            default -> sender.sendMessage("§7Nutzung: /ccscript <reload|list|run|unload> [datei]");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("reload", "list", "run", "unload");
        return List.of();
    }
}
