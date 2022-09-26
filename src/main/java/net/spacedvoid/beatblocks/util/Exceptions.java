package net.spacedvoid.beatblocks.util;

import java.util.Arrays;

public class Exceptions {
	public static String getStackTrace(Throwable thrown) {
		return thrown.getClass().getName() + ": " + thrown.getMessage() + "\n  at " + String.join("\n  at ", Arrays.stream(thrown.getStackTrace())
			.map(element -> element.toString().substring(element.toString().lastIndexOf("/") == -1? 0 : element.toString().lastIndexOf("/") + 1))
			.filter(string -> string.contains("beatblocks"))
			.filter(string -> !string.contains("commandapi")).toList());
	}
}
