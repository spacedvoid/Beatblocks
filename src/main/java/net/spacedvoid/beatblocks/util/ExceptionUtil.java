package net.spacedvoid.beatblocks.util;

import net.spacedvoid.beatblocks.Beatblocks;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ExceptionUtil {
	// TODO: Use setStacktrace() to filter stacktraces with paths instead of filtering all stack traces
	public static String getFullMessage(Throwable thrown, boolean override) {
		if(override) {
			return getExceptionInfo(thrown).append(walk(thrown)).toString();
		}
		else {
			return getFullMessage(thrown);
		}
	}

	public static String getFullMessage(Throwable thrown) {
		if(Beatblocks.getPlugin().getConfig().getBoolean(Beatblocks.Config.SHOW_STACKTRACE, true)) {
			return getExceptionInfo(thrown).append(walk(thrown)).toString();
		}
		return getExceptionInfo(thrown).toString();
	}

	private static StringBuilder walk(@Nullable Throwable thrown) {
		StringBuilder builder = new StringBuilder();
		if(thrown == null) return builder;
		builder.append(getCauseMessage(thrown.getCause()));
		builder.append(walk(thrown.getCause()));
		for(Throwable suppress : thrown.getSuppressed()) {
			if(!builder.isEmpty()) builder.append("\n");
			builder.append(getSuppressedMessage(suppress));
			builder.append(walk(suppress));
		}
		return builder.isEmpty()? builder : builder.insert(0, "\n");
	}

	private static StringBuilder getExceptionInfo(Throwable thrown) {
		StringBuilder builder = new StringBuilder(thrown.getClass().getName());
		if(thrown.getMessage() != null) builder.append(": ").append(thrown.getMessage());
		return builder;
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
		return getExceptionInfo(thrown).toString();
	}

	public static String getCauseMessage(@Nullable Throwable thrown) {
		return thrown == null ? "" : "Caused by: " + getStackTrace(thrown);
	}

	public static String getSuppressedMessage(@Nullable Throwable thrown) {
		return thrown == null ? "" : "Suppressed: " + getStackTrace(thrown);
	}
}
