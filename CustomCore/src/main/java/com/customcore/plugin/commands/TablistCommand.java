package com.customcore.plugin.commands;

import com.customcore.plugin.CustomCorePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class TablistCommand implements CommandExecutor, TabCompleter {

    private final CustomCorePlugin plugin;

    public TablistCommand(CustomCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customcore.tablist.admin")) {
            sender.sendMessage("§cDazu hast du keine Berechtigung.");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDieser Befehl ist nur für Spieler.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§7Nutzung: /tablist <edit|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "edit" -> {
                player.sendMessage("§6--- Tablist-Editor ---");
                player.sendMessage("§eGib zuerst den neuen Header ein (mit \\n für Zeilenumbruch, 'skip' zum Überspringen):");
                plugin.chatInput().awaitInput(player, headerInput -> {
                    if (!headerInput.equalsIgnoreCase("skip")) {
                        plugin.tablist().setHeader(headerInput);
                    }
                    player.sendMessage("§eJetzt den neuen Footer eingeben ('skip' zum Überspringen):");
                    plugin.chatInput().awaitInput(player, footerInput -> {
                        if (!footerInput.equalsIgnoreCase("skip")) {
                            plugin.tablist().setFooter(footerInput);
                        }
                        player.sendMessage("§aTablist aktualisiert.");
                    });
                });
            }

            case "reload" -> {
                plugin.tablist().load();
                plugin.tablist().refreshAll();
                sender.sendMessage("§aTablist neu geladen.");
            }

            default -> sender.sendMessage("§7Nutzung: /tablist <edit|reload>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("edit", "reload");
        return List.of();
    }
}
