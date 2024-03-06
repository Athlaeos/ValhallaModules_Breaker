package me.athlaeos.vbreaking.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Timer {
    private static final Map<String, Map<UUID, Long>> allCooldowns = new HashMap<>();
    private static final Map<String, Map<UUID, Long>> allTimers = new HashMap<>();
    private static final Map<String, Map<UUID, Long>> allTimersNanos = new HashMap<>();

    /**
     * Sets a cooldown of a given duration.
     * @param entity the entity to set the cooldown to
     * @param timems the time (in milliseconds) for the cooldown
     * @param cooldownKey the cooldown key, should be unique per cooldown timer
     */
    public static void setCooldown(UUID entity, int timems, String cooldownKey){
        if (!allCooldowns.containsKey(cooldownKey)) allCooldowns.put(cooldownKey, new HashMap<>());
        allCooldowns.get(cooldownKey).put(entity, System.currentTimeMillis() + timems);
    }

    /**
     * Returns the remaining cooldown of the entity.
     * @param entity the entity to return the cooldown of
     * @param cooldownKey the key of the cooldown to return
     * @return the remaining cooldown in milliseconds, or 0 if passed
     */
    public static long getCooldown(UUID entity, String cooldownKey){
        if (!allCooldowns.containsKey(cooldownKey)) allCooldowns.put(cooldownKey, new HashMap<>());
        if (allCooldowns.get(cooldownKey).containsKey(entity)){
            return allCooldowns.get(cooldownKey).get(entity) - System.currentTimeMillis();
        }
        return 0;
    }

    /**
     * Returns true if the cooldown has passed
     * @param entity the entity to check their cooldown
     * @param cooldownKey the cooldown key to check
     * @return true if passed, false if not
     */
    public static boolean isCooldownPassed(UUID entity, String cooldownKey){
        if (!allCooldowns.containsKey(cooldownKey)) allCooldowns.put(cooldownKey, new HashMap<>());
        if (allCooldowns.get(cooldownKey).containsKey(entity)){
            return allCooldowns.get(cooldownKey).get(entity) <= System.currentTimeMillis();
        }
        return true;
    }
}