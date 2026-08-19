package com.customcore.plugin.commands;

import com.customcore.plugin.CustomCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatColorCommand implements CommandExecutor {

    private final CustomCorePlugin plugin;

    public ChatColorCommand(CustomCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDieser Befehl ist nur für Spieler.");
            return true;
        }
        if (!sender.hasPermission("customcore.chatcolor.use")) {
            sender.sendMessage("§cDazu hast du keine Berechtigung.");
            return true;
        }
        if (!plugin.getConfig().getBoolean("chat.allow-player-chatcolor", false)) {
            sender.sendMessage("§cChatfarben sind auf diesem Server deaktiviert.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("§7Nutzung: /chatcolor <farbcode ohne &, z.B. a für grün>");
            return true;
        }

        char code = args[0].toLowerCase().charAt(0);
        if (ChatColor.getByChar(code) == null) {
            sender.sendMessage("§cUngültiger Farbcode.");
            return true;
        }

        plugin.chat().setPlayerColor(player, "&" + code);
        sender.sendMessage("§aChatfarbe gesetzt.");
        return true;
    }
}
