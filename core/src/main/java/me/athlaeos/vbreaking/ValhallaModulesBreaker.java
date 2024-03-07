package me.athlaeos.vbreaking;

import me.athlaeos.vbreaking.commands.*;
import me.athlaeos.vbreaking.configuration.ConfigManager;
import me.athlaeos.vbreaking.configuration.ConfigUpdater;
import me.athlaeos.vbreaking.hooks.*;
import me.athlaeos.vbreaking.listeners.*;
import me.athlaeos.vbreaking.nms.NMS;
import me.athlaeos.vbreaking.block.BlockBreakNetworkHandlerImpl;
import me.athlaeos.vbreaking.nms.PacketListener;
import me.athlaeos.vbreaking.playerstats.PlayerProfileManager;
import me.athlaeos.vbreaking.tools.BlockHardnessStick;
import me.athlaeos.vbreaking.utility.Utils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ValhallaModulesBreaker extends JavaPlugin {
    private static NMS nms = null;
    private static PacketListener packetListener = null;
    private static ValhallaModulesBreaker instance;
    private static final Map<Class<? extends PluginHook>, PluginHook> activeHooks = new HashMap<>();
    private static YamlConfiguration pluginConfig;
    private final static Collection<String> worldBlacklist = new HashSet<>();

    {
        instance = this;
    }

    public static ValhallaModulesBreaker getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        // save and update configs
        pluginConfig = saveAndUpdateConfig("config.yml");
        saveConfig("default_block_hardnesses.yml");

        registerHook(new PAPIHook());
        registerHook(new WorldGuardHook());
    }

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("ValhallMMO") != null){
            getServer().getPluginManager().disablePlugin(this);
            logWarning("ValhallaMMO already found, disabling plugin");
            return;
        }

        if (setupNMS()){
            packetListener = new PacketListener(new BlockBreakNetworkHandlerImpl());
            packetListener.addAll();
            registerListener(new CustomBreakSpeedListener());
            registerListener(packetListener);

            PlayerProfileManager.loadProfiles();
        } else {
            logSevere("No NMS version found for your server version, disabling plugin");
            this.getPluginLoader().disablePlugin(this);
        }

        for (PluginHook hook : activeHooks.values()) hook.whenPresent();

        if (ConfigManager.getConfig("config.yml").get().getBoolean("metrics", true)){
            new Metrics(this, 14942);
        }

        registerListener(new ArmorSwitchListener());
        registerListener(new HandSwitchListener());

        registerListener(new BlockHardnessStick());

        registerCommand(new CustomBreakingStatsCommand(), "vbreaking");

        worldBlacklist.addAll(pluginConfig.getStringList("world_blacklist"));
    }

    @Override
    public void onDisable() {
        for (Player p : getServer().getOnlinePlayers()) {
            CustomBreakSpeedListener.removeFatiguedPlayer(p);
        }
        PlayerProfileManager.saveProfiles();
        if (packetListener != null) packetListener.closeAll();
    }

    private boolean setupNMS() {
        try {
            String version = getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> clazz = Class.forName("me.athlaeos.vbreaking.nms.NMS_" + version);

            if (NMS.class.isAssignableFrom(clazz)) {
                nms = (NMS) clazz.getDeclaredConstructor().newInstance();
            }

            return nms != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static NMS getNms() {
        return nms;
    }

    private void registerListener(Listener listener){
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerCommand(CommandExecutor command, String cmd){
        PluginCommand c = ValhallaModulesBreaker.getInstance().getCommand(cmd);
        if (c == null) return;
        c.setExecutor(command);
    }

    public YamlConfiguration saveConfig(String name){
        save(name);
        return ConfigManager.saveConfig(name).get();
    }

    public void save(String name){
        File file = new File(this.getDataFolder(), name);
        if (!file.exists()) this.saveResource(name, false);
    }

    private void updateConfig(String name){
        File configFile = new File(getDataFolder(), name);
        try {
            ConfigUpdater.update(instance, name, configFile, new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private YamlConfiguration saveAndUpdateConfig(String config){
        save(config);
        updateConfig(config);
        return saveConfig(config);
    }

    public static void logInfo(String message){
        instance.getServer().getLogger().info("[ValhallaMMO] " + message);
    }

    public static void logWarning(String warning){
        instance.getServer().getLogger().warning("[ValhallaMMO] " + warning);
    }
    public static void logFine(String warning){
        instance.getServer().getLogger().fine("[ValhallaMMO] " + warning);
        instance.getServer().getConsoleSender().sendMessage(Utils.chat("&a[ValhallaMMO] " + warning));
    }

    public static void logSevere(String help){
        instance.getServer().getLogger().severe("[ValhallaMMO] " + help);
    }

    public static YamlConfiguration getPluginConfig() {
        return pluginConfig;
    }

    private static void registerHook(PluginHook hook){
        if (hook.isPresent()) activeHooks.put(hook.getClass(), hook);
    }

    public static boolean isHookFunctional(Class<? extends PluginHook> hook){
        return activeHooks.containsKey(hook);
    }

    @SuppressWarnings("unchecked")
    public static <T extends PluginHook> T getHook(Class<T> hook){
        return (T) activeHooks.get(hook);
    }

    public static boolean isWorldBlacklisted(String world) {
        return worldBlacklist.contains(world);
    }
}
