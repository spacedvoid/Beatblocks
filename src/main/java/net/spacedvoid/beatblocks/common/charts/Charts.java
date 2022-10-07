package net.spacedvoid.beatblocks.common.charts;

import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.common.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.common.exceptions.CommandFailedException;
import net.spacedvoid.beatblocks.singleplayer.chart.Chart;
import net.spacedvoid.beatblocks.singleplayer.exceptions.ChartFileException;
import net.spacedvoid.beatblocks.singleplayer.parser.DefaultParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Charts {
	public static final Path chartFolderPath = Path.of(Beatblocks.getPlugin().getDataFolder().getAbsolutePath() + "/charts");

	public static final Map<String, Map.Entry<Map.Entry<String, String>, ChartStatus>> CHARTS = new HashMap<>();

	public static String getChartPath(String chartName) {
		return CHARTS.get(chartName).getKey().getKey();
	}

	public static String getSoundPath(String chartName) {
		return CHARTS.get(chartName).getKey().getValue();
	}

	public static Map.Entry<String, String> getChartPaths(String chartName) {
		return CHARTS.get(chartName).getKey();
	}

	public static ChartStatus getChartStatus(String chartName) {
		return CHARTS.get(chartName).getValue();
	}

	public static Map.Entry<Map.Entry<String, String>, ChartStatus> getFullEntry(String chartName) {
		return CHARTS.get(chartName);
	}

	public static void setFileStatus(String chartName, ChartStatus status) {
		if(CHARTS.get(chartName) == null) listCharts();
		CHARTS.put(chartName, new AbstractMap.SimpleEntry<>(getChartPaths(chartName), status));
	}

	/**
	 * Loads all charts except those that could not be loaded. This method blocks using {@link Charts#listCharts()}
	 */
	public static void loadAll() {
		Bukkit.getLogger().info("Loading all charts");
		listCharts();
		DefaultParser parser = new DefaultParser();
		for(String key : CHARTS.keySet()) {
			CompletableFuture<Chart> future = parser.readChartAsync(key);
			try {
				future.get();
			} catch (ExecutionException e) {
				if(e.getCause() instanceof ChartFileException) {
					Bukkit.getLogger().warning("Failed to read chart " + key + ": " + e.getCause().getMessage());
				}
				else Bukkit.getLogger().warning("Failed to read chart " + key + ": " + new CommandFailedException(e).getMessage());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void clearChartList() {
		CHARTS.clear();
	}

	public static final String BIC_EXTENTION = ".bic";

	public static CompletableFuture<Void> listChartsAsync() {
		return CompletableFuture.runAsync(Charts::listCharts);
	}

	public static void listCharts() {
		File chartsFolder = chartFolderPath.toFile();
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
				if(file.getName().endsWith(BIC_EXTENTION)) {
					Bukkit.getLogger().info("Found chart file " + file.getAbsolutePath());
					chart = file;
				}
			}
		    if(sound != null && chart != null) {
			    Bukkit.getLogger().info("Adding chart " + chart + " and sound " + sound);
			    CHARTS.put(chartFolder.getName(), new AbstractMap.SimpleEntry<>(new AbstractMap.SimpleEntry<>(chart.getAbsolutePath(), sound.getAbsolutePath()), ChartStatus.NOT_LOADED));
		    }
		    else if(chart == null) {
			    Bukkit.getLogger().info("No chart in " + chartFolder.getPath() + "; ignoring");
		    }
		    else {
			    Bukkit.getLogger().info("No sound in " + chartFolder.getPath() + "; ignoring");
		    }
	    }
	}

	public enum ChartStatus {
		LOADED("Loaded", ChatColor.GREEN), NEEDS_REWRITE(LOADED), INVALID_FORMAT("Invalid chart file", ChatColor.RED), VERSION_MISMATCH("Version does not match", ChatColor.RED), NO_SOUND_FILE("No sound file", ChatColor.RED), NOT_LOADED("Not loaded", ChatColor.GRAY);

		public final String display;
		public final ChatColor color;

		ChartStatus(String display, ChatColor color) {
			this.display = display;
			this.color = color;
		}

		ChartStatus(ChartStatus parent) {
			this.display = parent.display;
			this.color = parent.color;
		}
	}
}
