package me.athlaeos.vbreaking.playerstats;

import com.google.gson.*;
import me.athlaeos.vbreaking.utility.Catch;
import me.athlaeos.vbreaking.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Type;

/**
 * Provided by user Schottky on spigotmc.org
 */
public class ItemStackGSONAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        String element = ItemUtils.serialize(src);
        if (element == null) throw new IllegalStateException("ItemStack could not be serialized");
        return new JsonPrimitive(element);
    }

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        return convert(Catch.catchOrElse(() -> ItemUtils.deserialize(jsonElement.getAsString()), null));
    }

    /**
     * Changes an item's type if its display name contains a formatted string telling the plugin it should be turned
     * into another item if it exists
     * @param i the item to convert
     * @return the converted item
     */
    private ItemStack convert(ItemStack i){
        if (i == null) return null;
        if (!i.hasItemMeta()) return i;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return null;
        if (!meta.hasDisplayName()) return i;
        String displayName = meta.getDisplayName();
        if (!displayName.contains("REPLACEWITH:")) return i;
        String[] args = displayName.split("REPLACEWITH:");
        if (args.length != 2) return i;
        Material m = Catch.catchOrElse(() -> Material.valueOf(args[1]), null);
        if (m == null) return i;
        return new ItemStack(m);
    }
}
