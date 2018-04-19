package me.badbones69.crazyenchantments.controlers;

import me.badbones69.crazyenchantments.Methods;
import me.badbones69.crazyenchantments.api.CrazyEnchantments;
import me.badbones69.crazyenchantments.api.enums.EnchantmentType;
import me.badbones69.crazyenchantments.api.enums.Scrolls;
import me.badbones69.crazyenchantments.api.objects.CEBook;
import me.badbones69.crazyenchantments.api.objects.CEnchantment;
import me.badbones69.crazyenchantments.api.objects.FileManager.Files;
import org.apache.commons.lang.WordUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ScrollControl implements Listener {
	
	private static CrazyEnchantments ce = CrazyEnchantments.getInstance();
	
	@EventHandler
	public void onScrollUse(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		Inventory inv = e.getInventory();
		ItemStack item = e.getCurrentItem();
		ItemStack scroll = e.getCursor();
		if(inv != null) {
			if(item == null) item = new ItemStack(Material.AIR);
			if(scroll == null) scroll = new ItemStack(Material.AIR);
			if(item.getType() != Material.AIR && scroll.getType() != Material.AIR) {
				if(inv.getType() == InventoryType.CRAFTING) {
					if(e.getRawSlot() < 9) {
						return;
					}
				}
				if(scroll.isSimilar(Scrolls.TRANSMOG_SCROLL.getScroll())) {
					if(player.getGameMode() == GameMode.CREATIVE && scroll.getAmount() > 1) {
						player.sendMessage(Methods.getPrefix() + Methods.color("&cPlease unstack the scrolls for them to work."));
						return;
					}
					if(ce.hasEnchantments(item)) {
						if(item.isSimilar(orderEnchantments(item.clone()))) {
							return;
						}
						e.setCancelled(true);
						ItemStack it = orderEnchantments(item);
						it = Methods.addGlow(it);
						e.setCurrentItem(it);
						player.setItemOnCursor(Methods.removeItem(scroll));
						player.updateInventory();
						return;
					}
				}
				if(scroll.isSimilar(Scrolls.WHITE_SCROLL.getScroll())) {
					if(player.getGameMode() == GameMode.CREATIVE && scroll.getAmount() > 1) {
						player.sendMessage(Methods.getPrefix() + Methods.color("&cPlease unstack the scrolls for them to work."));
						return;
					}
					if(!Methods.isProtected(item)) {
						ArrayList<Material> types = new ArrayList<>(EnchantmentType.ALL.getItems());
						if(types.contains(item.getType())) {
							e.setCancelled(true);
							e.setCurrentItem(Methods.addLore(item, Files.CONFIG.getFile().getString("Settings.WhiteScroll.ProtectedName")));
							player.setItemOnCursor(Methods.removeItem(scroll));
							return;
						}
					}
				}
				if(scroll.isSimilar(Scrolls.BlACK_SCROLL.getScroll())) {
					if(player.getGameMode() == GameMode.CREATIVE && scroll.getAmount() > 1) {
						player.sendMessage(Methods.getPrefix() + Methods.color("&cPlease unstack the scrolls for them to work."));
						return;
					}
					HashMap<String, Integer> lvl = new HashMap<>();
					ArrayList<CEnchantment> enchants = new ArrayList<>();
					Boolean i = false;
					if(ce.hasEnchantments(item)) {
						for(CEnchantment en : ce.getRegisteredEnchantments()) {
							if(ce.hasEnchantment(item, en)) {
								enchants.add(en);
								lvl.put(en.getName(), ce.getPower(item, en));
								i = true;
							}
						}
					}
					if(i) {
						e.setCancelled(true);
						player.setItemOnCursor(Methods.removeItem(scroll));
						if(Files.CONFIG.getFile().getBoolean("Settings.BlackScroll.Chance-Toggle")) {
							if(!Methods.randomPicker(Files.CONFIG.getFile().getInt("Settings.BlackScroll.Chance"), 100)) {
								player.sendMessage(Methods.getPrefix() + Methods.color(Files.MESSAGES.getFile().getString("Messages.Black-Scroll-Unsuccessful")));
								return;
							}
						}
						CEnchantment enchantment = pickEnchant(enchants);
						e.setCurrentItem(ce.removeEnchantment(item, enchantment));
						CEBook book = new CEBook(enchantment, lvl.get(enchantment.getName()), 1);
						player.getInventory().addItem(book.buildBook());
						player.updateInventory();
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		ItemStack item = Methods.getItemInHand(player);
		if(item != null) {
			if(Methods.isSimilar(item, Scrolls.BlACK_SCROLL.getScroll())) {
				e.setCancelled(true);
				player.sendMessage(Methods.getPrefix() + Methods.color(Files.MESSAGES.getFile().getString("Messages.Right-Click-Black-Scroll")));
			}else if(Methods.isSimilar(item, Scrolls.WHITE_SCROLL.getScroll()) || Methods.isSimilar(item, Scrolls.TRANSMOG_SCROLL.getScroll())) {
				e.setCancelled(true);
			}
		}
	}
	
	public static ItemStack orderEnchantments(ItemStack item) {
		HashMap<String, Integer> enchants = new HashMap<>();
		HashMap<String, Integer> categories = new HashMap<>();
		List<String> order = new ArrayList<>();
		ArrayList<String> enchantments = new ArrayList<>();
		for(CEnchantment en : ce.getItemEnchantments(item)) {
			enchantments.add(en.getName());
		}
		for(String ench : enchantments) {
			int top = 0;
			if(ce.getEnchantmentFromName(ench) != null) {
				for(String cat : ce.getEnchantmentFromName(ench).getCategories()) {
					if(top < ce.getCategoryRarity(cat)) {
						top = ce.getCategoryRarity(cat);
					}
				}
				enchants.put(ench, ce.getPower(item, ce.getEnchantmentFromName(ench)));
				ce.removeEnchantment(item, ce.getEnchantmentFromName(ench));
			}
			categories.put(ench, top);
			order.add(ench);
		}
		order = orderInts(order, categories);
		ItemMeta m = item.getItemMeta();
		ArrayList<String> lore = new ArrayList<>();
		for(String ench : order) {
			if(ce.getEnchantmentFromName(ench) != null) {
				CEnchantment en = ce.getEnchantmentFromName(ench);
				lore.add(en.getColor() + en.getCustomName() + " " + ce.convertPower(enchants.get(ench)));
			}
		}
		if(m.hasLore()) {
			lore.addAll(m.getLore());
		}
		m.setLore(lore);
		String name = Methods.color("&b" + WordUtils.capitalizeFully(item.getType().toString().replaceAll("_", " ").toLowerCase()));
		String enchs = Files.CONFIG.getFile().getString("Settings.TransmogScroll.Amount-of-Enchantments");
		if(m.hasDisplayName()) {
			name = m.getDisplayName();
			for(int i = 0; i <= 100; i++) {
				if(m.getDisplayName().endsWith(Methods.color(enchs.replaceAll("%Amount%", i + "").replaceAll("%amount%", i + "")))) {
					name = m.getDisplayName().substring(0, m.getDisplayName().length() - (enchs.replaceAll("%Amount%", i + "").replaceAll("%amount%", i + "")).length());
				}
			}
		}
		int amount = order.size();
		if(Files.CONFIG.getFile().getBoolean("Settings.TransmogScroll.Count-Vanilla-Enchantments")) {
			for(Enchantment ench : item.getEnchantments().keySet()) {
				try {
					if(Methods.getEnchantments().contains(ench.getName())) {
						amount++;
					}
				}catch(Exception e) {
				}
			}
		}
		if(Files.CONFIG.getFile().getBoolean("Settings.TransmogScroll.Amount-Toggle")) {
			m.setDisplayName(name + Methods.color(enchs.replaceAll("%Amount%", amount + "").replaceAll("%amount%", amount + "")));
		}
		item.setItemMeta(m);
		return item;
	}
	
	public static List<String> orderInts(List<String> list, final Map<String, Integer> map) {
		list.sort((a1, a2) -> {
			Integer string1 = map.get(a1);
			Integer string2 = map.get(a2);
			return string2.compareTo(string1);
		});
		return list;
	}
	
	private CEnchantment pickEnchant(List<CEnchantment> enchants) {
		Random i = new Random();
		return enchants.get(i.nextInt(enchants.size()));
	}
}