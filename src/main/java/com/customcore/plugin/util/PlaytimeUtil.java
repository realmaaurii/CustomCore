package com.customcore.plugin.util;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public class PlaytimeUtil {

    /** Formatiert die Gesamt-Spielzeit eines Spielers als "Xh Ym". */
    public static String format(Player player) {
        int ticks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        long totalMinutes = ticks / 20L / 60L;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return hours + "h " + minutes + "m";
    }
}
