package com.customcore.plugin.commands;

import com.customcore.plugin.CustomCorePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of("edit", "reload", "toggle", "title", "setline", "removeline", "addline");

    private final CustomCorePlugin plugin;

    public ScoreboardCommand(CustomCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customcore.scoreboard.admin")) {
            sender.sendMessage("§cDazu hast du keine Berechtigung.");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDieser Befehl ist nur für Spieler.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§7Nutzung: /scoreboard <edit|reload|toggle|title|setline|removeline|addline>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "edit" -> plugin.scoreboardGUI().open(player);

            case "reload" -> {
                plugin.scoreboards().load();
                plugin.scoreboards().refreshAll();
                sender.sendMessage("§aScoreboard neu geladen.");
            }

            case "toggle" -> {
                plugin.scoreboards().toggle(player);
                sender.sendMessage("§aScoreboard-Sichtbarkeit umgeschaltet.");
            }

            case "title" -> {
                if (args.length < 2) { sender.sendMessage("§cNutzung: /scoreboard title <text>"); return true; }
                String title = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                plugin.scoreboards().setTitle(title);
                sender.sendMessage("§aTitel gesetzt.");
            }

            case "addline" -> {
                if (args.length < 2) { sender.sendMessage("§cNutzung: /scoreboard addline <text>"); return true; }
                String text = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                plugin.scoreboards().addLine(text);
                sender.sendMessage("§aZeile hinzugefügt.");
            }

            case "setline" -> {
                if (args.length < 3) { sender.sendMessage("§cNutzung: /scoreboard setline <nummer> <text>"); return true; }
                try {
                    int index = Integer.parseInt(args[1]) - 1;
                    String text = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                    plugin.scoreboards().setLine(index, text);
                    sender.sendMessage("§aZeile " + args[1] + " aktualisiert.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cUngültige Zeilennummer.");
                }
            }

            case "removeline" -> {
                if (args.length < 2) { sender.sendMessage("§cNutzung: /scoreboard removeline <nummer>"); return true; }
                try {
                    int index = Integer.parseInt(args[1]) - 1;
                    plugin.scoreboards().removeLine(index);
                    sender.sendMessage("§aZeile " + args[1] + " entfernt.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cUngültige Zeilennummer.");
                }
            }

            default -> sender.sendMessage("§7Nutzung: /scoreboard <edit|reload|toggle|title|setline|removeline|addline>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>();
            for (String s : SUBS) if (s.startsWith(args[0].toLowerCase())) out.add(s);
            return out;
        }
        return List.of();
    }
}
