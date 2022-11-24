package net.spacedvoid.beatblocks.common.exceptions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Check whether this is being thrown from {@link org.bukkit.scheduler.BukkitTask BukkitTask}s. When such, extended executors will not catch it.
 */
public class UncheckedThrowable extends RuntimeException {
	public UncheckedThrowable(@NotNull Throwable cause) {
		this.message = null;
		this.cause = cause;
	}

	private final String message;
	private final Throwable cause;

	public UncheckedThrowable(String msg, IOException exception) {
		this.message = msg;
		this.cause = exception;
	}

	@NotNull public Throwable getCause() {
		if(cause instanceof UncheckedThrowable unchecked) {
			return unchecked.getCause();
		}
		return cause;
	}
}
