package net.spacedvoid.beatblocks.events;

import net.spacedvoid.beatblocks.game.Game;
import net.spacedvoid.beatblocks.game.GameInstance;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotePressedEvent implements Listener {
	private static final List<UUID> INCLUDED = new ArrayList<>();

	@EventHandler
	public void notePressed(PlayerItemHeldEvent e) {
		Bukkit.getLogger().info(e.getPlayer().getName() + " pressed");
		if(!INCLUDED.contains(e.getPlayer().getUniqueId())) {
			Bukkit.getLogger().info("1");
			return;
		}
		if(e.getNewSlot() == 4) {
			Bukkit.getLogger().info("2");
			return;
		}
		if(e.getPreviousSlot() == e.getNewSlot()) {
			Bukkit.getLogger().info("3");
			return;
		}
		GameInstance instance;
		if((instance = Game.get(e.getPlayer())) == null) {
			Bukkit.getLogger().info("4");
			return;
		}
		Bukkit.getLogger().info("Requesting process for " + e.getPlayer().getName() + " at lane " + e.getNewSlot());
		instance.processNote(e.getPlayer(), e.getNewSlot());
		e.getPlayer().getInventory().setHeldItemSlot(4);
		e.setCancelled(true);
	}

	public static void include(Player player) {
		INCLUDED.add(player.getUniqueId());
		player.getInventory().setHeldItemSlot(4);
	}

	public static void exclude(Player player) {
		INCLUDED.remove(player.getUniqueId());
	}
}
