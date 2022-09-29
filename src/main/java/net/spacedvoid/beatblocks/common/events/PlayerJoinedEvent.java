package net.spacedvoid.beatblocks.common.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinedEvent implements Listener {
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().setResourcePack("https://drive.google.com/uc?export=download&id=134JGMP1CXhhcPlGR_yK0G105NkaF47-K", "a6ac7c6bdc40ca7da4b5ca14c22a00e4446901a4");
	}
}
