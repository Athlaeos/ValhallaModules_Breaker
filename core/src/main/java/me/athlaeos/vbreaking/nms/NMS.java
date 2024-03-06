package me.athlaeos.vbreaking.nms;

import io.netty.channel.Channel;
import me.athlaeos.vbreaking.block.DigPacketInfo;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public interface NMS extends Listener {
    Channel channel(Player p);
    DigPacketInfo readDiggingPacket(Player p, Object packet);

    void blockBreakAnimation(Player p, Block b, int id, int stage);
    float toolPower(ItemStack tool, Block b);
    float toolPower(ItemStack tool, Material b);
    void breakBlock(Player p, Block b);
}
