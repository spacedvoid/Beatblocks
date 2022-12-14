package net.spacedvoid.beatblocks.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * Check whether this is being thrown from {@link org.bukkit.scheduler.BukkitTask BukkitTask}s. When such, extended executors will not catch it.
 */
public class UncheckedThrowable extends RuntimeException {
	public UncheckedThrowable(@NotNull Throwable cause) {
		super(null, cause, true, false);
	}

	public UncheckedThrowable(String msg, Throwable cause) {
		super(msg, cause, true, false);
	}

	@Override
	public Throwable getCause() {
		if(super.getCause() instanceof UncheckedThrowable unchecked) {
			return unchecked.getCause();
		}
		return super.getCause();
	}
}
