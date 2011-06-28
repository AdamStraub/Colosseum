package me.wooskie.colosseum;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ColosseumEntityListener extends EntityListener{
	
	private Colosseum plugin;
	
	public ColosseumEntityListener(Colosseum parent) {
		plugin = parent;
	}

	public void onEntityDeath(EntityDeathEvent event) {
		
		// Aborts script if colosseum not in use:
		if (!plugin.colosseumInUse) {
			return;
		}
		
		Entity entity = event.getEntity();

		if (plugin.roster.containsKey(entity)) {
			
			// defines player:
			Player player = (Player) entity;
			
			// Identifies arrows in inventory:
			HashMap<Integer, ? extends ItemStack> arrows = player.getInventory().all(Material.ARROW);
			
			// Clears drops:
			event.getDrops().clear();
			
			// Adds remaining arrows to drops:
			for (int slot : arrows.keySet()) {
				event.getDrops().add(arrows.get(slot));
			}
			
			// sets player health to 20 to prevent respawn:
			player.setHealth(20);
			
			// delays one tick before executing player death function:
			PlayerDeathRunnable playerDeathRunnable = new PlayerDeathRunnable(player);
			player.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) plugin, playerDeathRunnable, 1L);
		}
	}
	
	private class PlayerDeathRunnable implements Runnable {
	     Player player;

	     PlayerDeathRunnable(Player player) {
	       this.player = player;
	     }
	     public void run() {
	    	 Colosseum.colosseumCommands.PlayerDeath(player);
	     }
	  }
	
	public void onEntityDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (plugin.roster.containsKey(entity)) {
			if (event instanceof EntityDamageByProjectileEvent) {
	        	EntityDamageByProjectileEvent edbpe = (EntityDamageByProjectileEvent) event;
	        	if (edbpe.getProjectile() instanceof Arrow) {
	        		event.setDamage(3);
		        }
	        } else if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
	        	event.setDamage(6);
	        }
		}
    }
	
}
