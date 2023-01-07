package net.spacedvoid.beatblocks.printer;

import net.spacedvoid.beatblocks.chart.Chart;
import net.spacedvoid.beatblocks.charts.Charts;

import java.nio.file.Path;

/**
 * Prints a {@link Chart} to BIC files.
 */
public class Printer {
	public Path print(Chart chart, Path outputDir) {
		// TODO
		return outputDir.resolve(chart.chartName + Charts.BIC_EXTENSION);
	}
}
