package me.wooskie.colosseum;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ColosseumPlayerListener extends PlayerListener{

	private Colosseum plugin;
	private ColosseumCommands colosseumCommands;
	private ColosseumMonitor colosseumMonitor;
	
	public ColosseumPlayerListener(Colosseum parent) {
		plugin = parent;
		colosseumCommands = new ColosseumCommands( plugin );
		colosseumMonitor = new ColosseumMonitor( plugin );
	}
	
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		// If a block is right clicked and it is a registered
		// sign, passes sign and player to command handler:
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Block block = event.getClickedBlock();
			if (block.getState() instanceof Sign) {
				Sign sign = (Sign) block.getState();
				for (Sign key : plugin.colosseumSigns.keySet()) {
					if (key.getBlock().equals(sign.getBlock())) {
						Player player = event.getPlayer();
						colosseumCommands.SignCommandHandler(key, player);
					}
				}
			}
		}
	}
	
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (plugin.roster.containsKey(event.getPlayer())) {
			colosseumCommands.PlayerQuit(event.getPlayer());
		}
	}
	
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (plugin.roster.containsKey(event.getPlayer())) {
			colosseumMonitor.DropItem(event.getPlayer(), event);
		}
	}
		
	
}
