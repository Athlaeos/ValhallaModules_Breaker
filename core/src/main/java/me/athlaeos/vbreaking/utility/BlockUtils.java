package me.athlaeos.vbreaking.utility;

import com.jeff_media.customblockdata.CustomBlockData;
import me.athlaeos.vbreaking.ValhallaModulesBreaker;
import me.athlaeos.vbreaking.configuration.ConfigManager;
import me.athlaeos.vbreaking.item.ItemBuilder;
import me.athlaeos.vbreaking.item.MiningSpeed;
import me.athlaeos.vbreaking.listeners.CustomBreakSpeedListener;
import me.athlaeos.vbreaking.playerstats.EntityCache;
import me.athlaeos.vbreaking.playerstats.EntityProperties;
import me.athlaeos.vbreaking.playerstats.PlayerProfile;
import me.athlaeos.vbreaking.playerstats.PlayerProfileManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class BlockUtils {
    private static final NamespacedKey CUSTOM_HARDNESS = new NamespacedKey(ValhallaModulesBreaker.getInstance(), "custom_hardness");

    private static final Map<Material, Float> customBlockHardnesses = new HashMap<>();
    static {
        YamlConfiguration config = ConfigManager.getConfig("default_block_hardnesses.yml").get();
        ConfigurationSection section = config.getConfigurationSection("");
        if (section != null){
            for (String material : section.getKeys(false)){
                Material block = Catch.catchOrElse(() -> Material.valueOf(material), null);
                if (block == null) ValhallaModulesBreaker.logWarning("Material in default_block_hardnesses.yml is invalid: " + material);
                else customBlockHardnesses.put(block, (float) config.getDouble(material));
            }
        }
    }

    public static void setCustomHardness(Block b, float hardness){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaModulesBreaker.getInstance());
        customBlockData.set(CUSTOM_HARDNESS, PersistentDataType.FLOAT, hardness);
    }

    public static void setDefaultHardness(Material m, Float hardness){
        if (hardness == null) customBlockHardnesses.remove(m);
        else customBlockHardnesses.put(m, hardness);
    }

    public static float getHardness(Block b){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaModulesBreaker.getInstance());
        return customBlockData.getOrDefault(CUSTOM_HARDNESS, PersistentDataType.FLOAT, customBlockHardnesses.getOrDefault(b.getType(), b.getType().getHardness()));
    }

    public static boolean hasCustomHardness(Block b){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaModulesBreaker.getInstance());
        return customBlockData.has(CUSTOM_HARDNESS, PersistentDataType.FLOAT);
    }

    public static void removeCustomHardness(Block b){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaModulesBreaker.getInstance());
        customBlockData.remove(CUSTOM_HARDNESS);
    }

    private static final ItemStack stic = new ItemStack(Material.STICK);
    public static boolean hasDrops(Block b, Entity e, ItemStack item){
        return !b.getDrops(item == null ? stic : item, e).isEmpty();
    }

    private static final Map<UUID, Float> cachedMultiplier = new HashMap<>();
    private static final Collection<UUID> cachedSwimmingMiners = new HashSet<>();
    private static final Collection<UUID> cachedAirMiners = new HashSet<>();
    public static void resetMinerCache(UUID uuid){
        cachedMultiplier.remove(uuid);
        cachedSwimmingMiners.remove(uuid);
        cachedAirMiners.remove(uuid);
    }

    public static float calculateBlockDamage(Player digger, Block b){
        if (digger == null || b == null || b.getType().isAir()) return 0;
        ItemBuilder tool = EntityCache.getAndCacheProperties(digger).getMainHand();
        PlayerProfile profile = PlayerProfileManager.getProfile(digger);
        if (tool == null) {
            // replace empty hand tool only if tool power of that item would be > 1 (valid tool for block)
            if (profile != null && profile.getCachedEmptyHandTool() != null && ValhallaModulesBreaker.getNms().toolPower(profile.getEmptyHandTool(), b) > 1) tool = profile.getCachedEmptyHandTool();
        }
        float hardness = tool == null ? BlockUtils.getHardness(b) : MiningSpeed.getHardness(digger, tool.getMeta(), b);
        if (hardness < 0 || hardness > 100000) return 0;
        EntityProperties properties = EntityCache.getAndCacheProperties(digger);
        Map<Material, Material> hardnessTranslations = MiningSpeed.getHardnessTranslations(digger, tool == null ? null : tool.getMeta());
        // changing tool power to be equal to that if the tool mined a different type of block
        float toolStrength = !hardnessTranslations.isEmpty() && hardnessTranslations.containsKey(b.getType()) ?
                ValhallaModulesBreaker.getNms().toolPower(tool == null ? null : tool.getItem(), hardnessTranslations.get(b.getType())) :
                ValhallaModulesBreaker.getNms().toolPower(tool == null ? null : tool.getItem(), b);

        boolean canHarvest = BlockUtils.hasDrops(b, digger, tool == null ? null : tool.getItem());
        float baseMultiplier = 1;
        if (toolStrength > 1){
            // preferred tool for block
            baseMultiplier = tool == null ? 1F : (float) MiningSpeed.getMiningPower(tool.getMeta(), b.getType());

            int efficiency = tool == null ? 0 : tool.getItem().getEnchantmentLevel(Enchantment.DIG_SPEED);
            if (efficiency > 0) baseMultiplier += Math.pow(efficiency, 2) + 1;
        }

        boolean canSwimMine = cachedSwimmingMiners.contains(digger.getUniqueId());
        boolean canAirMine = cachedAirMiners.contains(digger.getUniqueId());
        float additionalMultiplier = 1;
        if (profile != null) {
            baseMultiplier += profile.getBreakingSpeedBonus();
            if (profile.getBlockSpecificBreakingSpeedBonus().containsKey(b.getType().toString()))
                baseMultiplier += profile.getBlockSpecificBreakingSpeedBonus().get(b.getType().toString());
        }
        if (cachedMultiplier.containsKey(digger.getUniqueId())) {
            additionalMultiplier = cachedMultiplier.get(digger.getUniqueId());
        } else {
            PotionEffect haste = digger.getPotionEffect(PotionEffectType.FAST_DIGGING);
            if (haste != null) additionalMultiplier += (0.2 * haste.getAmplifier());

            PotionEffect fatigue = digger.getPotionEffect(PotionEffectType.SLOW_DIGGING);
            if (fatigue != null && fatigue.getAmplifier() != -1 && fatigue.getAmplifier() < 5) {
                additionalMultiplier *= Math.pow(0.3, Math.min(fatigue.getAmplifier() + 1, 4));
            }

            canSwimMine = properties.getCombinedEnchantments().getOrDefault(Enchantment.WATER_WORKER, 0) > 0 ||
                    digger.hasPotionEffect(PotionEffectType.CONDUIT_POWER) || (profile != null && profile.hasNaturalAquaAffinity());

            cachedMultiplier.put(digger.getUniqueId(), additionalMultiplier);
            if (canSwimMine) cachedSwimmingMiners.add(digger.getUniqueId());
            if (profile != null && profile.hasNaturalAerialAffinity()) cachedAirMiners.add(digger.getUniqueId());

            additionalMultiplier *= EntityUtils.getPlayerMiningSpeed(digger) + properties.getCombinedMiningSpeedMultiplier();
        }
        baseMultiplier *= additionalMultiplier;

        if (isInWater(digger) && !canSwimMine) baseMultiplier /= 5;
        if (!EntityUtils.isOnGround(digger) && !canAirMine) baseMultiplier /= 5;

        float damage = baseMultiplier / hardness;

        if (canHarvest) damage /= 30;
        else damage /= 100;
        return Math.max(0, damage);
    }

    private static boolean isInWater(Player p){
        return p.getEyeLocation().getBlock().getType() == Material.WATER || p.getEyeLocation().getBlock().getType() == Material.LAVA;
    }

    public static void breakBlockInstantly(Player by, Block block){
        blocksToBreakInstantly.put(by.getUniqueId(), block.getLocation());
        if (instantBlockBreakerTask != null && !done) return;
        done = false;
        instantBlockBreakerTask = ValhallaModulesBreaker.getInstance().getServer().getScheduler().runTask(ValhallaModulesBreaker.getInstance(), () -> {
            blocksToBreakInstantly.forEach((u, l) -> {
                Player p = ValhallaModulesBreaker.getInstance().getServer().getPlayer(u);
                if (p == null || !p.isOnline()){
                    blocksToBreakInstantly.remove(u);
                    return;
                }
                Block b = l.getBlock();

                ItemStack tool = null;
                if (ItemUtils.isEmpty(p.getInventory().getItemInMainHand())) {
                    PlayerProfile profile = PlayerProfileManager.getProfile(by);
                    if (profile != null && profile.getEmptyHandTool() != null) tool = profile.getEmptyHandTool();

                    by.getInventory().setItemInMainHand(tool);
                    ValhallaModulesBreaker.getNms().breakBlock(by, block);
                    by.getInventory().setItemInMainHand(null);
                } else ValhallaModulesBreaker.getNms().breakBlock(by, block);

                CustomBreakSpeedListener.getBlockDigProcesses().remove(b.getLocation());
                for (UUID uuid : CustomBreakSpeedListener.getTotalMiningBlocks().getOrDefault(b.getLocation(), new HashSet<>())) CustomBreakSpeedListener.getMiningPlayers().remove(uuid);
                CustomBreakSpeedListener.getTotalMiningBlocks().remove(b.getLocation());
                BlockUtils.removeCustomHardness(b);
                sendCracks(b, -1);
            });
            blocksToBreakInstantly.clear();
            done = true;
        });
    }

    private static boolean done = false;
    private static BukkitTask instantBlockBreakerTask = null;
    private static final Map<UUID, Location> blocksToBreakInstantly = new HashMap<>();

    public static void sendCracks(Block block, int cracks){
        ValhallaModulesBreaker.getInstance().getServer().getScheduler().runTask(ValhallaModulesBreaker.getInstance(), () -> {
            for (Entity p : block.getWorld().getNearbyEntities(block.getLocation(), 20, 20, 20, (e) -> e instanceof Player))
                ValhallaModulesBreaker.getNms().blockBreakAnimation((Player) p, block, getID(block), cracks);
        });
    }

    private static int getID(Block block){
        return ((block.getX() & 0xFFF) << 20) | ((block.getZ() & 0xFFF) << 8) | (block.getY() & 0xFF);
    }
}
