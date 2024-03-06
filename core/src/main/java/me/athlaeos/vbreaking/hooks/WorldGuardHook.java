package me.athlaeos.vbreaking.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardHook extends PluginHook{
    public WorldGuardHook() {
        super("WorldGuard");
    }

    public static boolean canBreakBlocks(Location l, Player p){
        return WorldGuardWrapper.canBreakBlocks(l, p);
    }

    @Override
    public void whenPresent() {
    }
}
