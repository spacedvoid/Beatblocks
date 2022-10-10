package net.spacedvoid.beatblocks.util.consumer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

public class WrapperConsumer {
	/**
	 * Catches {@link IOException}s and throws {@link UncheckedIOException}s.
	 */
	public static <T> Consumer<T> consumer(IOConsumer<T> consumer) {
		return i -> {
			try {
				consumer.accept(i);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		};
	}
}
