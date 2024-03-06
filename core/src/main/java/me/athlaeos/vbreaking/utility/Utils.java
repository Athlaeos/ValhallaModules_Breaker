package me.athlaeos.vbreaking.utility;

import me.athlaeos.vbreaking.ValhallaModulesBreaker;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    static final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static List<String> chat(List<String> messages){
        if (messages == null) return new ArrayList<>();
        return messages.stream().map(Utils::chat).toList();
    }

    public static Collection<Player> selectPlayers(CommandSender source, String selector) throws IllegalArgumentException{
        Collection<Player> targets = new HashSet<>();
        if (selector.startsWith("@")){
            for (Entity part : Bukkit.selectEntities(source, selector)){
                if (part instanceof Player)
                    targets.add((Player) part);
            }
        } else {
            Player target = ValhallaModulesBreaker.getInstance().getServer().getPlayer(selector);
            if (target != null) targets.add(target);
        }
        return targets;
    }

    /**
     * Converts all color codes to ChatColor. Works with hex codes.
     * Hex code format is triggered with &#123456
     * @param message the message to convert
     * @return the converted message
     */
    public static String chat(String message) {
        if (message == null || message.isEmpty()) return "";
        char COLOR_CHAR = ChatColor.COLOR_CHAR;
        Matcher matcher = hexPattern.matcher(message);
        StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
        while (matcher.find())
        {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static <T> Map<Integer, List<T>> paginate(int pageSize, List<T> allEntries) {
        Map<Integer, List<T>> pages = new HashMap<>();

        int maxPages = (int) Math.ceil((double) allEntries.size() / (double) pageSize);
        for (int pageNumber = 0; pageNumber < maxPages; pageNumber++) {
            pages.put(pageNumber, allEntries.subList( // sublist from start of page to start of next page
                    Math.min(pageNumber * pageSize, allEntries.size()),
                    Math.min((pageNumber + 1) * pageSize, allEntries.size())
            ));
        }

        return pages;
    }
}
