package com.customcore.plugin.commands;

import com.customcore.plugin.CustomCorePlugin;
import com.customcore.plugin.data.Rank;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RankCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of("create", "delete", "edit", "set", "list");

    private final CustomCorePlugin plugin;

    public RankCommand(CustomCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customcore.rank.admin")) {
            sender.sendMessage("§cDazu hast du keine Berechtigung.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§7Nutzung: /rank <create|delete|edit|set|list>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                sender.sendMessage("§6Ränge:");
                for (Rank r : plugin.ranks().getAllRanks()) {
                    sender.sendMessage(" §7- §f" + r.getId() + " §7(" + r.getDisplayName() + ") Gewicht: " + r.getWeight());
                }
            }

            case "create" -> {
                if (args.length < 3) { sender.sendMessage("§cNutzung: /rank create <id> <anzeigename>"); return true; }
                String id = args[1];
                String displayName = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                if (plugin.ranks().getRank(id) != null) { sender.sendMessage("§cDieser Rang existiert bereits."); return true; }
                plugin.ranks().createRank(id, displayName);
                sender.sendMessage("§aRang '" + id + "' erstellt.");
            }

            case "delete" -> {
                if (args.length < 2) { sender.sendMessage("§cNutzung: /rank delete <id>"); return true; }
                if (plugin.ranks().deleteRank(args[1])) {
                    sender.sendMessage("§aRang gelöscht.");
                } else {
                    sender.sendMessage("§cRang konnte nicht gelöscht werden (existiert nicht oder ist 'default').");
                }
            }

            case "set" -> {
                if (args.length < 3) { sender.sendMessage("§cNutzung: /rank set <spieler> <rangid>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage("§cSpieler nicht online."); return true; }
                if (plugin.ranks().getRank(args[2]) == null) { sender.sendMessage("§cRang existiert nicht."); return true; }
                plugin.ranks().setPlayerRank(target, args[2]);
                plugin.tablist().render(target);
                plugin.scoreboards().render(target);
                sender.sendMessage("§aRang von " + target.getName() + " auf " + args[2] + " gesetzt.");
            }

            case "edit" -> {
                if (args.length < 4) {
                    sender.sendMessage("§cNutzung: /rank edit <id> <prefix|suffix|chatcolor|weight|displayname> <wert>");
                    return true;
                }
                Rank rank = plugin.ranks().getRank(args[1]);
                if (rank == null) { sender.sendMessage("§cRang existiert nicht."); return true; }
                String value = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));

                switch (args[2].toLowerCase()) {
                    case "prefix" -> rank.setPrefix(value.replace('&', '\u00A7'));
                    case "suffix" -> rank.setSuffix(value.replace('&', '\u00A7'));
                    case "chatcolor" -> rank.setChatColor(value.replace('&', '\u00A7'));
                    case "displayname" -> rank.setDisplayName(value);
                    case "weight" -> {
                        try {
                            rank.setWeight(Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            sender.sendMessage("§cGewicht muss eine Zahl sein.");
                            return true;
                        }
                    }
                    default -> {
                        sender.sendMessage("§cUnbekanntes Feld. Erlaubt: prefix, suffix, chatcolor, displayname, weight");
                        return true;
                    }
                }
                plugin.ranks().save();
                plugin.tablist().refreshAll();
                sender.sendMessage("§aRang '" + args[1] + "' aktualisiert.");
            }

            default -> sender.sendMessage("§7Nutzung: /rank <create|delete|edit|set|list>");
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
        if (args.length == 2 && List.of("delete", "edit", "set").contains(args[0].toLowerCase())) {
            List<String> out = new ArrayList<>();
            for (Rank r : plugin.ranks().getAllRanks()) if (r.getId().startsWith(args[1])) out.add(r.getId());
            return out;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("edit")) {
            return List.of("prefix", "suffix", "chatcolor", "displayname", "weight");
        }
        return List.of();
    }
}
