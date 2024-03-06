package me.athlaeos.vbreaking.playerstats;

import me.athlaeos.vbreaking.item.ItemBuilder;
import me.athlaeos.vbreaking.utility.ItemUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityProperties {
    private ItemBuilder helmet = null;
    private ItemBuilder chestplate = null;
    private ItemBuilder leggings = null;
    private ItemBuilder boots = null;
    private ItemBuilder mainHand = null;
    private ItemBuilder offHand = null;
    private final Map<Enchantment, Integer> combinedEnchantments = new HashMap<>();
    private double combinedMiningSpeedMultiplier = 0;

    public EntityProperties(){}

    public EntityProperties(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, ItemStack mainHand, ItemStack offHand){
        this.helmet = ItemUtils.isEmpty(helmet) ? null : new ItemBuilder(helmet);
        this.chestplate = ItemUtils.isEmpty(chestplate) ? null : new ItemBuilder(chestplate);
        this.leggings = ItemUtils.isEmpty(leggings) ? null : new ItemBuilder(leggings);
        this.boots = ItemUtils.isEmpty(boots) ? null : new ItemBuilder(boots);
        this.mainHand = ItemUtils.isEmpty(mainHand) ? null : new ItemBuilder(mainHand);
        this.offHand = ItemUtils.isEmpty(offHand) ? null : new ItemBuilder(offHand);
    }

    public void addCombinedEnchantments(ItemBuilder builder){
        if (builder == null) return;
        for (Enchantment e : builder.getItem().getEnchantments().keySet()){
            int existingLevel = combinedEnchantments.getOrDefault(e, 0);
            existingLevel += builder.getItem().getEnchantmentLevel(e);
            combinedEnchantments.put(e, existingLevel);
        }
    }

    public ItemBuilder getHelmet() { return helmet; }
    public void setHelmet(ItemStack helmet) { this.helmet = ItemUtils.isEmpty(helmet) ? null : new ItemBuilder(helmet); }
    public ItemBuilder getChestplate() { return chestplate; }
    public void setChestplate(ItemStack chestplate) { this.chestplate = ItemUtils.isEmpty(chestplate) ? null : new ItemBuilder(chestplate); }
    public ItemBuilder getBoots() { return boots; }
    public void setBoots(ItemStack boots) { this.boots = ItemUtils.isEmpty(boots) ? null : new ItemBuilder(boots); }
    public ItemBuilder getLeggings() { return leggings; }
    public void setLeggings(ItemStack leggings) { this.leggings = ItemUtils.isEmpty(leggings) ? null : new ItemBuilder(leggings); }
    public ItemBuilder getMainHand() { return mainHand; }
    public void setMainHand(ItemStack mainHand) { this.mainHand = ItemUtils.isEmpty(mainHand) ? null : new ItemBuilder(mainHand); }
    public ItemBuilder getOffHand() { return offHand; }
    public void setOffHand(ItemStack offHand) { this.offHand = ItemUtils.isEmpty(offHand) ? null : new ItemBuilder(offHand); }
    public Map<Enchantment, Integer> getCombinedEnchantments() { return combinedEnchantments; }

    public double getCombinedMiningSpeedMultiplier() { return combinedMiningSpeedMultiplier; }
    public void setCombinedMiningSpeedMultiplier(double combinedMiningSpeedMultiplier) { this.combinedMiningSpeedMultiplier = combinedMiningSpeedMultiplier; }

    /**
     * Returns a list containing all of the entity's equipment.
     * @param includeHands whether the hand items should be included also
     * @param hand true if only main hand should be returned, false if only offhand, null if both
     * @return a list containing all of the entity's equipment
     */
    public List<ItemBuilder> getIterable(boolean includeHands, Boolean hand){
        List<ItemBuilder> iterable = new ArrayList<>();
        if (helmet != null) iterable.add(helmet);
        if (chestplate != null) iterable.add(chestplate);
        if (leggings != null) iterable.add(leggings);
        if (boots != null) iterable.add(boots);
        if (includeHands){
            if ((hand == null || hand) && mainHand != null) iterable.add(mainHand);
            if ((hand == null || !hand) && offHand != null) iterable.add(offHand);
        }
        return iterable;
    }

    public List<ItemBuilder> getHands(){
        List<ItemBuilder> iterable = new ArrayList<>();
        if (mainHand != null) iterable.add(mainHand);
        if (offHand != null) iterable.add(offHand);
        return iterable;
    }
}