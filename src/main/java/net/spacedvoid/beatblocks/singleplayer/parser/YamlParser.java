package net.spacedvoid.beatblocks.singleplayer.parser;

import net.spacedvoid.beatblocks.singleplayer.chart.Chart;

public class YamlParser implements IParser {
	public static final double READER_VERSION = 1.0;
	public static final String PARSER_VERSION = "YAML-1.0";

	@Override
	public String getVersion() {
		return PARSER_VERSION;
	}

	@Override
	public Chart readChart(String chartName) {
		throw new UnsupportedOperationException("YAML Parser not available yet");
	}
}
