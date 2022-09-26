package net.spacedvoid.beatblocks.singleplayer;

import net.kyori.adventure.text.Component;
import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.common.charts.ChartDisplayer;
import net.spacedvoid.beatblocks.common.events.PressedNoteEvent;
import org.bukkit.Bukkit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class SinglePlayer {
	private static SinglePlayer instance;

	private SinglePlayer(){}

	public static SinglePlayer get() {
		return instance == null? (instance = new SinglePlayer()) : instance;
	}

	public static boolean isEnabled = false;

	public void enable() {
		Bukkit.getServer().getPluginManager().registerEvents(new PressedNoteEvent(), Beatblocks.getPlugin());
		//Bukkit.getServer().getPluginManager().registerEvents(new PlayerLeaveEvent(), Beatblocks.getPlugin());
		CompletableFuture<Void> listChartsTask = ChartDisplayer.listChartsAsync();
		Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(), () -> {
			try {
				listChartsTask.get();
			} catch (InterruptedException | ExecutionException e) {
				Bukkit.getLogger().log(Level.WARNING, "Could not load chart list", e.getCause());
			}
		}, 5);
		Bukkit.getOnlinePlayers().forEach(player -> {
			player.sendMessage(Component.text("Attempting to set Beatblocks default resource pack"));
			player.setResourcePack("https://drive.google.com/uc?export=download&id=134JGMP1CXhhcPlGR_yK0G105NkaF47-K", "a6ac7c6bdc40ca7da4b5ca14c22a00e4446901a4");
		});
		isEnabled = true;
	}
}
