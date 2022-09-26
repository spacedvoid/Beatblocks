package net.spacedvoid.beatblocks.common.exceptions;

public class BeatblocksException extends CommandFailedException {
	public BeatblocksException(Exception e) {
		super(e);
	}

	public BeatblocksException(String message) {
		super(message);
	}

	public BeatblocksException(String message, Throwable cause) {
		super(message, cause);
	}
}
