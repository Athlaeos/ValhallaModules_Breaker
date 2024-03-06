package me.athlaeos.vbreaking.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.athlaeos.vbreaking.ValhallaModulesBreaker;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldGuardWrapper {
    public static boolean canBreakBlocks(Location l, Player p){
        if (l.getWorld() == null) return false;
        if (ValhallaModulesBreaker.isHookFunctional(WorldGuardHook.class)){
            LocalPlayer worldguardPlayer = p == null ? null : WorldGuardPlugin.inst().wrapPlayer(p);
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();

            return query.testState(BukkitAdapter.adapt(l), worldguardPlayer, Flags.BLOCK_BREAK);
        }
        return true;
    }
}
