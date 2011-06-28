package me.wooskie.colosseum;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class ColosseumData {

	static private Colosseum plugin;
	static private Server server;
	static private File locFile;
	static private File signFile;
	static private File gatesFile;
	static private String locFilename = "colosseum.locations";
	static private String signFilename = "colosseum.signs";
	static private String gatesFilename = "colosseum.gates";
	
	public ColosseumData(Colosseum parent) {
		plugin = parent;
	    server = plugin.getServer();	    
	    File dataFolder = plugin.getDataFolder();
	    
	    if (!dataFolder.exists()) {
	    	dataFolder.mkdirs();
	    }
	    locFile = new File(dataFolder, locFilename);
	    signFile = new File(dataFolder, signFilename);
	    gatesFile = new File(dataFolder, gatesFilename);
	    LoadLocations();
	    LoadSigns();
	    LoadGates();
	}

	public void SaveLocations() {
		
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(locFile)));
			for (String key : plugin.colosseumLocs.keySet()) {
				Location loc = plugin.colosseumLocs.get(key);
				bw.write(String.format("%s,%s,%f,%f,%f,%f,%f%n", key, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
			}
			bw.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void LoadLocations() {
		plugin.colosseumLocs.clear();
		String inputLine;
		if (locFile.exists()) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(locFile)));
				while ((inputLine = br.readLine()) != null) {
					String splits[] = inputLine.split(",", 7);

					plugin.colosseumLocs.put(splits[0], new Location(
					server.getWorld(splits[1]),
					Double.parseDouble(splits[2]),
					Double.parseDouble(splits[3]),
					Double.parseDouble(splits[4]),
					Float.parseFloat(splits[5]),
					Float.parseFloat(splits[6]))
					);
				}
				br.close();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void SaveSigns() {
		
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(signFile)));
			for (Sign key : plugin.colosseumSigns.keySet()) {
				Location loc = key.getBlock().getLocation();
				bw.write(String.format("%s,%s,%f,%f,%f%n", plugin.colosseumSigns.get(key), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));
			}
			bw.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void LoadSigns() {
		plugin.colosseumSigns.clear();
		String inputLine;
		if (signFile.exists()) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(signFile)));
				while ((inputLine = br.readLine()) != null) {
					String splits[] = inputLine.split(",", 5);
					Location l = new Location(
							server.getWorld(splits[1]),
							Double.parseDouble(splits[2]),
							Double.parseDouble(splits[3]),
							Double.parseDouble(splits[4])
							);
					Sign sign = (Sign) l.getBlock().getState();
					plugin.colosseumSigns.put(sign, splits[0]);
				}
				br.close();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean SetLocation(Player player, String string) {
		Location l = player.getLocation();
		if (string.equals("red")) {
			plugin.colosseumLocs.put(string, l);
		} else if (string.equals("blue")) {
			plugin.colosseumLocs.put(string, l);
		} else if (string.equals("holding")) {
			plugin.colosseumLocs.put(string, l);
		} else if (string.equals("victory")) {
			plugin.colosseumLocs.put(string, l);
		} else if (string.equals("loss")) {
			plugin.colosseumLocs.put(string, l);
		} else if (string.equals("quit")) {
			plugin.colosseumLocs.put(string, l);
		} else {
			player.sendMessage("Not a valid location name.");
			return true;
		}
		SaveLocations();
		player.sendMessage("Location registered.");
		return true;
	}
	
	public boolean SetSign(Player player, String string) {
		Block block = player.getTargetBlock(null, 50);
		if (!(block.getState() instanceof Sign)) {
			player.sendMessage("Must be targeting sign.");
			return true;
		} else {
			Sign sign = (Sign) block.getState();
			if (string.equals("redteam")) {
				plugin.colosseumSigns.put(sign, string);
				sign.setLine(0, ChatColor.DARK_RED + "Red Team:");
				sign.setLine(1, "");
				sign.setLine(2, "");
				sign.setLine(3, "");
				sign.update();
			} else if (string.equals("blueteam")) {
				plugin.colosseumSigns.put(sign, string);
				sign.setLine(0, ChatColor.DARK_BLUE + "Blue Team:");
				sign.setLine(1, "");
				sign.setLine(2, "");
				sign.setLine(3, "");
				sign.update();
			} else if (string.equals("redready")) {
				plugin.colosseumSigns.put(sign, string);
				sign.setLine(0, "");
				sign.setLine(1, ChatColor.RED + "Not Ready");
				sign.setLine(2, "(Red team");
				sign.setLine(3, "ready status)");
				sign.update();
			} else if (string.equals("blueready")) {
				plugin.colosseumSigns.put(sign, string);
				sign.setLine(0, "");
				sign.setLine(1, ChatColor.RED + "Not Ready");
				sign.setLine(2, "(Blue team");
				sign.setLine(3, "ready status)");
				sign.update();
			} else {
				player.sendMessage("Not a valid sign name.");
				return true;
			}
			SaveSigns();
			player.sendMessage("Sign registered.");
			return true;
		}
	}

	public boolean ClearSign(Player player) {
		Block block = player.getTargetBlock(null, 50);
		if (!(block.getState() instanceof Sign)) {
			player.sendMessage("Must be targeting sign.");
			return true;
		}
		Sign sign = (Sign) block.getState();
		for (Sign key : plugin.colosseumSigns.keySet()) {
			if (key.getBlock() == sign.getBlock()) {
				player.sendMessage("'" + plugin.colosseumSigns.get(key) + "' sign removed.");
				sign.setLine(0, "");
				sign.setLine(1, "");
				sign.setLine(2, "");
				sign.setLine(3, "");
				sign.update();
				plugin.colosseumSigns.remove(key);
				SaveSigns();
				return true;
			}
		}
		player.sendMessage("Sign not registered, no action taken.");
		return true;
	}
	
	public boolean ClearAllSigns(Player player) {
		for (Sign sign : plugin.colosseumSigns.keySet()) {
				sign.setLine(0, "");
				sign.setLine(1, "");
				sign.setLine(2, "");
				sign.setLine(3, "");
				sign.update();
		}
		plugin.colosseumSigns.clear();
		SaveSigns();
		player.sendMessage("Signs cleared.");
		return true;
	}

	public boolean SetGate(Player player, String string) {
		Block block = player.getTargetBlock(null, 50);
		if (!block.getType().equals(Material.FENCE)) {
			player.sendMessage("Must target top left fence block.");
			return true;
		}
		if (!(string.equals("inner") || string.equals("outer"))) {
			player.sendMessage("Must indicate either 'inner' or 'outer' gate.");
			return true;
		}
		
		Location l = block.getLocation();
		Float direction = player.getLocation().getYaw();
		double x;
		double z;
		double directionSign1 = Math.signum(Math.cos(direction*3.14/180*2));
		double directionSign2 = -Math.signum(Math.cos(direction*3.14/180));
		
		if (directionSign1 > 0) {
			x = (double) 1*directionSign2; z = (double) 0;
		} else { x = (double) 0; z = (double) 1*directionSign2; }
		Location ig1; ig1 =  new Location(l.getWorld(), l.getX()      , l.getY() - 1, l.getZ()      );
		Location ig2; ig2 =  new Location(l.getWorld(), l.getX() + x  , l.getY() - 1, l.getZ() + z  );
		Location ig3; ig3 =  new Location(l.getWorld(), l.getX() + 2*x, l.getY() - 1, l.getZ() + 2*z);
		Location ig4; ig4 =  new Location(l.getWorld(), l.getX() + 3*x, l.getY() - 1, l.getZ() + 3*z);
		Location ig5; ig5 =  new Location(l.getWorld(), l.getX()      , l.getY() - 2, l.getZ()      );
		Location ig6; ig6 =  new Location(l.getWorld(), l.getX() + x  , l.getY() - 2, l.getZ() + z  );
		Location ig7; ig7 =  new Location(l.getWorld(), l.getX() + 2*x, l.getY() - 2, l.getZ() + 2*z);
		Location ig8; ig8 =  new Location(l.getWorld(), l.getX() + 3*x, l.getY() - 2, l.getZ() + 3*z);
		plugin.colosseumGates.put(ig1, string);
		plugin.colosseumGates.put(ig2, string);
		plugin.colosseumGates.put(ig3, string);
		plugin.colosseumGates.put(ig4, string);
		plugin.colosseumGates.put(ig5, string);
		plugin.colosseumGates.put(ig6, string);
		plugin.colosseumGates.put(ig7, string);
		plugin.colosseumGates.put(ig8, string);
		
		player.sendMessage("Gate registered.");
		SaveGates();
		return true;
		
	}
	
	public void SaveGates() {
		
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(gatesFile)));
			for (Location key : plugin.colosseumGates.keySet()) {
				bw.write(String.format("%s,%s,%f,%f,%f%n", plugin.colosseumGates.get(key), key.getWorld().getName(), key.getX(), key.getY(), key.getZ()));
			}
			bw.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void LoadGates() {
		plugin.colosseumGates.clear();
		String inputLine;
		if (gatesFile.exists()) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(gatesFile)));
				while ((inputLine = br.readLine()) != null) {
					String splits[] = inputLine.split(",", 5);
					Location l = new Location(
							server.getWorld(splits[1]),
							Double.parseDouble(splits[2]),
							Double.parseDouble(splits[3]),
							Double.parseDouble(splits[4])
							);
					plugin.colosseumGates.put(l, splits[0]);
				}
				br.close();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
