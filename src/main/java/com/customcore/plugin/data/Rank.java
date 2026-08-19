package com.customcore.plugin.data;

public class Rank {

    private String id;
    private String displayName;
    private String prefix;
    private String suffix;
    private String chatColor; // z.B. "&a"
    private int weight;       // Höher = wichtiger, bestimmt Sortierung in Tablist

    public Rank(String id, String displayName, String prefix, String suffix, String chatColor, int weight) {
        this.id = id;
        this.displayName = displayName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.chatColor = chatColor;
        this.weight = weight;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public String getChatColor() { return chatColor; }
    public void setChatColor(String chatColor) { this.chatColor = chatColor; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
}
