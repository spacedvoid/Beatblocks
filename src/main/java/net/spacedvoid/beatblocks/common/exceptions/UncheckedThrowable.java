package net.spacedvoid.beatblocks.common.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * Check whether this is being thrown from {@link org.bukkit.scheduler.BukkitTask BukkitTask}s. When such, extended executors will not catch it.
 */
public class UncheckedThrowable extends RuntimeException {
	public UncheckedThrowable(@NotNull Throwable cause) {
		this.cause = cause;
	}

	private final Throwable cause;

	@NotNull public Throwable getCause() {
		return cause;
	}
}
