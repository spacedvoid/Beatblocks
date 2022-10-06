package net.spacedvoid.beatblocks.util;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Exceptions {
	public static String getStackTrace(Throwable thrown) {
		String ret = thrown.getClass().getName() + ": " + thrown.getMessage();
		if(thrown.getStackTrace().length != 0) {
			Stream<String> stacktraceStream = Arrays.stream(thrown.getStackTrace())
				.map(element -> element.toString().substring(element.toString().lastIndexOf("/") == -1 ? 0 : element.toString().lastIndexOf("/") + 1))
				.filter(string -> string.contains("beatblocks")).filter(string -> !string.contains("commandapi"));
			List<String> list;
			if((list = stacktraceStream.toList()).size() != 0)
				ret += "\n  at " + String.join("\n  at ", list);
		}
		return ret;
	}

	public static String getCauseDetail(@Nullable Throwable thrown) {
		return thrown == null? "" : "Caused by: " + getStackTrace(thrown);
	}

	public static String getSuppressedDetail(@Nullable Throwable thrown) {
		return thrown == null? "" : "Suppressed: " + getStackTrace(thrown);
	}
}
