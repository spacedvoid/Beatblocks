package net.spacedvoid.beatblocks.parser;

import net.spacedvoid.beatblocks.chart.Chart;

import java.nio.file.Path;

public class YamlParser implements IParser {
	public static final double PARSER_VERSION = 1.0;
	public static final String PARSER_FORMAT = "YAML-1.0";

	public Chart readChart(Path chartName) {
		throw new UnsupportedOperationException("YAML Parser not available yet");
	}
}
