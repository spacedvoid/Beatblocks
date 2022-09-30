package net.spacedvoid.beatblocks.singleplayer;

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
		CompletableFuture<Void> listChartsTask = ChartDisplayer.listChartsAsync();
		Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(), () -> {
			try {
				listChartsTask.get();
			} catch (InterruptedException | ExecutionException e) {
				Bukkit.getLogger().log(Level.WARNING, "Could not load chart list", e.getCause());
			}
		}, 5);
		isEnabled = true;
	}
}
