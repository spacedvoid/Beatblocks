package net.spacedvoid.beatblocks.singleplayer.parser;

public class Parsers {
	private static IParser parser = new DefaultParser();

	public static <T extends IParser> void setParser(IParser parser) {
		Parsers.parser = parser;
	}

	public static IParser getParser() {
		return Parsers.parser;
	}
}
