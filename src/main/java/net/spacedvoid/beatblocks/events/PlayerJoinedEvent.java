package net.spacedvoid.beatblocks.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinedEvent implements Listener {
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().setResourcePack("https://drive.google.com/uc?export=download&id=1wfph4Q8TM7JKsnbe8g2PMd7UL5cvYomK", "a6ac7c6bdc40ca7da4b5ca14c22a00e4446901a4");
	}
}
