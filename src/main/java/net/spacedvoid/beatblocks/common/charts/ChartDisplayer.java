package net.spacedvoid.beatblocks.common.charts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.spacedvoid.beatblocks.singleplayer.chart.Chart;
import org.bukkit.ChatColor;

import java.util.Map;

public class ChartDisplayer {
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
