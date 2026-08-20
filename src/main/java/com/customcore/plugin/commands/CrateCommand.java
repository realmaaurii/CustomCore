package com.customcore.plugin.commands;

import com.customcore.plugin.CustomCorePlugin;
import com.customcore.plugin.crates.CrateType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CrateCommand implements CommandExecutor, TabCompleter {

    private final CustomCorePlugin plugin;

    public CrateCommand(CustomCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customcore.crate.admin")) {
            sender.sendMessage("§cDazu hast du keine Berechtigung.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§7Nutzung: /crate give <spieler> <crateid> [anzahl]");
            sender.sendMessage("§7           /crate list | reload");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (args.length < 3) { sender.sendMessage("§cNutzung: /crate give <spieler> <crateid> [anzahl]"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage("§cSpieler nicht online."); return true; }
                CrateType type = plugin.crates().getCrateType(args[2]);
                if (type == null) { sender.sendMessage("§cCrate-Typ existiert nicht."); return true; }
                int amount = 1;
                if (args.length >= 4) {
                    try { amount = Integer.parseInt(args[3]); } catch (NumberFormatException ignored) {}
                }
                plugin.crates().giveKey(target, type, amount);
                sender.sendMessage("§a" + amount + "x " + type.getDisplayName() + " Key an " + target.getName() + " gegeben.");
            }

            case "list" -> {
                sender.sendMessage("§6Verfügbare Crates:");
                for (CrateType t : plugin.crates().getAllCrateTypes()) {
                    sender.sendMessage(" §7- §f" + t.getId() + " §7(" + t.getRewards().size() + " Belohnungen)");
                }
            }

            case "reload" -> {
                plugin.crates().load();
                sender.sendMessage("§aCrates neu geladen (crates.yml).");
            }

            default -> sender.sendMessage("§7Nutzung: /crate give <spieler> <crateid> [anzahl] | list | reload");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>();
            for (String s : List.of("give", "list", "reload")) if (s.startsWith(args[0].toLowerCase())) out.add(s);
            return out;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> out = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) out.add(p.getName());
            return out;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            List<String> out = new ArrayList<>();
            for (CrateType t : plugin.crates().getAllCrateTypes()) if (t.getId().startsWith(args[2])) out.add(t.getId());
            return out;
        }
        return List.of();
    }
}
