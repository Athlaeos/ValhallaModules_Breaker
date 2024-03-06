package me.athlaeos.vbreaking.commands;

import me.athlaeos.vbreaking.item.MiningSpeed;
import me.athlaeos.vbreaking.playerstats.PlayerProfile;
import me.athlaeos.vbreaking.playerstats.PlayerProfileManager;
import me.athlaeos.vbreaking.tools.SpecializedToolRegistry;
import me.athlaeos.vbreaking.utility.BlockUtils;
import me.athlaeos.vbreaking.utility.Catch;
import me.athlaeos.vbreaking.utility.ItemUtils;
import me.athlaeos.vbreaking.utility.Utils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CustomBreakingStatsCommand implements TabExecutor {
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!sender.hasPermission("vbreaker.vb")){
			sender.sendMessage(Utils.chat("&cYou do not have permission to use this command"));
			return true;
		}
		if (args.length > 1){
			if (args[0].equalsIgnoreCase("stats")){
				if (args.length > 2){
					if (args[1].equalsIgnoreCase("item")){ // vbreaker stats item <stat> <value>
						if (sender instanceof Player p){
							ItemStack held = p.getInventory().getItemInMainHand();
							if (ItemUtils.isEmpty(held)) {
								p.sendMessage(Utils.chat("&cMust be holding an item!"));
								return true;
							}
							ItemMeta meta = held.getItemMeta();
							if (meta == null) return true;
							switch (args[2]){
								case "mining_power" -> {
									if (args.length > 3){
										Double d = Catch.catchOrElse(() -> Double.parseDouble(args[3]), null);
										if (d == null){
											p.sendMessage(Utils.chat("&cInvalid number!"));
											return true;
										}
										MiningSpeed.setMiningPower(meta, d);
										p.sendMessage(Utils.chat("&aThe held item now has a mining power of " + d));
									} else {
										MiningSpeed.setMiningPower(meta, null);
										p.sendMessage(Utils.chat("&aReset the mining power of this item "));
									}
								}
								case "mining_power_exception" -> {
									if (args.length > 4){
										Double d = Catch.catchOrElse(() -> Double.parseDouble(args[4]), null);
										Material m = Catch.catchOrElse(() -> Material.valueOf(args[3]), null);
										if (d == null){
											p.sendMessage(Utils.chat("&cInvalid number!"));
											return true;
										}
										if (m == null || !m.isBlock()){
											p.sendMessage(Utils.chat("&cInvalid block material!"));
											return true;
										}
										MiningSpeed.addMiningPowerException(meta, m, d);
										p.sendMessage(Utils.chat("&aThe held item now has a mining power of " + d + " against " + m + " specifically"));
									} else {
										MiningSpeed.setExceptions(meta, null);
										p.sendMessage(Utils.chat("&aReset the block-specific mining power of this item"));
									}
								}
								case "mining_speed_bonus" -> {
									if (args.length > 3){
										Double d = Catch.catchOrElse(() -> Double.parseDouble(args[3]), null);
										if (d == null){
											p.sendMessage(Utils.chat("&cInvalid number!"));
											return true;
										}
										MiningSpeed.setMiningSpeedBonus(meta, d);
										p.sendMessage(Utils.chat("&aThe held item now mines " + (d * 100) + "% faster"));
									} else {
										MiningSpeed.setMiningPower(meta, null);
										p.sendMessage(Utils.chat("&aRemoved mining speed bonus off the item"));
									}
								}
								case "hardness_translation" -> {
									if (args.length > 4){
										Material m1 = Catch.catchOrElse(() -> Material.valueOf(args[3]), null);
										Material m2 = Catch.catchOrElse(() -> Material.valueOf(args[4]), null);
										if (m1 == null || !m1.isBlock() || m2 == null || !m2.isBlock()){
											p.sendMessage(Utils.chat("&cInvalid block material(s)"));
											return true;
										}
										MiningSpeed.addHardnessTranslation(meta, m1, m2);
										p.sendMessage(Utils.chat("&aThis item now mines " + m1 + " at the same speed as " + m2));
									} else {
										MiningSpeed.setHardnessTranslations(meta, null);
										p.sendMessage(Utils.chat("&aRemoved hardness translations from the item"));
									}
								}
								default -> {
									return false;
								}
							}
							held.setItemMeta(meta);
						} else {
							sender.sendMessage(Utils.chat("&cOnly players may execute this command"));
						}
						return true;
					} else if (args[1].equalsIgnoreCase("player") && args.length > 3){ // vbreaker stats player <player> <stat>
						Collection<Player> targets = Utils.selectPlayers(sender, args[2]);

						if (targets.isEmpty()){
							sender.sendMessage(Utils.chat("&cNo (valid) players found"));
							return true;
						}

						for (Player p : targets){
							PlayerProfile profile = PlayerProfileManager.getOrCreateProfile(p);
							switch (args[3]){
								case "mining_speed_bonus" -> { // vbreaker stats player <player> mining_speed_bonus add/set <value>
									if (args.length > 5){
										Double d = Catch.catchOrElse(() -> Double.parseDouble(args[5]), null);
										if (d == null){
											sender.sendMessage(Utils.chat("&cInvalid number!"));
											return true;
										}
										if (args[4].equalsIgnoreCase("set")){
											profile.setBreakingSpeedBonus(d);
										} else if (args[4].equalsIgnoreCase("add")){
											profile.setBreakingSpeedBonus(profile.getBreakingSpeedBonus() + d);
										} else return false;
										sender.sendMessage(Utils.chat("&a" + p.getName() + " now has a mining speed bonus of " + (profile.getBreakingSpeedBonus() * 100) + "%"));
									} else {
										profile.setBreakingSpeedBonus(0);
										sender.sendMessage(Utils.chat("&a" + p.getName() + " now has no specific mining speed bonus"));
									}
								}
								case "mining_speed_bonus_exception" -> { // vbreaker stats player <player> mining_speed_bonus_exception add/set <block> <value>
									if (args.length > 5){
										Double d = null;
										Material m = Catch.catchOrElse(() -> Material.valueOf(args[5]), null);
										if (m == null || !m.isBlock()){
											p.sendMessage(Utils.chat("&cInvalid block material!"));
											return true;
										}
										if (args.length > 6){
											d = Catch.catchOrElse(() -> Double.parseDouble(args[6]), null);
										}
										if (d == null){
											sender.sendMessage(Utils.chat("&cInvalid number!"));
											return true;
										}
										if (args[4].equalsIgnoreCase("set")){
											profile.getBlockSpecificBreakingSpeedBonus().put(m.toString(), d);
											sender.sendMessage(Utils.chat("&a" + p.getName() + " now has a mining speed bonus of " + (profile.getBlockSpecificBreakingSpeedBonus().getOrDefault(m.toString(),0D) * 100) + "%") + " against " + m);
										} else if (args[4].equalsIgnoreCase("add")){
											profile.getBlockSpecificBreakingSpeedBonus().put(m.toString(), profile.getBlockSpecificBreakingSpeedBonus().getOrDefault(m.toString(), 0D) + d);
											sender.sendMessage(Utils.chat("&a" + p.getName() + " now has a mining speed bonus of " + (profile.getBlockSpecificBreakingSpeedBonus().getOrDefault(m.toString(),0D) * 100) + "%") + " against " + m);
										} else if (args[4].equalsIgnoreCase("remove")) {
											profile.getBlockSpecificBreakingSpeedBonus().remove(m.toString());
											sender.sendMessage(Utils.chat("&a" + p.getName() + " now has no specific mining speed bonus against " + m));
										} else return false;
									} else {
										profile.getBlockSpecificBreakingSpeedBonus().clear();
										sender.sendMessage(Utils.chat("&a" + p.getName() + " now has no specific mining speed bonus against any block"));
									}
								}
								case "hardness_translation" -> { // vbreaker stats player <player> hardness_translation <frommaterial> <tomaterial>
									if (args.length > 5){
										Material m1 = Catch.catchOrElse(() -> Material.valueOf(args[4]), null);
										Material m2 = Catch.catchOrElse(() -> Material.valueOf(args[5]), null);
										if (m1 == null || !m1.isBlock() || m2 == null || !m2.isBlock()){
											p.sendMessage(Utils.chat("&cInvalid block material(s)"));
											return true;
										}
										profile.getBlockHardnessTranslations().put(m1.toString(), m2.toString());
										sender.sendMessage(Utils.chat("&aPlayer " + p.getName() + " now mines " + m1 + " at the same speed as " + m2));
									} else {
										profile.getBlockHardnessTranslations().clear();
										sender.sendMessage(Utils.chat("&aRemoved hardness translations from " + p.getName()));
									}
								}
								case "empty_hand_tool" -> { // vbreaker stats player <player> hardness_translation <material>
									if (args.length > 4 && !args[4].equalsIgnoreCase("hand")){
										Material m = Catch.catchOrElse(() -> Material.valueOf(args[4]), null);
										if (m == null){
											p.sendMessage(Utils.chat("&cInvalid tool material(s)"));
											return true;
										}
										profile.setEmptyHandTool(new ItemStack(m));
										sender.sendMessage(Utils.chat("&aPlayer " + p.getName() + " now mines blocks with the empty hand as if holding " + m));
									} else if (sender instanceof Player pl) {
										ItemStack hand = pl.getInventory().getItemInMainHand();
										if (ItemUtils.isEmpty(hand)){
											profile.setEmptyHandTool(null);
											sender.sendMessage(Utils.chat("&aRemoved empty-hand tool from " + p.getName()));
										} else {
											profile.setEmptyHandTool(hand);
											sender.sendMessage(Utils.chat("&aPlayer " + p.getName() + " now mines blocks with the empty hand as if holding custom " + hand.getType()));
										}
									} else {
										profile.setEmptyHandTool(null);
										sender.sendMessage(Utils.chat("&aRemoved empty-hand tool from " + p.getName()));
									}
								}
								case "aquaaffinity" -> { // vbreaker stats player <player> aquaaffinity true/false
									if (args.length > 4){
										Boolean a = Catch.catchOrElse(() -> Boolean.parseBoolean(args[4]), null);
										if (a == null){
											p.sendMessage(Utils.chat("&cInvalid boolean"));
											return true;
										}
										profile.setNaturalAquaAffinity(a);
										sender.sendMessage(Utils.chat("&aPlayer " + p.getName() + " now" + (!a ? " no longer" : "") + " has natural aqua affinity"));
									} else {
										profile.setNaturalAquaAffinity(false);
										sender.sendMessage(Utils.chat("&aPlayer " + p.getName() + " now no longer has natural aqua affinity"));
									}
								}
								case "airaffinity" -> { // vbreaker stats player <player> airaffinity true/false
									if (args.length > 4){
										Boolean a = Catch.catchOrElse(() -> Boolean.parseBoolean(args[4]), null);
										if (a == null){
											p.sendMessage(Utils.chat("&cInvalid boolean"));
											return true;
										}
										profile.setNaturalAerialAffinity(a);
										sender.sendMessage(Utils.chat("&aPlayer " + p.getName() + " now" + (!a ? " no longer" : "") + " has natural air affinity"));
									} else {
										profile.setNaturalAquaAffinity(false);
										sender.sendMessage(Utils.chat("&aPlayer " + p.getName() + " now no longer has natural air affinity"));
									}
								}
								default -> {
									return false;
								}
							}
							BlockUtils.resetMinerCache(p.getUniqueId());
						}
					} else return false;
				} else return false;
			} else if (args[0].equalsIgnoreCase("give")){
				if (!(sender instanceof Player p)) {
					sender.sendMessage(Utils.chat("&cOnly players may execute this command"));
					return true;
				}
				ItemStack item = SpecializedToolRegistry.getTools().get(args[1]);
				if (ItemUtils.isEmpty(item)){
					p.sendMessage(Utils.chat("&cInvalid item"));
					return true;
				}
				p.getInventory().addItem(item.clone());
				p.sendMessage(Utils.chat("&aItem granted!"));
				return true;
			}
		} else {
			sender.sendMessage(Utils.chat("&e/vbreaking <arg>"));
			sender.sendMessage(Utils.chat("&e        give <tool>"));
			sender.sendMessage(Utils.chat("&8        > Gives you a special plugin tool, the plugin currently only offers a stick allowing you to change the hardness of individual blocks"));
			sender.sendMessage(Utils.chat("&6        stats"));
			sender.sendMessage(Utils.chat("&6            item"));
			sender.sendMessage(Utils.chat("&6                mining_power <value>"));
			sender.sendMessage(Utils.chat("&8                > Changes the base mining power of the held item"));
			sender.sendMessage(Utils.chat("&6                mining_power_exception <block> <value>"));
			sender.sendMessage(Utils.chat("&8                > Changes the base mining power of the held item against a specific block type"));
			sender.sendMessage(Utils.chat("&6                mining_speed_bonus <value>"));
			sender.sendMessage(Utils.chat("&8                > Changes the mining speed bonus of the held item"));
			sender.sendMessage(Utils.chat("&6                hardness_translation <from> <to>"));
			sender.sendMessage(Utils.chat("&8                > Makes the held item mine &7from&8 blocks with the same speed as if it was mining &7to&8 blocks"));
			sender.sendMessage(Utils.chat("&6            player"));
			sender.sendMessage(Utils.chat("&6                mining_speed_bonus <players> [add/set] <value>"));
			sender.sendMessage(Utils.chat("&8                > Grants the given players additional (or reduced) mining speed"));
			sender.sendMessage(Utils.chat("&6                mining_speed_bonus_exception <players> [add/set] <block> <value>"));
			sender.sendMessage(Utils.chat("&8                > Grants the given players additional (or reduced) mining speed against the specific block"));
			sender.sendMessage(Utils.chat("&6                hardness_translation <players> <from> <to>"));
			sender.sendMessage(Utils.chat("&8                > Allows players to mine &7from&8 blocks with the same speed as if they were mining &7to&8 blocks"));
			sender.sendMessage(Utils.chat("&6                empty_hand_tool <players> <value>"));
			sender.sendMessage(Utils.chat("&8                > Allows players to mine blocks with the empty hand as if they were holding the given tool"));
			sender.sendMessage(Utils.chat("&6                aquaaffinity <players> <value>"));
			sender.sendMessage(Utils.chat("&8                > Allows players to mine at normal speeds, even if in water"));
			sender.sendMessage(Utils.chat("&6                airaffinity <players> <value>"));
			sender.sendMessage(Utils.chat("&8                > Allows players to mine at normal speeds, even if not on solid ground"));
		}
		return false;
	}

	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1) return Arrays.asList("give", "stats");
		if (args.length == 2){
			if (args[0].equalsIgnoreCase("give")) return new ArrayList<>(SpecializedToolRegistry.getTools().keySet());
			else if (args[0].equalsIgnoreCase("stats")) return Arrays.asList("item", "player");
		}
		if (args.length >= 3 && (args[1].equalsIgnoreCase("item") || args[1].equalsIgnoreCase("player"))){
			if (args[1].equalsIgnoreCase("item")){
				if (args.length == 3) return Arrays.asList("mining_power", "mining_power_exception", "mining_speed_bonus", "hardness_translation");
				else if (args.length == 4) {
					switch (args[2]){
						case "mining_speed_bonus", "mining_power" -> {
							return Arrays.asList("-0.1", "0.1", "-0.5", "0.5", "-1", "1");
						}
						case "mining_power_exception", "hardness_translation" -> {
							return Arrays.stream(Material.values()).filter(Material::isBlock).map(Material::toString).collect(Collectors.toList());
						}
					}
				} else if (args.length == 5){
					switch (args[2]){
						case "hardness_translation" -> {
							return Arrays.stream(Material.values()).filter(Material::isBlock).map(Material::toString).collect(Collectors.toList());
						}
						case "mining_power_exception" -> {
							return Arrays.asList("-0.1", "0.1", "-0.5", "0.5", "-1", "1");
						}
					}
				}
			} else {
				if (args.length == 3) return null;
				else if (args.length == 4) return Arrays.asList("mining_speed_bonus", "mining_speed_bonus_exception", "hardness_translation", "empty_hand_tool", "aquaaffinity", "airaffinity");
				else if (args.length == 5){
					switch (args[3]){
						case "mining_speed_bonus_exception", "mining_speed_bonus" -> {
							return Arrays.asList("add", "set");
						}
						case "hardness_translation", "empty_hand_tool" -> {
							return Arrays.stream(Material.values()).filter(Material::isBlock).map(Material::toString).collect(Collectors.toList());
						}
						case "aquaaffinity", "airaffinity" -> {
							return Arrays.asList("true", "false");
						}
					}
				} else if (args.length == 6){
					switch (args[3]){
						case "mining_speed_bonus" -> {
							return Arrays.asList("-0.1", "0.1", "-0.5", "0.5", "-1", "1");
						}
						case "hardness_translation", "mining_speed_bonus_exception" -> {
							return Arrays.stream(Material.values()).filter(Material::isBlock).map(Material::toString).collect(Collectors.toList());
						}
					}
				} else if (args.length == 7){
					if (args[3].equalsIgnoreCase("mining_speed_bonus_exception"))
						return Arrays.asList("-0.1", "0.1", "-0.5", "0.5", "-1", "1");
				}
			}
		}
		return null;
	}
}
