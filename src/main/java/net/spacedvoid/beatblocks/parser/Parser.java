package net.spacedvoid.beatblocks.parser;

import net.spacedvoid.beatblocks.chart.Chart;
import net.spacedvoid.beatblocks.exceptions.UncheckedThrowable;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface Parser {
	/**
	 * This does not store the returned {@link Chart} somewhere for memory advantages, and should not be so.
	 * {@link java.util.concurrent.ExecutionException ExecutionException}s should just be rethrown
	 * with {@link UncheckedThrowable UncheckThrowable}s when executed via commands.
	 * @param chartPath The folder that contains the chart file and the sound file.
	 */
	default CompletableFuture<Chart> readChartAsync(Path chartPath) {
		return CompletableFuture.supplyAsync(() -> readChart(chartPath));
	}

	/**
	 * This does not store the returned {@link Chart} somewhere for memory advantages, and should not be so.
	 * @param chartPath The folder that contains the chart file and the sound file.
	 */
	Chart readChart(Path chartPath);
}
