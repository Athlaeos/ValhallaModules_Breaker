package me.athlaeos.vbreaking.hooks;

import me.athlaeos.vbreaking.ValhallaModulesBreaker;
import me.athlaeos.vbreaking.playerstats.PlayerProfile;
import me.athlaeos.vbreaking.playerstats.PlayerProfileManager;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PAPIHook extends PluginHook{

    public PAPIHook() {
        super("PlaceholderAPI");
    }

    public static String parse(Player player, String string){
        return PlaceholderAPI.setPlaceholders(player, string);
    }

    @Override
    public void whenPresent() {
        new PlaceholderExpansion() {
            @Override
            public @NotNull String getIdentifier() {
                return "vbreaking";
            }

            @Override
            public @NotNull String getAuthor() {
                return "Athlaeos";
            }

            @Override
            public @NotNull String getVersion() {
                return ValhallaModulesBreaker.getInstance().getDescription().getVersion();
            }

            @Override
            public boolean persist() {
                return true;
            }

            @Override
            public String onRequest(OfflinePlayer player, @NotNull String params) {
                if (params.equalsIgnoreCase("speedbonus")) {
                    if (player == null || !player.isOnline()) return "+0%";
                    PlayerProfile profile = PlayerProfileManager.getProfile((Player) player);
                    return String.format("%s%.1f%%", profile.getBreakingSpeedBonus() >= 0 ? "+" : "", profile.getBreakingSpeedBonus() * 100);
                }
                return null;
            }
        }.register();
    }
}
