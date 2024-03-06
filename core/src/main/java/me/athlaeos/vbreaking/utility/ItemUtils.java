package me.athlaeos.vbreaking.utility;

import me.athlaeos.vbreaking.ValhallaModulesBreaker;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class ItemUtils {
    public static boolean isEmpty(ItemStack i){
        return i == null || i.getType().isAir() || i.getAmount() <= 0;
    }

    private static final NamespacedKey TYPE_KEY = new NamespacedKey(ValhallaModulesBreaker.getInstance(), "temporary_type_storage");

    /**
     * Wrapper to get the item meta of the item. The type of the item is immediately stored as temporary variable onto the meta.
     * If the meta is to be returned to the item, {@link ItemUtils#setItemMeta(ItemStack, ItemMeta)} is expected to be used.
     * It's not a big deal if it's not used, you just have a jump nbt tag left on the item.
     * @param i the item to get the item meta from
     * @return the item meta, if any. Null if the item is null or air or if the returned meta is also null.
     */
    public static ItemMeta getItemMeta(ItemStack i){
        if (isEmpty(i)) return null;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return null;
        meta.getPersistentDataContainer().set(TYPE_KEY, PersistentDataType.STRING, i.getType().toString());
        return meta;
    }

    public static String getPDCString(NamespacedKey key, ItemMeta i, String def){
        String value = i.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return value == null ? def : value;
    }

    /**
     * Sets the item meta to the item, removing the temporary type variable from the meta first.
     * @param i the item to set the item meta to
     * @param meta the item meta to put on the item
     */
    public static void setItemMeta(ItemStack i, ItemMeta meta){
        meta = meta.clone();
        meta.getPersistentDataContainer().remove(TYPE_KEY);
        i.setItemMeta(meta);
    }

    /**
     * Gets the stored type from the item meta, if any
     * @param meta the meta to get the stored type of
     * @return the Material type if one is present
     */
    public static Material getStoredType(ItemMeta meta){
        return stringToMaterial(meta.getPersistentDataContainer().get(TYPE_KEY, PersistentDataType.STRING), null);
    }

    /**
     * Updates the stored type on the metadata. Should be used whenever {@link ItemStack#setType(Material)} is used.
     * @param meta the meta to update its stored material tag on
     * @param newType the new material to store on the meta
     */
    public static void updateStoredType(ItemMeta meta, Material newType){
        if (meta.getPersistentDataContainer().has(TYPE_KEY, PersistentDataType.STRING))
            meta.getPersistentDataContainer().set(TYPE_KEY, PersistentDataType.STRING, newType.toString());
    }

    public static Material stringToMaterial(String material, Material def){
        if (material == null || material.isEmpty()) return def;
        try {
            return Material.valueOf(material);
        } catch (IllegalArgumentException ignored){
            return def;
        }
    }

    public static String serialize(ItemStack itemStack) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeObject(itemStack);

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception ignored) {}
        return null;
    }

    public static ItemStack deserialize(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack i = (ItemStack) dataInput.readObject();
            dataInput.close();
            return i;
        } catch (ClassNotFoundException | IOException ignored) {}
        return null;
    }

    public static Collection<Material> getMaterialSet(Collection<String> materials){
        Collection<Material> m = new HashSet<>();
        if (materials == null) return m;
        for (String s : materials){
            try {
                m.add(Material.valueOf(s));
            } catch (IllegalArgumentException ignored){
            }
        }
        return m;
    }

    public static Collection<Material> getMaterialSet(String... materials){
        return getMaterialSet(Arrays.asList(materials));
    }

    private static final Collection<Material> instantlyBreakingItems = getMaterialSet(
            "AIR", "GRASS", "TALL_GRASS", "SHORT_GRASS", "END_ROD", "BARRIER", "BRAIN_CORAL",
            "BRAIN_CORAL_FAN", "BUBBLE_CORAL", "BUBBLE_CORAL_FAN", "FIRE_CORAL", "FIRE_CORAL_FAN", "HORN_CORAL",
            "HORN_CORAL_FAN", "TUBE_CORAL", "TUBE_CORAL_FAN", "DEAD_BRAIN_CORAL", "DEAD_BRAIN_CORAL_FAN",
            "DEAD_BUBBLE_CORAL", "DEAD_BUBBLE_CORAL_FAN", "DEAD_FIRE_CORAL", "DEAD_FIRE_CORAL_FAN", "DEAD_HORN_CORAL",
            "DEAD_HORN_CORAL_FAN", "DEAD_TUBE_CORAL", "DEAD_TUBE_CORAL_FAN", "TORCH", "REDSTONE_TORCH",
            "WALL_TORCH", "REDSTONE_WALL_TORCH", "FERN", "LARGE_FERN", "BEETROOTS", "WHEAT", "POTATOES",
            "CARROTS", "OAK_SAPLING", "DARK_OAK_SAPLING", "SPRUCE_SAPLING", "ACACIA_SAPLING", "BIRCH_SAPLING",
            "JUNGLE_SAPLING", "FLOWER_POT", "POPPY", "DANDELION", "ALLIUM", "BLUE_ORCHID", "AZURE_BLUET",
            "RED_TULIP", "ORANGE_TULIP", "WHITE_TULIP", "PINK_TULIP", "OXEYE_DAISY", "CORNFLOWER",
            "LILY_OF_THE_VALLEY", "WITHER_ROSE", "SUNFLOWER", "LILAC", "ROSE_BUSH", "PEONY", "LILY_PAD",
            "FIRE", "DEAD_BUSH", "MELON_STEM", "PUMPKIN_STEM", "BROWN_MUSHROOM", "RED_MUSHROOM",
            "NETHER_WART", "REDSTONE_WIRE", "COMPARATOR", "REPEATER", "SLIME_BLOCK", "STRUCTURE_VOID",
            "SUGAR_CANE", "TNT", "TRIPWIRE", "TRIPWIRE_HOOK", "WARPED_FUNGUS", "CRIMSON_FUNGUS",
            "HONEY_BLOCK", "NETHER_SPROUTS", "CRIMSON_ROOTS", "WARPED_ROOTS", "TWISTING_VINES_PLANT",
            "WEEPING_VINES_PLANT", "SMALL_DRIPLEAF", "CAVE_VINES_PLANT", "CAVE_VINES", "SPORE_BLOSSOM",
            "AZALEA", "FLOWERING_AZALEA", "DECORATED_POT", "FROGSPAWN", "PINK_PETALS", "PITCHER_CROP",
            "PITCHER_PLANT", "TORCHFLOWER", "TORCHFLOWER_CROP"
    );

    public static boolean breaksInstantly(Material m){
        return instantlyBreakingItems.contains(m);
    }
}
