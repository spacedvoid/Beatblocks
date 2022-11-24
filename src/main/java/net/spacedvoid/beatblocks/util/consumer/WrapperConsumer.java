package net.spacedvoid.beatblocks.util.consumer;

import net.spacedvoid.beatblocks.common.exceptions.UncheckedThrowable;

import java.io.IOException;
import java.util.function.Consumer;

public class WrapperConsumer {
	/**
	 * Catches {@link IOException}s and throws {@link UncheckedThrowable}s.
	 */
	public static <T> Consumer<T> consumer(IOConsumer<T> consumer) {
		return i -> {
			try {
				consumer.accept(i);
			} catch (IOException e) {
				throw new UncheckedThrowable(e);
			}
		};
	}
}
