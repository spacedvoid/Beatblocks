package net.spacedvoid.beatblocks.common.events;

import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.common.commands.CommandFlag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinedEvent implements Listener {
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(Beatblocks.getPlugin().getConfig().getBoolean("debug", false)) CommandFlag.setFlag(CommandFlag.DEBUG, true);
		event.getPlayer().setResourcePack("https://drive.google.com/uc?export=download&id=134JGMP1CXhhcPlGR_yK0G105NkaF47-K", "a6ac7c6bdc40ca7da4b5ca14c22a00e4446901a4");
	}
}
