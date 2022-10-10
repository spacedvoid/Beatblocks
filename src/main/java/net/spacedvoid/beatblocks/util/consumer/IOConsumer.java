package net.spacedvoid.beatblocks.util.consumer;

import java.io.IOException;

public interface IOConsumer<T> {
	void accept(T t) throws IOException;
}
