package com.customcore.plugin.commands;

import com.customcore.plugin.CustomCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CreditsCommand implements CommandExecutor, TabCompleter {

    private final CustomCorePlugin plugin;

    public CreditsCommand(CustomCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /credits (ohne Argumente) -> eigenes Guthaben anzeigen
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cNutzung: /credits <spieler>");
                return true;
            }
            long balance = plugin.economy().getBalance(player);
            sender.sendMessage("§7Dein Guthaben: §6" + plugin.economy().format(balance));
            return true;
        }

        // /credits <spieler> -> fremdes Guthaben anzeigen (jeder darf das)
        if (args.length == 1) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            long balance = plugin.economy().getBalance(target);
            sender.sendMessage("§7Guthaben von " + target.getName() + ": §6" + plugin.economy().format(balance));
            return true;
        }

        // Ab hier: Admin-Befehle
        if (!sender.hasPermission("customcore.credits.admin")) {
            sender.sendMessage("§cDazu hast du keine Berechtigung.");
            return true;
        }

        String sub = args[0].toLowerCase();
        if (args.length < 3 || !List.of("give", "set", "take").contains(sub)) {
            sender.sendMessage("§7Nutzung: /credits give|set|take <spieler> <betrag>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        long amount;
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cUngültiger Betrag.");
            return true;
        }

        switch (sub) {
            case "give" -> {
                plugin.economy().deposit(target, amount);
                sender.sendMessage("§a" + plugin.economy().format(amount) + " an " + target.getName() + " gegeben.");
            }
            case "take" -> {
                boolean ok = plugin.economy().withdraw(target, amount);
                sender.sendMessage(ok
                        ? "§a" + plugin.economy().format(amount) + " von " + target.getName() + " abgezogen."
                        : "§cSpieler hat nicht genug Guthaben.");
            }
            case "set" -> {
                plugin.economy().setBalance(target, amount);
                sender.sendMessage("§aGuthaben von " + target.getName() + " auf " + plugin.economy().format(amount) + " gesetzt.");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>();
            for (String s : List.of("give", "set", "take")) if (s.startsWith(args[0].toLowerCase())) out.add(s);
            for (Player p : Bukkit.getOnlinePlayers()) if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) out.add(p.getName());
            return out;
        }
        if (args.length == 2 && List.of("give", "set", "take").contains(args[0].toLowerCase())) {
            List<String> out = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) out.add(p.getName());
            return out;
        }
        return List.of();
    }
}
