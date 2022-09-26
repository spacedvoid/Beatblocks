package net.spacedvoid.beatblocks.singleplayer.exceptions;

import net.spacedvoid.beatblocks.common.exceptions.BeatblocksException;

public class ResourceBuildException extends BeatblocksException {
	public ResourceBuildException(String message) {
		super(message);
	}

	public ResourceBuildException(String message, Throwable cause) {
		super(message, cause);
	}
}
