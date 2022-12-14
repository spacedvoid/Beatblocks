package net.spacedvoid.beatblocks.parser;

import net.spacedvoid.beatblocks.chart.Chart;
import net.spacedvoid.beatblocks.exceptions.UncheckedThrowable;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface IParser {
	/**
	 * This does not store the returned {@link Chart} somewhere for memory advantages, and should not be so.
	 * {@link java.util.concurrent.ExecutionException ExecutionException}s should just be rethrown
	 * with {@link UncheckedThrowable UncheckThrowable}s when executed via commands.
	 */
	default CompletableFuture<Chart> readChartAsync(Path chartPath) {
		return CompletableFuture.supplyAsync(() -> readChart(chartPath));
	}

	/**
	 * This does not store the returned {@link Chart} somewhere for memory advantages, and should not be so.
	 */
	Chart readChart(Path chartPath);
}
