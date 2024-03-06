package me.athlaeos.vbreaking.utility;

import me.athlaeos.vbreaking.item.MiningSpeed;
import me.athlaeos.vbreaking.playerstats.EntityProperties;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EntityUtils {
    private static Method isOnGroundMethod = null;
    /**
     * Method by mfnalex (jeff media)
     */
    public static boolean isOnGround(Entity e){
        try {
            // Use reflection to get the isOnGround method from the Entity class
            Method method = isOnGroundMethod;
            if (method == null) {
                isOnGroundMethod = Entity.class.getDeclaredMethod("isOnGround");
                method = isOnGroundMethod;
            }

            // Invoke the method on the Player object
            return (boolean) method.invoke(e);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static EntityProperties getEntityProperties(LivingEntity e){
        EntityProperties equipment = new EntityProperties();
        if (e == null) return equipment;
        return updateProperties(equipment, e);
    }

    private static final Attribute miningSpeedAttribute = Catch.catchOrElse(() -> Attribute.valueOf("GENERIC_BLOCK_BREAK_SPEED"), null);
    public static double getPlayerMiningSpeed(Player p){
        if (miningSpeedAttribute == null) return 1.0;
        AttributeInstance speed = p.getAttribute(miningSpeedAttribute);
        if (speed != null) return speed.getBaseValue();
        return 1.0;
    }

    public static EntityProperties updateProperties(EntityProperties properties, LivingEntity e){
        if (e.getEquipment() != null) {
            properties.getCombinedEnchantments().clear();
            properties.setHelmet(e.getEquipment().getHelmet());
            properties.setChestplate(e.getEquipment().getChestplate());
            properties.setLeggings(e.getEquipment().getLeggings());
            properties.setBoots(e.getEquipment().getBoots());
            properties.setMainHand(e.getEquipment().getItemInMainHand());
            properties.setOffHand(e.getEquipment().getItemInOffHand());

            properties.addCombinedEnchantments(properties.getHelmet());
            properties.addCombinedEnchantments(properties.getChestplate());
            properties.addCombinedEnchantments(properties.getLeggings());
            properties.addCombinedEnchantments(properties.getBoots());
            properties.addCombinedEnchantments(properties.getMainHand());
            properties.addCombinedEnchantments(properties.getOffHand());

            double multiplier = 0;
            if (properties.getHelmet() != null) multiplier += MiningSpeed.getMiningSpeedBonus(properties.getHelmet().getMeta());
            if (properties.getChestplate() != null) multiplier += MiningSpeed.getMiningSpeedBonus(properties.getChestplate().getMeta());
            if (properties.getLeggings() != null) multiplier += MiningSpeed.getMiningSpeedBonus(properties.getLeggings().getMeta());
            if (properties.getBoots() != null) multiplier += MiningSpeed.getMiningSpeedBonus(properties.getBoots().getMeta());
            if (properties.getMainHand() != null) multiplier += MiningSpeed.getMiningSpeedBonus(properties.getMainHand().getMeta());
            if (properties.getOffHand() != null) multiplier += MiningSpeed.getMiningSpeedBonus(properties.getOffHand().getMeta());
            properties.setCombinedMiningSpeedMultiplier(multiplier);
        }

        return properties;
    }
}
