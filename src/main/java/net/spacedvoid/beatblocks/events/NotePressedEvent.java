package net.spacedvoid.beatblocks.events;

import net.spacedvoid.beatblocks.game.Game;
import net.spacedvoid.beatblocks.game.GameInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotePressedEvent implements Listener {
	private static final List<UUID> INCLUDED = new ArrayList<>();

	@SuppressWarnings("unused")
	@EventHandler
	public void notePressed(PlayerItemHeldEvent e) {
		if(!INCLUDED.contains(e.getPlayer().getUniqueId())) return;
		GameInstance instance;
		if((instance = Game.get(e.getPlayer())) == null) return;
		if(e.getPreviousSlot() == e.getNewSlot()) return;
		if(e.getNewSlot() == 4) return;
		instance.processNote(e.getPlayer(), e.getNewSlot());
		e.getPlayer().getInventory().setHeldItemSlot(4);
		e.setCancelled(true);
	}

	public static void include(Player player) {
		if(INCLUDED.contains(player.getUniqueId())) {
			INCLUDED.add(player.getUniqueId());
			player.getInventory().setHeldItemSlot(4);
		}
	}

	public static void exclude(Player player) {
		INCLUDED.remove(player.getUniqueId());
	}
}
