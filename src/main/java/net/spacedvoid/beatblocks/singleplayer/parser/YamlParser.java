package net.spacedvoid.beatblocks.singleplayer.parser;

import net.spacedvoid.beatblocks.singleplayer.chart.Chart;

import java.util.concurrent.CompletableFuture;

public class YamlParser {
	public static final double PARSER_VERSION = 1.0;
	public static final String PARSER_FORMAT = "YAML-1.0";

	public CompletableFuture<Chart> readChartAsync(String chartFileName) {
		return CompletableFuture.supplyAsync(() -> readChart(chartFileName));
	}

	public Chart readChart(String chartName) {
		throw new UnsupportedOperationException("YAML Parser not available yet");
	}
}
