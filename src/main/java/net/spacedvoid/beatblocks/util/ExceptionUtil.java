package net.spacedvoid.beatblocks.util;

import net.spacedvoid.beatblocks.Beatblocks;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class ExceptionUtil {
	public static String getFullMessage(Throwable thrown) {
		if(Beatblocks.getPlugin().getConfig().getBoolean(Beatblocks.Config.SHOW_STACKTRACE, true)) return walk(thrown).toString();
		return getExceptionInfo(thrown).toString();
	}

	private static StringBuilder walk(@Nullable Throwable thrown) {
		if(thrown == null) return new StringBuilder(0);
		StringBuilder builder = new StringBuilder();
		builder.append(getStackTrace(thrown));
		if(thrown.getCause() != null) {
			builder.append("\n").append("Caused by: ");
			builder.append(walk(thrown.getCause()));
		}
		for(Throwable suppressed : thrown.getSuppressed()) {
			builder.append("\n").append("Suppressed: ");
			builder.append(walk(suppressed));
		}
		return builder;
	}

	private static StringBuilder getExceptionInfo(Throwable thrown) {
		StringBuilder builder = new StringBuilder(thrown.getClass().getName());
		if(thrown.getMessage() != null) builder.append(": ").append(thrown.getMessage());
		return builder;
	}

	public static String getStackTrace(Throwable thrown) {
		if(thrown.getStackTrace().length != 0) {
			abbreviateStacktrace(thrown);
			List<String> list = Arrays.stream(thrown.getStackTrace()).map(StackTraceElement::toString)
				.map(element -> element.substring(element.lastIndexOf("/") == -1 ? 0 : element.lastIndexOf("/") + 1)).toList();
			if(list.size() != 0) return getExceptionInfo(thrown) + "\n  at " + String.join("\n  at ", list);
		}
		return getExceptionInfo(thrown).toString();
	}

	public static Throwable abbreviateStacktrace(Throwable thrown) {
		StackTraceElement[] stacktrace = thrown.getStackTrace();
		thrown.setStackTrace(Arrays.stream(stacktrace)
			.takeWhile(e -> e.toString().contains("beatblocks"))
			.filter(e -> !e.toString().contains("commandapi"))
			.filter(e -> !e.toString().contains("ngrok"))
			.toArray(StackTraceElement[]::new)
		);
		return thrown;
	}
}
