package me.wooskie.colosseum;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Colosseum extends JavaPlugin{

	Logger log = Logger.getLogger("Minecraft");
	
	static ColosseumEntityListener eListener;
	static ColosseumPlayerListener pListener;
	static ColosseumCommands colosseumCommands;
	static ColosseumActions colosseumActions;
	static ColosseumData colosseumData;
	static ColosseumMonitor colosseumMonitor;
	
	public HashMap<Player, Boolean> inPlay = new HashMap<Player, Boolean>();
	public HashMap<Player, String> roster = new HashMap<Player, String>();
	public HashMap<Player, ItemStack[]> initialInvItemStack = new HashMap<Player, ItemStack[]>();
	public HashMap<Player, ItemStack[]> initialArmItemStack = new HashMap<Player, ItemStack[]>();
	public HashMap<String, Location> colosseumLocs = new HashMap<String, Location>();
	public HashMap<Sign, String> colosseumSigns = new HashMap<Sign, String>();
	public HashMap<Location, String> colosseumGates = new HashMap<Location, String>();
	public Boolean redReady = false;
	public Boolean blueReady = false;
	public Boolean battleInProgress = false;
	public Boolean colosseumInUse = false;
	public ChatColor chatColor = ChatColor.YELLOW;
	public String redString = ChatColor.RED + "RED" + chatColor;
	public String blueString = ChatColor.BLUE + "BLUE" + chatColor;
	public int monitorID;
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (commandLabel.equalsIgnoreCase("pvp")) {
			Player player = (Player) sender;
			if (args.length < 2) {
				return false;
			}
			if (args[0].equalsIgnoreCase("set")) {
				if (args.length > 2) {
					if (args[1].equalsIgnoreCase("location")) {
						return colosseumData.SetLocation(player, args[2]);
					} else if (args[1].equalsIgnoreCase("sign")) {
						return colosseumData.SetSign(player, args[2]);
					} else if (args[1].equalsIgnoreCase("gate")) {
						return colosseumData.SetGate(player, args[2]);
					} 
				}
			} else if (args[0].equalsIgnoreCase("clear")) {
				if (args[1].equalsIgnoreCase("allsigns")) {
					return colosseumData.ClearAllSigns(player);
				} else if (args[1].equalsIgnoreCase("sign")) {
					return colosseumData.ClearSign(player);
				} else if (args[1].equalsIgnoreCase("locations")) {
					colosseumLocs.clear();
					colosseumData.SaveLocations();
					player.sendMessage("Locations cleared.");
					return true;
				} else if (args[1].equalsIgnoreCase("gates"))
					colosseumCommands.OpenGates("inner");
					colosseumCommands.OpenGates("outer");
					colosseumGates.clear();
					colosseumData.SaveGates();
					player.sendMessage("Gates cleared.");
					return true;
			}
		}
		return false;
	}

	public void onEnable() {
		
		eListener = new ColosseumEntityListener( this );
		pListener = new ColosseumPlayerListener( this );
		colosseumCommands = new ColosseumCommands( this );
		colosseumActions = new ColosseumActions( this );
		colosseumData = new ColosseumData( this );
		colosseumMonitor = new ColosseumMonitor( this );
		
		colosseumCommands.UpdateTeamSigns();
		
		log.info("Colosseum 0.1 enabled.");
		PluginManager pm = getServer().getPluginManager();	
		pm.registerEvent(Event.Type.ENTITY_DEATH, eListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, eListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, pListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, pListener, Priority.Normal, this);
	}
	
	public void onDisable() {
		log.info("Colosseum 0.1 disabled.");
	}
	
	
}
