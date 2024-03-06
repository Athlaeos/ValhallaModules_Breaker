package me.athlaeos.vbreaking.block;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Much of this was referenced from asangarin's breaker plugin
 */

public class DigPacketInfo {
    private final Player digger;
    private final Location l;
    private final Type type;

    public DigPacketInfo(Player digger, int x, int y, int z, Type type){
        this.digger = digger;
        this.l = new Location(digger.getWorld(), x, y, z);
        this.type = type;
    }

    public Location getLocation() {
        return l;
    }

    public Player getDigger() {
        return digger;
    }

    public Type getType() {
        return type;
    }

    public int id(){
        return ((l.getBlockX() & 0xFFF) << 20) | ((l.getBlockZ() & 0xFFF) << 8) | (l.getBlockY() & 0xFF);
    }

    public Block getBlock(){
        return l.getBlock();
    }

    public boolean finished(){
        return type == Type.ABORT || type == Type.STOP;
    }

    public static Type fromName(String name) {
        return switch (name.toUpperCase()) {
            case "START_DESTROY_BLOCK" -> Type.START;
            case "STOP_DESTROY_BLOCK" -> Type.STOP;
            case "ABORT_DESTROY_BLOCK" -> Type.ABORT;
            default -> Type.INVALID;
        };
    }


    public enum Type{
        START, STOP, ABORT, INVALID
    }

    private static record BlockCache(Block block, double value){}
}
