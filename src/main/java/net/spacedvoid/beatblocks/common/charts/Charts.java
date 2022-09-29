package net.spacedvoid.beatblocks.common.charts;

import net.spacedvoid.beatblocks.common.Beatblocks;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class Charts {
	public static final String chartFolderPath = new File(Beatblocks.getPlugin().getDataFolder().getPath() + "/charts").getPath();

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
		if(CHARTS.get(chartName) == null) ChartDisplayer.listCharts();
		CHARTS.put(chartName, new AbstractMap.SimpleEntry<>(getChartPaths(chartName), status));
	}

	public static void clearChartList() {
		CHARTS.clear();
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
