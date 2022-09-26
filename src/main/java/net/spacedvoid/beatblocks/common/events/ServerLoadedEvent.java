package net.spacedvoid.beatblocks.common.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class ServerLoadedEvent implements Listener {
	@EventHandler
	public void onServerLoad(ServerLoadEvent event) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "singleplayer");
	}
}
