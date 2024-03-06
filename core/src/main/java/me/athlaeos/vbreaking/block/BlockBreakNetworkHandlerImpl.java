package me.athlaeos.vbreaking.block;

import me.athlaeos.vbreaking.ValhallaModulesBreaker;
import me.athlaeos.vbreaking.listeners.CustomBreakSpeedListener;
import me.athlaeos.vbreaking.nms.NetworkHandler;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class BlockBreakNetworkHandlerImpl implements NetworkHandler {
    @Override
    public PacketStatus readBefore(Player player, Object packet) {
        if (player.getGameMode() == GameMode.CREATIVE) return PacketStatus.ALLOW;
        ValhallaModulesBreaker.getNms().readDiggingPacket(player, packet);
        return PacketStatus.ALLOW;
    }

    @Override
    public void readAfter(Player player, Object packet) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        DigPacketInfo info = ValhallaModulesBreaker.getNms().readDiggingPacket(player, packet);
        if (info == null || info.getType() == DigPacketInfo.Type.INVALID) return;

        switch (info.getType()){
            case ABORT, STOP, INVALID -> CustomBreakSpeedListener.onStop(info);
            case START -> CustomBreakSpeedListener.onStart(info);
        }
    }
}
