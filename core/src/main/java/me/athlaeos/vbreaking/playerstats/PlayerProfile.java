package me.athlaeos.vbreaking.playerstats;

import me.athlaeos.vbreaking.item.ItemBuilder;
import me.athlaeos.vbreaking.utility.ItemUtils;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerProfile {
    private static final Map<UUID, ItemBuilder> cachedItems = new HashMap<>();

    private final UUID owner;
    private double breakingSpeedBonus = 0;
    private ItemStack emptyHandTool = null;
    private boolean naturalAquaAffinity = false;
    private boolean naturalAerialAffinity = false;
    private final Map<String, Double> blockSpecificBreakingSpeedBonus = new HashMap<>();
    private final Map<String, String> blockHardnessTranslations = new HashMap<>();

    public PlayerProfile(UUID owner){
        this.owner = owner;
    }

    public UUID getOwner() { return owner; }
    public double getBreakingSpeedBonus() { return breakingSpeedBonus; }
    public ItemStack getEmptyHandTool() { return emptyHandTool; }
    public void setNaturalAquaAffinity(boolean naturalAquaAffinity) { this.naturalAquaAffinity = naturalAquaAffinity; }
    public void setNaturalAerialAffinity(boolean naturalAerialAffinity) { this.naturalAerialAffinity = naturalAerialAffinity; }
    public Map<String, Double> getBlockSpecificBreakingSpeedBonus() { return blockSpecificBreakingSpeedBonus; }
    public Map<String, String> getBlockHardnessTranslations() { return blockHardnessTranslations; }
    public void setBreakingSpeedBonus(double breakingSpeedBonus) { this.breakingSpeedBonus = breakingSpeedBonus; }
    public void setEmptyHandTool(ItemStack emptyHandTool) {
        this.emptyHandTool = emptyHandTool;
        cachedItems.put(owner, ItemUtils.isEmpty(emptyHandTool) ? null : new ItemBuilder(emptyHandTool));
    }
    public ItemBuilder getCachedEmptyHandTool(){ return cachedItems.get(owner); }
    public boolean hasNaturalAerialAffinity() { return naturalAerialAffinity; }
    public boolean hasNaturalAquaAffinity() { return naturalAquaAffinity; }
}
