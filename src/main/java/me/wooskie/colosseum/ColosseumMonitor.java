package me.wooskie.colosseum;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ColosseumMonitor {
	
	private Colosseum plugin;
	private ColosseumCommands colosseumCommands;
	
	final HashMap<Player, Location> lastLoc = new HashMap<Player, Location>();
	final HashMap<Player, int[]> lastHealth = new HashMap<Player, int[]>();

	public ColosseumMonitor(Colosseum parent) {
		plugin = parent;
		colosseumCommands = new ColosseumCommands( plugin );
	}

	public void DropItem(Player player, PlayerDropItemEvent event) {
		if (!event.getItemDrop().getItemStack().getType().equals(Material.ARROW)) {
			event.setCancelled(true);
			player.sendMessage(plugin.chatColor + "Nice try, but you can't farm items this way!");
		}
	}
	
	private void PositionCheck(Player player) {
		
		if (!plugin.inPlay.get(player)) {
			return;
		}

		Location l = player.getLocation();
		
		if (lastLoc.containsKey(player)) {
	
			Location ll = lastLoc.get(player);
			
			// Checks for speeding:
			double xdistance = l.getX() - ll.getX();
			double zdistance = l.getZ() - ll.getZ();
			double xzdistance = Math.sqrt(xdistance*xdistance + zdistance*zdistance);
			if (xzdistance>12) {
				SpeedingFoul(player);
			}
			
			// Checks for floating:
			double height = GetHeight(l);
			if (height > 1.5) {
				Double lastHeight = GetHeight(ll);
				if ((lastHeight>1.5) && (l.getZ()>ll.getZ()-.01)) {
					FloatingFoul(player);
				}
			}
			
			// Checks out of bounds
			if (plugin.colosseumLocs.containsKey("quit")) {
				if (l.distance(plugin.colosseumLocs.get("quit")) > 50) {
					OOBFoul(player);
				}
			}
			
			
		}
		
		// Stores current location for next check:
		lastLoc.put(player, l);
	}



	private double GetHeight(Location l) {
		// This method calculates the player's height above the ground:
		World w = l.getWorld();
		
		// Get's y coordinate for highest possible "ground" block among the 
		// four blocks player could stand on based on XZ location:
		int h1 = w.getHighestBlockYAt((int) Math.round(l.getX()), (int) Math.round(l.getZ()));
		int h2 = w.getHighestBlockYAt((int) Math.round(l.getX()), (int) Math.round(l.getZ())-1);
		int h3 = w.getHighestBlockYAt((int) Math.round(l.getX())-1, (int) Math.round(l.getZ()));
		int h4 = w.getHighestBlockYAt((int) Math.round(l.getX())-1, (int) Math.round(l.getZ())-1);
		int h = Math.max(Math.max(h1, h2), Math.max(h3, h4));

		// Returns player's height above highest possible "ground" block:
		double height = l.getY() - (double) h;
		return height;
	}
	
	private void FloatingFoul(Player player) {
		if (plugin.battleInProgress) {
			colosseumCommands.AnnounceAll("Foul! " + player.getName() + " detected floating!");
			player.damage(4);
		} else {
			player.sendMessage(ChatColor.RED + "WARNING! " + ChatColor.WHITE + "You have been detected floating. Turn off flymod!");
		}
	}

	private void SpeedingFoul(Player player) {
		if (plugin.battleInProgress) {
			colosseumCommands.AnnounceAll("Foul! " + player.getName() + " detected speeding!");
			player.damage(4);
		} else {
			player.sendMessage(ChatColor.RED + "WARNING! " + ChatColor.WHITE + "You have been detected speeding. Turn off flymod!");
		}
	}
	
	private void OOBFoul(Player player) {
		colosseumCommands.AnnounceAll("Foul! " + player.getName() + " is out of bounds!");
		player.damage(4);
	}
	
	private void HelmetCheck(Player player) {
		if (!plugin.inPlay.containsKey(player)) {
			return;
		}
		ItemStack redhelmet = new ItemStack(35, 1, (short) 14);
		ItemStack bluehelmet = new ItemStack(35, 1, (short) 11);
		if ((!player.getInventory().getHelmet().toString().equals(redhelmet.toString())) && 
		   (!player.getInventory().getHelmet().toString().equals(bluehelmet.toString()))) {
			HelmetFoul(player);
		}
		player.getInventory().remove(Material.WOOL);
	}
	
	private void HelmetFoul(Player player) {
		if (plugin.battleInProgress) {
			colosseumCommands.AnnounceAll("Foul! " + player.getName() + "removed his helmet and has been penalized two hearts.");
			player.damage(4);
		} else {
			player.sendMessage(ChatColor.RED + "WARNING! " + ChatColor.WHITE + "Removal of helmet is not allowed.");
		}
		ItemStack redhelmet = new ItemStack(35, 1, (short) 14);
		ItemStack bluehelmet = new ItemStack(35, 1, (short) 11);
		if (plugin.roster.get(player).equals("red")) {
			player.getInventory().setHelmet(redhelmet);
		} else if (plugin.roster.get(player).equals("blue")) {
			player.getInventory().setHelmet(bluehelmet);
		}
	}

	private void HealthCheck(Player player) {
		int h = player.getHealth();
		if (h==20) {
			return;
		}
		int restTime = 15;
		int[] lh = {0,0};
		if (lastHealth.containsKey(player)) {
			lh = lastHealth.get(player);
			if (h >= lh[0]) {
				lh[1]++;
				if (lh[1] >= restTime) {
					if (lh[1] == restTime) {
						colosseumCommands.AnnounceAll(player.getName() + " has begun restoring health!");
					}
					h++;
					player.setHealth(h);
					if (h==20) {
						lh[1] = 0;
					}
				}
			} else { lh[1] = 0; }
		}
		lh[0]=h;
		lastHealth.put(player, lh);
	}
	
	public void Monitor() {
		for (Player player : plugin.roster.keySet()) {
			HelmetCheck(player);
			PositionCheck(player);
			HealthCheck(player);
		}
	}
	
	public void StartMonitor() {
		lastLoc.clear();
		lastHealth.clear();
		plugin.monitorID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin) plugin, new Runnable() {
			public void run() { Monitor(); }	
		}, 40L, 40L);
	}

}