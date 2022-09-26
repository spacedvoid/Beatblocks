package net.spacedvoid.beatblocks.singleplayer.parser;

import net.spacedvoid.beatblocks.resourcepack.ResourceBuilder;
import net.spacedvoid.beatblocks.singleplayer.chart.Chart;
import net.spacedvoid.beatblocks.common.exceptions.BeatblocksException;

import java.util.concurrent.CompletableFuture;

public interface IParser {
	String getVersion();

	default CompletableFuture<Chart> readChartAsync(String chartFileName) {
		return CompletableFuture.supplyAsync(() -> readChart(chartFileName)).exceptionally(thrown -> {throw new BeatblocksException("Reading chart failed", thrown);});
	}

	/**
	 * Must run {@link ResourceBuilder#checkChartFolderLock()} before starting.
	 */
	Chart readChart(String chartName);
}
