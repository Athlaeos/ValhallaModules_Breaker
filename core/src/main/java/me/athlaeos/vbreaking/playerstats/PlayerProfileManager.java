package me.athlaeos.vbreaking.playerstats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.athlaeos.vbreaking.ValhallaModulesBreaker;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PlayerProfileManager {
    private static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ItemStackGSONAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();

    private static final Map<UUID, PlayerProfile> profiles = new HashMap<>();

    public static void loadProfiles(){
        File f = new File(ValhallaModulesBreaker.getInstance().getDataFolder(), "player_stats.json");
        if (!f.exists()) {
            try {
                if (!f.createNewFile()) ValhallaModulesBreaker.logWarning("Could not create new file for player_stats.json");
            } catch (IOException ignored) {}
        }
        if (f.exists()){
            try (BufferedReader reader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))){
                PlayerProfile[] collectedProfiles = gson.fromJson(reader, PlayerProfile[].class);
                if (collectedProfiles != null)
                    for (PlayerProfile profile : collectedProfiles)
                        if (profile != null) {
                            profiles.put(profile.getOwner(), profile);
                            profile.setEmptyHandTool(profile.getEmptyHandTool()); // updates cache
                        }
            } catch (IOException | JsonSyntaxException | NoClassDefFoundError ignored){
            }
        }
    }

    public static void saveProfiles(){
        File f = new File(ValhallaModulesBreaker.getInstance().getDataFolder(), "player_stats.json");
        if (!f.exists()) {
            try {
                if (!f.createNewFile()) ValhallaModulesBreaker.logWarning("Could not create new file for player_stats.json");
            } catch (IOException ignored) {}
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8))){
            JsonElement element = gson.toJsonTree(new ArrayList<>(profiles.values()), new TypeToken<ArrayList<PlayerProfile>>(){}.getType());
            gson.toJson(element, writer);
            writer.flush();
        } catch (IOException | JsonSyntaxException ignored) {}
    }

    public static PlayerProfile getOrCreateProfile(Player p){
        PlayerProfile profile = profiles.get(p.getUniqueId());
        if (profile == null){
            profile = new PlayerProfile(p.getUniqueId());
            profiles.put(p.getUniqueId(), profile);
        }
        return profile;
    }

    public static PlayerProfile getProfile(Player p){
        return profiles.get(p.getUniqueId());
    }
}
