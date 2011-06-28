package me.wooskie.colosseum;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ColosseumCommands {

	private Colosseum plugin;
	
	public ColosseumCommands(Colosseum parent) {
		plugin = parent;
	}
	
	public void JoinTeam(Player player, String team) {
		
		// Checks to make sure player can join team:
		int teamSize = 0;
		boolean redundant = false;
		if (plugin.battleInProgress) {
			player.sendMessage("Battle in progress. Teams are fixed.");
			return;
		} else {	
			for (Player key : plugin.roster.keySet()) {
				if (plugin.roster.get(key).equals(team)) {
					teamSize++;
					if (key.equals(player)) {
						redundant = true;
					}
				}
			}
			if (redundant) {
				Leave(player);
				return;
			} else if (teamSize >= 3) {
				player.sendMessage("The " + team + " team is already full.");
				return;
			}
		}
		
		// Adds player to team:
		plugin.roster.put(player, team);
		if (plugin.colosseumLocs.containsKey(team)) {
			player.teleport(plugin.colosseumLocs.get(team));
		} else { player.sendMessage("Red starting location not yet set."); }
		player.setHealth(20);
		ToPvpInventory(player, team);
		UpdateTeamSigns();
		
		// Announces new teams:
		String teamString = null;
		if (team.equals("red")) { teamString = plugin.redString; }
		else if (team.equals("blue")) { teamString = plugin.blueString; }
		AnnounceAll(player.getDisplayName() + " has joined the " + teamString + " team.");
		
		// Additional script relevant to player joining Colosseum:
		if (!plugin.colosseumInUse) {
			CloseGates("inner");
			CloseGates("outer");
			Colosseum.colosseumMonitor.StartMonitor();
		}
		plugin.colosseumInUse = true;
		plugin.inPlay.put(player, true);
		plugin.redReady = false;
		SetSignsToNotReady("redready");
		plugin.blueReady = false;
		SetSignsToNotReady("blueready");
		
	}
	
	public void Leave(Player player) {
		if (plugin.colosseumLocs.containsKey("quit")) {
			player.teleport(plugin.colosseumLocs.get("quit"));
		} else { player.sendMessage("Quit location not yet set."); }
		ToStandardInventory(player);
		plugin.roster.remove(player);
		if (!VictoryCheck()) {
			UpdateTeamSigns();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void ToPvpInventory(Player player, String team) {
	
		// Identify and remove arrows from inventory:
		HashMap<Integer, ? extends ItemStack> arrows = player.getInventory().all(Material.ARROW);
		player.getInventory().remove(Material.ARROW);
		
		// Store player inventory and armor in HashMap:
		plugin.initialInvItemStack.put(player, player.getInventory().getContents());
		plugin.initialArmItemStack.put(player, player.getInventory().getArmorContents());
		
		// Clear player inventory and armor:
		player.getInventory().clear();
		player.getInventory().setHelmet(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
		player.getInventory().setBoots(null);
		
		// Give player bow and stone sword:
		player.getInventory().addItem(new ItemStack(Material.BOW, 1, (short) 0));
		player.getInventory().addItem(new ItemStack(Material.GOLD_SWORD, 1, (short) 0));
		
		// Give helmet:
		short color = 0;
		if (team.equals("red")) { color = 14; }
		else if (team.equals("blue")) { color = 11; }
		player.getInventory().setHelmet(new ItemStack(35, 1, color));
		
		// Restore arrows:
		for (int slot : arrows.keySet()) {
			player.getInventory().addItem(arrows.get(slot));
		}
		
		player.updateInventory(); // deprecated command, but needed for now to ensure client properly reads server change.
	}

	@SuppressWarnings("deprecation")
	public void ToStandardInventory(Player player) {
		// Halts method if player not on roster:
		if (!plugin.roster.containsKey(player)) {
			return;
		}
		
		// Set player health to full
		player.setHealth(20);
		
		// Identify arrows in inventory:
		HashMap<Integer, ? extends ItemStack> arrows = player.getInventory().all(Material.ARROW);
		
		// Restore original arrowless inventory and armor:
		player.getInventory().setContents(plugin.initialInvItemStack.get(player));
		player.getInventory().setArmorContents(plugin.initialArmItemStack.get(player));
		
		// Restore arrows:
		for (int slot : arrows.keySet()) {
			player.getInventory().addItem(arrows.get(slot));
		}
		player.updateInventory(); // deprecated command, but needed for now to ensure client properly reads server change.
	}
	
	public void TeamReady(String team) {
		
		// Sets team to ready:
		if (team.equals("red")) {
			plugin.redReady = true;
			AnnounceAll(plugin.redString + " team is ready!");
			for (Sign sign : plugin.colosseumSigns.keySet()) {
				if (plugin.colosseumSigns.get(sign).equals("redready")) {
					SetSignsToReady("redready");
				}
			}
		}
		else if (team.equals("blue")) { 
			plugin.blueReady = true; 
			AnnounceAll(plugin.blueString + " team is ready!");
			for (Sign sign : plugin.colosseumSigns.keySet()) {
				if (plugin.colosseumSigns.get(sign).equals("blueready")) {
					SetSignsToReady("blueready");
				}
			}
		}
		
		// Starts battle if both teams ready:
		if (plugin.redReady && plugin.blueReady) {
			plugin.battleInProgress = true;
			AnnounceAll("Let the battle begin!");
			OpenGates("inner");
		}
	}

	public void SetSignsToReady(String signID) {
		for (Sign sign : plugin.colosseumSigns.keySet()) {
			if (plugin.colosseumSigns.get(sign).equals(signID)) {
				sign.setLine(1, ChatColor.GREEN + "READY");
				sign.update();
			}
		}
	}
	
	public void SetSignsToNotReady(String signID) {
		for (Sign sign : plugin.colosseumSigns.keySet()) {
			if (plugin.colosseumSigns.get(sign).equals(signID)) {
				sign.setLine(1, ChatColor.RED + "NOT READY");
				sign.update();
			}
		}
	}
	
	public void TeamNotReady(String team) {
		
		// Sets team to not ready:
		if (team.equals("red")) { 
			plugin.redReady = false;
			AnnounceAll(plugin.redString + " team is no longer ready.");
			for (Sign sign : plugin.colosseumSigns.keySet()) {
				if (plugin.colosseumSigns.get(sign).equals("redready")) {
					SetSignsToNotReady("redready");
					CloseGates("inner"); // temporary;
				}
			}
		}
		else if (team.equals("blue")) { 
			plugin.blueReady = false; 
			AnnounceAll(plugin.blueString + " team is no longer ready.");
			for (Sign sign : plugin.colosseumSigns.keySet()) {
				if (plugin.colosseumSigns.get(sign).equals("blueready")) {
					SetSignsToNotReady("blueready");
					CloseGates("inner"); //temporary;
				}
			}
		}
	}
	
	public void PlayerDeath(Player player) {
		
		plugin.inPlay.put(player, false);
		
		// Announces player death:
		String team = plugin.roster.get(player);
		AnnounceAll(plugin.chatColor + player.getDisplayName() + " on the " + 
			((team.equals("red")) ? plugin.redString : plugin.blueString) + " team has died!");

		// Checks if victory conditions have been met:
		if (VictoryCheck()) {
			return;
		} else {
		// Teleports dead player to holding area:
			if (plugin.colosseumLocs.containsKey("holding")) {
				player.teleport(plugin.colosseumLocs.get("holding"));
			} else {
				player.sendMessage("Error: dead player location not yet set.");
			}
			plugin.inPlay.put(player, false);
		}
	}
	
	public boolean VictoryCheck() {
		// Checks if victory conditions have been met:
		int redLives = 0;
		int blueLives = 0;
		
		for (Player key : plugin.roster.keySet()) {
			if (plugin.inPlay.get(key)) {
				if (plugin.roster.get(key) == "red") {
					redLives++;
				} else if (plugin.roster.get(key) == "blue") {
					blueLives++;
				}
			}
		}
		
		if ((blueLives==0) && (redLives==0)) {
			Victory("tie",plugin.roster);
			return true;
		} else if (blueLives==0) {
			Victory("red",plugin.roster);
			return true;
		} else if (redLives==0) {
			Victory("blue",plugin.roster);
			return true;
		} else {
			return false;
		}
	}
	
	public void Victory(final String winningTeam, final HashMap<Player, String> roster) {
		
		if (winningTeam.equals("red")) {
			AnnounceAll("Red team wins!");
		} else if (winningTeam.equals("blue")) {
			AnnounceAll("Blue team wins!");
		} else if (winningTeam.equals("tie")) {
			AnnounceAll("Tie match. How exactly did you do that?");
		}
		
		for (Player key : roster.keySet()) {
			if (roster.get(key).equals(winningTeam)) {
				if (plugin.colosseumLocs.containsKey("victory")) {
					key.teleport(plugin.colosseumLocs.get("victory"));
				} else {
					key.sendMessage("Error: victory location not yet set.");
				}
			} else {
				if (plugin.colosseumLocs.containsKey("loss")) {
					key.teleport(plugin.colosseumLocs.get("loss"));
				} else {
					key.sendMessage("Error: loss location not yet set.");
				}
			}
			ToStandardInventory(key);
		}
		
		// Reset colosseum:
		plugin.battleInProgress = false;
		plugin.colosseumInUse = false;
		plugin.roster.clear();
		plugin.inPlay.clear();
		plugin.initialArmItemStack.clear();
		plugin.initialInvItemStack.clear();
		UpdateTeamSigns();
		OpenGates("inner");
		OpenGates("outer");
		//plugin.getServer().getScheduler().cancelTask(plugin.checkerID);
		
	}
	
	public void PlayerQuit(Player player) {
		String team = plugin.roster.get(player);
		AnnounceAll(plugin.chatColor + player.getDisplayName() + " on the " + 
			((team.equals("red")) ? plugin.redString : plugin.blueString) + " team has logged out!");
		VictoryCheck();
	}
	
	public String[] GenerateRoster() {
		
		String[] teamList = {"Red team: " + ChatColor.RED, "Blue Team: " + ChatColor.BLUE};
		
		int rednum = 0;
		int bluenum = 0;
		for (Player key : plugin.roster.keySet()) {
			if (plugin.roster.get(key).equals("red")) {
				rednum++;
				if (rednum == 1) {
					teamList[0] += key.getDisplayName();
				} else {
					teamList[0] += ", " + key.getDisplayName();
				}
			} else if (plugin.roster.get(key).equals("blue")) {
				bluenum++;
				if (bluenum == 1) {
					teamList[1] += key.getDisplayName();
				} else {
					teamList[1] += ", " + key.getDisplayName();
				}
			}
		}
		
		return teamList;

	}
	
	public void AnnounceAll(String message) {
		// Announces message to all players using Colosseum:
		for (Player key : plugin.roster.keySet()) {
			key.sendMessage(plugin.chatColor + message);
		}
	}
	
	public void SignCommandHandler(Sign sign, Player player) {
		
		String signType = plugin.colosseumSigns.get(sign);
		
		// Calls proper method based on sign clicked:
		if (signType.equals("redteam")) { JoinTeam(player, "red"); }
		else if (signType.equals("blueteam")) { JoinTeam(player, "blue"); }
		else if (signType.equals("redready")) { 
			if (plugin.battleInProgress) {
				player.sendMessage(plugin.chatColor + "Battle in progress.");
				return;
			}
			if (plugin.roster.containsKey(player)) {
				if (plugin.roster.get(player).equals("red")) {
					if (plugin.redReady) { TeamNotReady("red"); } 
					else { TeamReady("red"); }
				} else {
					player.sendMessage(plugin.chatColor + "You are not on the red team.");
				}
			} else {
				player.sendMessage(plugin.chatColor + "You must first join a team.");
			}
		}
		else if (signType.equals("blueready")) { 
			if (plugin.battleInProgress) {
				player.sendMessage(plugin.chatColor + "Battle in progress.");
				return;
			}
			if (plugin.roster.containsKey(player)) {
				if (plugin.roster.get(player).equals("blue")) {
					if (plugin.blueReady) { TeamNotReady("blue"); } 
					else { TeamReady("blue"); }
				} else {
					player.sendMessage(plugin.chatColor + "You are not on the blue team.");
				}
			} else {
				player.sendMessage(plugin.chatColor + "You must first join a team.");
			}
		}
	}

	public void UpdateTeamSigns() {
		int redLine;
		int blueLine;
		for (Sign sign : plugin.colosseumSigns.keySet()) {
			redLine = 1;
			blueLine = 1;
			if (plugin.colosseumSigns.get(sign).equals("redteam")) {
				for (Player player : plugin.roster.keySet()) {
					if (plugin.roster.get(player).equals("red")) {
						sign.setLine(redLine, player.getDisplayName());
						redLine++;
					}
				}
				while (redLine <= 3) {
					sign.setLine(redLine, "");
					redLine++;
				}
				sign.update();
			} else if (plugin.colosseumSigns.get(sign).equals("blueteam")) {
				for (Player player : plugin.roster.keySet()) {
					if (plugin.roster.get(player).equals("blue")) {
						sign.setLine(blueLine, player.getDisplayName());
						blueLine++;
					}
				}
				while (blueLine <= 3) {
					sign.setLine(blueLine, "");
					blueLine++;
				}
				sign.update();
			}
		}
	}
	
	public void OpenGates(String string) {
		for (Location l : plugin.colosseumGates.keySet()) {
			if (plugin.colosseumGates.get(l).equals(string)) {
				l.getBlock().setType(Material.AIR);
			}
		}
	}
	
	public void CloseGates(String string) {
		for (Location l : plugin.colosseumGates.keySet()) {
			if (plugin.colosseumGates.get(l).equals(string)) {
				l.getBlock().setType(Material.FENCE);
			}
		}
	}
	
	
}
