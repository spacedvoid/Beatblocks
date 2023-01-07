package net.spacedvoid.beatblocks.events;

import net.spacedvoid.beatblocks.game.Game;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveEvent implements Listener {
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if(Game.get(event.getPlayer()) != null) Game.stop(event.getPlayer(), true);
		if(RPAppliedEvent.isTracked(event.getPlayer())) RPAppliedEvent.untrack(event.getPlayer(), null);
	}
}
