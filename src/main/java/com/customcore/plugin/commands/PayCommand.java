package com.customcore.plugin.commands;

import com.customcore.plugin.CustomCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {

    private final CustomCorePlugin plugin;

    public PayCommand(CustomCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDieser Befehl ist nur für Spieler.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§7Nutzung: /pay <spieler> <betrag>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) { sender.sendMessage("§cSpieler nicht online."); return true; }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            sender.sendMessage("§cDu kannst dir nicht selbst Credits schicken.");
            return true;
        }

        long amount;
        try {
            amount = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cUngültiger Betrag.");
            return true;
        }
        if (amount <= 0) { sender.sendMessage("§cBetrag muss positiv sein."); return true; }

        if (!plugin.economy().withdraw(player, amount)) {
            sender.sendMessage("§cDu hast nicht genug Credits.");
            return true;
        }
        plugin.economy().deposit(target, amount);

        sender.sendMessage("§aDu hast " + plugin.economy().format(amount) + " an " + target.getName() + " gesendet.");
        target.sendMessage("§a" + player.getName() + " hat dir " + plugin.economy().format(amount) + " gesendet!");
        return true;
    }
}
