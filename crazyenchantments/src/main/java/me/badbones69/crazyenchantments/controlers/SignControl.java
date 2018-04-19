package me.badbones69.crazyenchantments.controlers;

import me.badbones69.crazyenchantments.Methods;
import me.badbones69.crazyenchantments.api.CrazyEnchantments;
import me.badbones69.crazyenchantments.api.currencyapi.Currency;
import me.badbones69.crazyenchantments.api.currencyapi.CurrencyAPI;
import me.badbones69.crazyenchantments.api.enums.Dust;
import me.badbones69.crazyenchantments.api.enums.Scrolls;
import me.badbones69.crazyenchantments.api.events.BuyBookEvent;
import me.badbones69.crazyenchantments.api.objects.FileManager.Files;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SignControl implements Listener {
	
	private static CrazyEnchantments ce = CrazyEnchantments.getInstance();
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if(e.getClickedBlock() == null) return;
		Location Loc = e.getClickedBlock().getLocation();
		Player player = e.getPlayer();
		if(!(e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
		if(e.getClickedBlock().getState() instanceof Sign) {
			FileConfiguration config = Files.CONFIG.getFile();
			for(String l : Files.SIGNS.getFile().getConfigurationSection("Locations").getKeys(false)) {
				String type = Files.SIGNS.getFile().getString("Locations." + l + ".Type");
				World world = Bukkit.getWorld(Files.SIGNS.getFile().getString("Locations." + l + ".World"));
				int x = Files.SIGNS.getFile().getInt("Locations." + l + ".X");
				int y = Files.SIGNS.getFile().getInt("Locations." + l + ".Y");
				int z = Files.SIGNS.getFile().getInt("Locations." + l + ".Z");
				Location loc = new Location(world, x, y, z);
				if(Loc.equals(loc)) {
					if(Methods.isInvFull(player)) {
						if(!Files.MESSAGES.getFile().contains("Messages.Inventory-Full")) {
							player.sendMessage(Methods.color("&cYour inventory is to full. Please open up some space to buy that."));
						}else {
							player.sendMessage(Methods.color(Files.MESSAGES.getFile().getString("Messages.Inventory-Full")));
						}
						return;
					}
					List<String> options = new ArrayList<>();
					options.add("ProtectionCrystal");
					options.add("Scrambler");
					options.add("DestroyDust");
					options.add("SuccessDust");
					options.add("BlackScroll");
					options.add("WhiteScroll");
					options.add("TransmogScroll");
					for(String o : options) {
						if(o.equalsIgnoreCase(type)) {
							if(player.getGameMode() != GameMode.CREATIVE) {
								if(Currency.isCurrency(config.getString("Settings.Costs." + o + ".Currency"))) {
									Currency currency = Currency.getCurrency(config.getString("Settings.Costs." + o + ".Currency"));
									int cost = config.getInt("Settings.Costs." + o + ".Cost");
									if(CurrencyAPI.canBuy(player, currency, cost)) {
										CurrencyAPI.takeCurrency(player, currency, cost);
									}else {
										String needed = (cost - CurrencyAPI.getCurrency(player, currency)) + "";
										if(currency != null) {
											switch(currency) {
												case VAULT:
													player.sendMessage(Methods.getPrefix() + Methods.color(Files.MESSAGES.getFile().getString("Messages.Need-More-Money")
													.replace("%Money_Needed%", needed).replace("%money_needed%", needed)));
													break;
												case XP_LEVEL:
													player.sendMessage(Methods.getPrefix() + Methods.color(Files.MESSAGES.getFile().getString("Messages.Need-More-XP-Lvls")
													.replace("%XP%", needed).replace("%xp%", needed)));
													break;
												case XP_TOTAL:
													player.sendMessage(Methods.getPrefix() + Methods.color(Files.MESSAGES.getFile().getString("Messages.Need-More-Total-XP")
													.replace("%XP%", needed).replace("%xp%", needed)));
													break;
											}
										}
										return;
									}
								}
							}
							if(config.contains("Settings.SignOptions." + o + "Style.Buy-Message")) {
								player.sendMessage(Methods.color(Methods.getPrefix() + config.getString("Settings.SignOptions." + o + "Style.Buy-Message")));
							}
							switch(o) {
								case "ProtectionCrystal":
									player.getInventory().addItem(ProtectionCrystal.getCrystals());
									break;
								case "Scrambler":
									player.getInventory().addItem(Scrambler.getScramblers());
									break;
								case "DestroyDust":
									player.getInventory().addItem(Dust.DESTROY_DUST.getDust());
									break;
								case "SuccessDust":
									player.getInventory().addItem(Dust.SUCCESS_DUST.getDust());
									break;
								case "BlackScroll":
									player.getInventory().addItem(Scrolls.BlACK_SCROLL.getScroll());
									break;
								case "WhiteScroll":
									player.getInventory().addItem(Scrolls.WHITE_SCROLL.getScroll());
									break;
								case "TransmogScroll":
									player.getInventory().addItem(Scrolls.TRANSMOG_SCROLL.getScroll());
									break;
							}
							return;
						}
					}
					for(String cat : config.getConfigurationSection("Categories").getKeys(false)) {
						if(type.equalsIgnoreCase(cat)) {
							Currency currency = null;
							int cost = 0;
							if(player.getGameMode() != GameMode.CREATIVE) {
								if(Currency.isCurrency(config.getString("Categories." + cat + ".Currency"))) {
									currency = Currency.getCurrency(config.getString("Categories." + cat + ".Currency"));
									cost = config.getInt("Categories." + cat + ".Cost");
									if(CurrencyAPI.canBuy(player, currency, cost)) {
										CurrencyAPI.takeCurrency(player, currency, cost);
									}else {
										String needed = (cost - CurrencyAPI.getCurrency(player, currency)) + "";
										if(currency != null) {
											switch(currency) {
												case VAULT:
													player.sendMessage(Methods.getPrefix() + Methods.color(Files.MESSAGES.getFile().getString("Messages.Need-More-Money")
													.replace("%Money_Needed%", needed).replace("%money_needed%", needed)));
													break;
												case XP_LEVEL:
													player.sendMessage(Methods.getPrefix() + Methods.color(Files.MESSAGES.getFile().getString("Messages.Need-More-XP-Lvls")
													.replace("%XP%", needed).replace("%xp%", needed)));
													break;
												case XP_TOTAL:
													player.sendMessage(Methods.getPrefix() + Methods.color(Files.MESSAGES.getFile().getString("Messages.Need-More-Total-XP")
													.replace("%XP%", needed).replace("%xp%", needed)));
													break;
											}
										}
										return;
									}
								}
							}
							ItemStack book = EnchantmentControl.pick(cat);
							book = Methods.addGlow(book);
							String C = config.getString("Categories." + cat + ".Name");
							if(config.contains("Settings.SignOptions.CategoryShopStyle.Buy-Message")) {
								player.sendMessage(Methods.color(Methods.getPrefix() + config.getString("Settings.SignOptions.CategoryShopStyle.Buy-Message")
								.replaceAll("%BookName%", book.getItemMeta().getDisplayName()).replaceAll("%bookname%", book.getItemMeta().getDisplayName())
								.replaceAll("%Category%", C).replaceAll("%category%", C)));
							}
							BuyBookEvent event = new BuyBookEvent(ce.getCEPlayer(player), currency, cost, ce.convertToCEBook(book));
							Bukkit.getPluginManager().callEvent(event);
							player.getInventory().addItem(book);
							return;
						}
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBreak(BlockBreakEvent e) {
		if(!e.isCancelled()) {
			Player player = e.getPlayer();
			Location Loc = e.getBlock().getLocation();
			for(String l : Files.SIGNS.getFile().getConfigurationSection("Locations").getKeys(false)) {
				World world = Bukkit.getWorld(Files.SIGNS.getFile().getString("Locations." + l + ".World"));
				int x = Files.SIGNS.getFile().getInt("Locations." + l + ".X");
				int y = Files.SIGNS.getFile().getInt("Locations." + l + ".Y");
				int z = Files.SIGNS.getFile().getInt("Locations." + l + ".Z");
				Location loc = new Location(world, x, y, z);
				if(Loc.equals(loc)) {
					Files.SIGNS.getFile().set("Locations." + l, null);
					Files.SIGNS.saveFile();
					player.sendMessage(Methods.color(Methods.getPrefix() + Files.MESSAGES.getFile().getString("Messages.Break-Enchantment-Shop-Sign")));
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onSignMake(SignChangeEvent e) {
		Player player = e.getPlayer();
		Location loc = e.getBlock().getLocation();
		FileConfiguration signs = Files.SIGNS.getFile();
		String id = new Random().nextInt(Integer.MAX_VALUE) + "";
		for(int i = 0; i < 200; i++) {
			if(signs.contains("Locations." + id)) {
				id = new Random().nextInt(Integer.MAX_VALUE) + "";
			}else {
				break;
			}
		}
		String line1 = e.getLine(0);
		String line2 = e.getLine(1);
		if(Methods.hasPermission(player, "sign", false)) {
			if(line1.equalsIgnoreCase("{CrazyEnchant}")) {
				for(String cat : Files.CONFIG.getFile().getConfigurationSection("Categories").getKeys(false)) {
					if(line2.equalsIgnoreCase("{" + cat + "}")) {
						e.setLine(0, placeHolders(Files.CONFIG.getFile().getString("Settings.SignOptions.CategoryShopStyle.Line1"), cat));
						e.setLine(1, placeHolders(Files.CONFIG.getFile().getString("Settings.SignOptions.CategoryShopStyle.Line2"), cat));
						e.setLine(2, placeHolders(Files.CONFIG.getFile().getString("Settings.SignOptions.CategoryShopStyle.Line3"), cat));
						e.setLine(3, placeHolders(Files.CONFIG.getFile().getString("Settings.SignOptions.CategoryShopStyle.Line4"), cat));
						signs.set("Locations." + id + ".Type", cat);
						signs.set("Locations." + id + ".World", loc.getWorld().getName());
						signs.set("Locations." + id + ".X", loc.getBlockX());
						signs.set("Locations." + id + ".Y", loc.getBlockY());
						signs.set("Locations." + id + ".Z", loc.getBlockZ());
						Files.SIGNS.saveFile();
						return;
					}
				}
				HashMap<String, String> types = new HashMap<>();
				types.put("Crystal", "ProtectionCrystal");
				types.put("Scrambler", "Scrambler");
				types.put("DestroyDust", "DestroyDust");
				types.put("SuccessDust", "SuccessDust");
				types.put("BlackScroll", "BlackScroll");
				types.put("WhiteScroll", "WhiteScroll");
				types.put("TransmogScroll", "TransmogScroll");
				for(String type : types.keySet()) {
					if(line2.equalsIgnoreCase("{" + type + "}")) {
						type = types.get(type);
						e.setLine(0, Methods.color(Files.CONFIG.getFile().getString("Settings.SignOptions." + type + "Style.Line1")));
						e.setLine(1, Methods.color(Files.CONFIG.getFile().getString("Settings.SignOptions." + type + "Style.Line2")));
						e.setLine(2, Methods.color(Files.CONFIG.getFile().getString("Settings.SignOptions." + type + "Style.Line3")));
						e.setLine(3, Methods.color(Files.CONFIG.getFile().getString("Settings.SignOptions." + type + "Style.Line4")));
						signs.set("Locations." + id + ".Type", type);
						signs.set("Locations." + id + ".World", loc.getWorld().getName());
						signs.set("Locations." + id + ".X", loc.getBlockX());
						signs.set("Locations." + id + ".Y", loc.getBlockY());
						signs.set("Locations." + id + ".Z", loc.getBlockZ());
						Files.SIGNS.saveFile();
						return;
					}
				}
			}
		}
	}
	
	private String placeHolders(String msg, String cat) {
		msg = Methods.color(msg);
		msg = msg.replaceAll("%category%", cat).replaceAll("%Category%", cat);
		msg = msg.replaceAll("%cost%", Files.CONFIG.getFile().getInt("Categories." + cat + ".Cost") + "").replaceAll("%Cost%", Files.CONFIG.getFile().getInt("Categories." + cat + ".Cost") + "");
		msg = msg.replaceAll("%xp%", Files.CONFIG.getFile().getInt("Categories." + cat + ".Cost") + "").replaceAll("%XP%", Files.CONFIG.getFile().getInt("Categories." + cat + ".Cost") + "");
		return msg;
	}
	
}