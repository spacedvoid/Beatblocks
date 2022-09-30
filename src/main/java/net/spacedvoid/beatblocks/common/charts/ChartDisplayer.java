package net.spacedvoid.beatblocks.common.charts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.spacedvoid.beatblocks.common.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.resourcepack.ResourceBuilder;
import net.spacedvoid.beatblocks.singleplayer.chart.Chart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChartDisplayer {

	public static CompletableFuture<Void> listChartsAsync() {
		return CompletableFuture.runAsync(ChartDisplayer::listCharts);
	}

	public static void listCharts() {
		ResourceBuilder.checkChartFolderLock();
		File chartsFolder = new File(Charts.chartFolderPath);
	    if(!chartsFolder.exists()) {
		    Bukkit.getLogger().info("Beatblocks charts folder not found. Creating /plugins/Beatblocks/charts...");
		    try { Files.createDirectories(chartsFolder.toPath()); }
			catch (IOException e) {
				throw new BeatblocksException("Failed to create charts folder", e);
			}
		    return;
	    }
	    File[] chartFolderList = chartsFolder.listFiles();
	    if(chartFolderList == null || !chartsFolder.isDirectory()) {
			Bukkit.getLogger().warning("Folder /plugins/Beatblocks/charts cannot be found or is not a directory");
			return;
	    }
	    for(File chartFolder : chartFolderList) {
			if(chartFolder.isFile()) continue;
			File[] list = chartFolder.listFiles();
			if(list == null) {
				Bukkit.getLogger().warning("Folder " + chartFolder.getPath() + " did not exist or an I/O error occurred; Skipping");
				continue;
			}
		    File chart = null;
		    File sound = null;
			for(File file : list) {
				if(file.isDirectory()) continue;
				if(file.getName().endsWith(".ogg")) {
					Bukkit.getLogger().info("Found sound file " + file.getAbsolutePath());
					sound = file;
				}
				if(file.getName().endsWith(".cht")) {
					Bukkit.getLogger().info("Found chart file " + file.getAbsolutePath());
					chart = file;
				}
			}
		    if(sound != null && chart != null) {
			    Bukkit.getLogger().info("Adding chart " + chart + " and sound " + sound);
			    Charts.CHARTS.put(chartFolder.getName(), new AbstractMap.SimpleEntry<>(new AbstractMap.SimpleEntry<>(chart.getAbsolutePath(), sound.getAbsolutePath()), Charts.ChartStatus.NOT_LOADED));
		    }
		    else if(chart == null) {
			    Bukkit.getLogger().info("No chart in " + chartFolder.getPath() + "; ignoring");
		    }
		    else {
			    Bukkit.getLogger().info("No sound in " + chartFolder.getPath() + "; ignoring");
		    }
	    }
	}

	public static TextComponent getListDisplay() {
		TextComponent component = Component.text("List of charts ("
			+ ChatColor.GREEN + "\u2588" + ChatColor.WHITE + "=Loaded,"
			+ ChatColor.GRAY + "\u2588" + ChatColor.WHITE + "=Not Loaded," + ChatColor.RED + "\u2588" + "=Error"
			+ ChatColor.WHITE + "):\n");
		for(Map.Entry<String, Map.Entry<Map.Entry<String, String>, Charts.ChartStatus>> entry : Charts.CHARTS.entrySet()) {
			component = component.append(Component.text(entry.getValue().getValue().color + "[" + entry.getKey() + "] ")
				.hoverEvent(HoverEvent.showText(Component.text(entry.getValue().getValue().display))));
		}
		return component;
	}

	public static TextComponent getChartInfo(Chart chart) {
		return Component.text(
			ChatColor.GREEN + "========================================================\n" +
				ChatColor.WHITE + "Song : " + chart.getString(Chart.song) + "\n" +
				"Artist : " + chart.getString(Chart.artist) + "\n" +
				"Creator : " + chart.getString(Chart.creator) + "\n" +
				"Difficulty : " + chart.getDouble(Chart.difficulty) + "\n" +
				"BPM : " + chart.getDouble(Chart.bpm) + "\n" +
				"Time : " + chart.getTime(Chart.time).toString() + "\n" +
				ChatColor.GREEN + "========================================================"
		);
	}

}
