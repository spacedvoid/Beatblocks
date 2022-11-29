package net.spacedvoid.beatblocks.util;

import net.spacedvoid.beatblocks.common.Beatblocks;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ExceptionUtil {
	public static String getFullMessage(Throwable thrown) {
		if(Beatblocks.getPlugin().getConfig().getBoolean("show-stacktrace", true)) {
			return getExceptionInfo(thrown) + walk(thrown);
		}
		return getExceptionInfo(thrown);
	}

	private static String walk(@Nullable Throwable thrown) {
		if(thrown == null) return "";
		StringBuilder builder = new StringBuilder();
		builder.append(getCauseMessage(thrown.getCause()));
		builder.append(walk(thrown.getCause()));
		for(Throwable suppress : thrown.getSuppressed()) {
			if(!builder.isEmpty()) builder.append("\n");
			builder.append(getSuppressedMessage(suppress));
			builder.append(walk(suppress));
		}
		return builder.isEmpty()? "" : builder.insert(0, "\n").toString();
	}

	private static String getExceptionInfo(Throwable thrown) {
		StringBuilder builder = new StringBuilder(thrown.getClass().getName());
		if(thrown.getMessage() != null) builder.append(": ").append(thrown.getMessage());
		return builder.toString();
	}

	public static String getStackTrace(Throwable thrown) {
		if(thrown.getStackTrace().length != 0) {
			var excluded = new HashSet<String>(Set.of("commandapi", "ngrok")) {
				public boolean notContains(String s) {
					for(String string : this) {
						if(s.contains(string)) return false;
 					}
					return true;
				}
			};
			Stream<String> stacktraceStream = Arrays.stream(thrown.getStackTrace()).map(StackTraceElement::toString)
				.map(element -> element.substring(element.lastIndexOf("/") == -1 ? 0 : element.lastIndexOf("/") + 1))
				.takeWhile(string -> string.contains("beatblocks")).filter(excluded::notContains);
			List<String> list;
			if((list = stacktraceStream.toList()).size() != 0) return getExceptionInfo(thrown) + "\n  at " + String.join("\n  at ", list);
		}
		return getExceptionInfo(thrown);
	}

	public static String getCauseMessage(@Nullable Throwable thrown) {
		return thrown == null ? "" : "Caused by: " + getStackTrace(thrown);
	}

	public static String getSuppressedMessage(@Nullable Throwable thrown) {
		return thrown == null ? "" : "Suppressed: " + getStackTrace(thrown);
	}
}
