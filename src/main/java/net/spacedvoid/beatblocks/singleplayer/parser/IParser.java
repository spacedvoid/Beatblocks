package net.spacedvoid.beatblocks.singleplayer.parser;

import net.spacedvoid.beatblocks.singleplayer.chart.Chart;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface IParser {
	/**
	 * This does not store the returned {@link Chart} somewhere for memory advantages, and should not be so.
	 */
	default CompletableFuture<Chart> readChartAsync(Path chartPath) {
		return CompletableFuture.supplyAsync(() -> readChart(chartPath));
	}

	/**
	 * This does not store the returned {@link Chart} somewhere for memory advantages, and should not be so.
	 */
	Chart readChart(Path chartPath);
}
