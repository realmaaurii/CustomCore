package com.customcore.plugin.crates;

import com.customcore.plugin.CustomCorePlugin;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CrateManager {

    private final CustomCorePlugin plugin;
    private final File file;
    private final Map<String, CrateType> crateTypes = new LinkedHashMap<>();
    private final CrateKeyUtil keyUtil;

    // Läuft aktuell: Blockposition -> Session (für schnellen Lookup bei Klicks)
    private final Map<Location, CrateSession> chestToSession = new HashMap<>();
    // Ein Spieler darf immer nur eine Session gleichzeitig offen haben
    private final Map<UUID, CrateSession> activeByPlayer = new HashMap<>();

    private static final int TOTAL_CHESTS = 8;
    private static final int PICKS_ALLOWED = 6;
    private static final int DISTANCE = 3;   // Abstand der Kisten-Paare vom Spieler
    private static final int GAP_OFFSET = 1; // halbe Lücke zwischen den 2 Kisten je Richtung
    private static final long TIMEOUT_TICKS = 20L * 45; // 45 Sekunden, dann Auto-Reset

    public CrateManager(CustomCorePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "crates.yml");
        this.keyUtil = new CrateKeyUtil(plugin);
    }

    public CrateKeyUtil keys() { return keyUtil; }

    public void load() {
        if (!file.exists()) {
            createDefaultFile();
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        crateTypes.clear();

        ConfigurationSection cratesSection = cfg.getConfigurationSection("crates");
        if (cratesSection == null) return;

        for (String id : cratesSection.getKeys(false)) {
            ConfigurationSection s = cratesSection.getConfigurationSection(id);
            if (s == null) continue;

            Material keyMat = Material.matchMaterial(s.getString("key-material", "ENDER_CHEST"));
            if (keyMat == null) keyMat = Material.ENDER_CHEST;

            CrateType type = new CrateType(id, s.getString("display-name", id), keyMat);

            List<Map<?, ?>> rewardMaps = s.getMapList("rewards");
            for (Map<?, ?> rm : rewardMaps) {
                String rewardId = String.valueOf(rm.get("id"));
                Object typeObj = rm.get("type");
                String typeStr = typeObj != null ? String.valueOf(typeObj) : "ITEM";
                Object displayObj = rm.get("display-name");
                String display = displayObj != null ? String.valueOf(displayObj) : rewardId;
                String category = rm.get("category") != null ? String.valueOf(rm.get("category")) : null;
                int weight = rm.get("weight") instanceof Number n ? n.intValue() : 1;

                if ("COMMAND".equalsIgnoreCase(typeStr)) {
                    String command = String.valueOf(rm.get("command"));
                    type.addReward(CrateReward.command(rewardId, display, weight, category, command));
                } else if ("CREDITS".equalsIgnoreCase(typeStr)) {
                    long creditsAmount = rm.get("credits") instanceof Number n ? n.longValue()
                            : (rm.get("amount") instanceof Number n2 ? n2.longValue() : 0);
                    type.addReward(CrateReward.credits(rewardId, display, weight, category, creditsAmount));
                } else {
                    Object materialObj = rm.get("material");
                    String materialName = materialObj != null ? String.valueOf(materialObj) : "STONE";
                    Material mat = Material.matchMaterial(materialName);
                    if (mat == null) mat = Material.STONE;
                    int amount = rm.get("amount") instanceof Number n ? n.intValue() : 1;
                    type.addReward(CrateReward.item(rewardId, display, weight, category, mat, amount));
                }
            }

            crateTypes.put(id, type);
        }
    }

    private void createDefaultFile() {
        FileConfiguration cfg = new YamlConfiguration();

        cfg.set("crates.common.display-name", "&aCommon Crate");
        cfg.set("crates.common.key-material", "ENDER_CHEST");

        String legendary = "&c&lLEGENDARY ITEMS";
        String epic = "&5&lEPIC ITEMS";
        String common = "&f&lCOMMON ITEMS";

        List<Map<String, Object>> rewards = new ArrayList<>();
        rewards.add(rewardMap("elytra", "ITEM", "&5Elytra", 1, legendary, "ELYTRA", 1, null));
        rewards.add(rewardMap("apple", "ITEM", "&6Goldener Apfel", 4, legendary, "GOLDEN_APPLE", 1, null));
        rewards.add(creditsMap("credits_big", "&e1000 Credits", 5, legendary, 1000));
        rewards.add(rewardMap("xp", "COMMAND", "&d150 XP", 6, epic, null, 0, "xp add %player% 150"));
        rewards.add(rewardMap("diamonds", "ITEM", "&b5 Diamanten", 10, epic, "DIAMOND", 5, null));
        rewards.add(rewardMap("emerald", "ITEM", "&a8 Smaragde", 12, epic, "EMERALD", 8, null));
        rewards.add(creditsMap("credits_small", "&e100 Credits", 15, epic, 100));
        rewards.add(rewardMap("gold", "ITEM", "&e16 Gold", 20, common, "GOLD_INGOT", 16, null));
        rewards.add(rewardMap("iron", "ITEM", "&f32 Eisen", 22, common, "IRON_INGOT", 32, null));
        rewards.add(rewardMap("bread", "ITEM", "&7Etwas Brot (Trostpreis)", 25, common, "BREAD", 8, null));
        cfg.set("crates.common.rewards", rewards);

        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Konnte crates.yml nicht erstellen: " + e.getMessage());
        }
    }

    private Map<String, Object> rewardMap(String id, String type, String display, int weight, String category,
                                           String material, int amount, String command) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("type", type);
        m.put("display-name", display);
        m.put("weight", weight);
        if (category != null) m.put("category", category);
        if (material != null) { m.put("material", material); m.put("amount", amount); }
        if (command != null) m.put("command", command);
        return m;
    }

    private Map<String, Object> creditsMap(String id, String display, int weight, String category, long credits) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("type", "CREDITS");
        m.put("display-name", display);
        m.put("weight", weight);
        if (category != null) m.put("category", category);
        m.put("credits", credits);
        return m;
    }

    public CrateType getCrateType(String id) { return crateTypes.get(id); }
    public Collection<CrateType> getAllCrateTypes() { return crateTypes.values(); }

    public void giveKey(Player player, CrateType type, int amount) {
        ItemStack key = keyUtil.createKey(type);
        key.setAmount(Math.max(1, amount));
        player.getInventory().addItem(key);
    }

    // ------------------------------------------------------------------
    // Crate öffnen: 8 Enderchests um den SPIELER platzieren - 4 Paare
    // (Nord/Ost/Süd/West), je 2 Kisten mit sichtbarer Lücke dazwischen.
    // ------------------------------------------------------------------
    public boolean openCrate(Player player, Block groundBlock, CrateType crateType) {
        if (activeByPlayer.containsKey(player.getUniqueId())) {
            player.sendMessage("§cDu hast bereits eine offene Crate - schließe sie zuerst ab!");
            return false;
        }

        int centerX = player.getLocation().getBlockX();
        int centerY = player.getLocation().getBlockY();
        int centerZ = player.getLocation().getBlockZ();

        int[][] directions = {
                {0, -1},  // Norden
                {1, 0},   // Osten
                {0, 1},   // Süden
                {-1, 0},  // Westen
        };

        List<Location> positions = new ArrayList<>();
        for (int[] dir : directions) {
            int dx = dir[0], dz = dir[1];
            int perpX = -dz, perpZ = dx; // 90° gedreht, für den Seitenversatz

            int baseX = centerX + dx * DISTANCE;
            int baseZ = centerZ + dz * DISTANCE;

            positions.add(new Location(player.getWorld(), baseX + perpX * GAP_OFFSET, centerY, baseZ + perpZ * GAP_OFFSET));
            positions.add(new Location(player.getWorld(), baseX - perpX * GAP_OFFSET, centerY, baseZ - perpZ * GAP_OFFSET));
        }

        for (Location loc : positions) {
            if (!loc.getBlock().getType().isAir()) {
                player.sendMessage("§cNicht genug freier Platz um dich herum, um die Crate zu öffnen.");
                return false;
            }
        }

        for (Location loc : positions) {
            loc.getBlock().setType(Material.ENDER_CHEST);
        }

        CrateSession session = new CrateSession(player, crateType, positions, PICKS_ALLOWED);
        activeByPlayer.put(player.getUniqueId(), session);
        for (Location loc : positions) {
            chestToSession.put(normalize(loc), session);
        }

        player.sendMessage("§6§l" + crateType.getDisplayName() + "§r §7ist geöffnet! " +
                "§eWähle " + PICKS_ALLOWED + " von " + TOTAL_CHESTS + " Kisten aus.");
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 1f);

        var task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (activeByPlayer.get(player.getUniqueId()) == session) {
                player.sendMessage("§cDie Crate wurde nicht rechtzeitig abgeschlossen und wurde zurückgesetzt.");
                closeSession(session);
            }
        }, TIMEOUT_TICKS);
        session.setTimeoutTask(task);

        return true;
    }

    /** Wird vom Listener aufgerufen, wenn ein Spieler eine der platzierten Kisten anklickt. */
    public boolean handleChestClick(Player player, Block clickedChest) {
        Location norm = normalize(clickedChest.getLocation());
        CrateSession session = chestToSession.get(norm);
        if (session == null) return false; // keine Session-Kiste, normal weiterlaufen lassen

        if (!session.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("§cDas ist nicht deine Crate!");
            return true;
        }
        if (session.isAlreadyClicked(norm)) {
            return true; // bereits angeklickt, ignorieren
        }

        session.markClicked(norm);
        clickedChest.setType(Material.AIR); // Kiste "verbraucht"

        CrateReward reward = session.getCrateType().rollReward();
        giveReward(player, reward);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.4f);
        player.sendMessage("§a✔ Du hast erhalten: " + (reward != null ? colorize(reward.getDisplayName()) : "§7(nichts)"));
        player.sendMessage("§7Noch " + session.getRemainingPicks() + " Kisten übrig zu wählen.");

        if (session.isFinished()) {
            player.sendMessage("§6§lCrate abgeschlossen!");
            closeSession(session);
        }

        return true;
    }

    private void giveReward(Player player, CrateReward reward) {
        if (reward == null) return;
        switch (reward.getType()) {
            case ITEM -> {
                ItemStack stack = reward.toItemStack();
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
                for (ItemStack over : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), over);
                }
            }
            case COMMAND -> {
                String cmd = reward.getCommand().replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
            case CREDITS -> plugin.economy().deposit(player, reward.getCredits());
        }
    }

    /** Entfernt verbleibende Kisten wieder, räumt die Session auf. */
    private void closeSession(CrateSession session) {
        session.cancelTimeout();
        for (Location loc : session.getChestLocations()) {
            Location norm = normalize(loc);
            chestToSession.remove(norm);
            Block block = loc.getWorld().getBlockAt(loc);
            if (block.getType() == Material.ENDER_CHEST) {
                block.setType(Material.AIR);
            }
        }
        activeByPlayer.remove(session.getOwner());
    }

    private Location normalize(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private String colorize(String s) {
        return s.replace('&', '§');
    }
}
