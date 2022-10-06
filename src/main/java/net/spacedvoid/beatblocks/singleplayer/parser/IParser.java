package net.spacedvoid.beatblocks.singleplayer.parser;

import net.spacedvoid.beatblocks.common.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.singleplayer.chart.Chart;

import java.util.concurrent.CompletableFuture;

public interface IParser {
	String getVersion();

	default CompletableFuture<Chart> readChartAsync(String chartFileName) {
		return CompletableFuture.supplyAsync(() -> readChart(chartFileName)).exceptionally(thrown -> {throw new BeatblocksException("Reading chart failed", thrown);});
	}

	Chart readChart(String chartName);
}
