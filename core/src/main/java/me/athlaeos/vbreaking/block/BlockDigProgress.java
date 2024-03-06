package me.athlaeos.vbreaking.block;

import me.athlaeos.vbreaking.ValhallaModulesBreaker;
import me.athlaeos.vbreaking.listeners.CustomBreakSpeedListener;
import me.athlaeos.vbreaking.utility.BlockUtils;
import me.athlaeos.vbreaking.utility.Timer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockDigProgress {
    private final Block block;
    private float health = 1F;
    private int ticksSinceUpdate = 0;

    private int lastStage = 0;

    public void damage(Player by, float damage, boolean force){
        // if block is forcefully damaged, the breaking cooldown is ignored
        if (!force && !Timer.isCooldownPassed(by.getUniqueId(), "delay_block_breaking_allowed")) return;
        health -= damage;
        ticksSinceUpdate = 0;
        if (health <= 0F) {
            // if block is not forcefully damaged and the damage was not enough to instantly break the block,
            // a cooldown is applied in which the player can not break the next block
            if (!force && damage < 1F && CustomBreakSpeedListener.isVanillaBlockBreakDelay()) Timer.setCooldown(by.getUniqueId(), 300, "delay_block_breaking_allowed");
            BlockUtils.breakBlockInstantly(by, block);
        } else {
            lastStage = getCracks();
            BlockUtils.sendCracks(block, lastStage);
        }
    }

    public void damage(Player by, float damage){
        damage(by, damage, false);
    }

    public int getTicksSinceUpdate() {
        return ticksSinceUpdate;
    }

    public BlockDigProgress(Block b){
        this.block = b;
    }

    public int getCracks(){
        if (health <= 0 || health >= 1F) return -1;
        return (int) Math.floor((1F - health) * 10F);
    }
    public void incrementTicksSinceUpdate(){
        ticksSinceUpdate++;
    }
    public void heal(float health){
        this.health += health;
        int cracks = getCracks();
        if (lastStage == cracks) return;
        lastStage = cracks;
        BlockUtils.sendCracks(block, cracks);
    }

    public float getHealth() {
        return health;
    }
}
