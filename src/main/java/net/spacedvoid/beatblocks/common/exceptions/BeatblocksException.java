package net.spacedvoid.beatblocks.common.exceptions;

public class BeatblocksException extends RuntimeException {
	public BeatblocksException(String message) {
		super(message);
	}

	public BeatblocksException(String message, Throwable cause) {
		super(message, cause);
	}

	public BeatblocksException(String message, boolean writableStackTrace) {
		super(message, null, true, writableStackTrace);
	}

	@Override
	public String getLocalizedMessage() {
		return getClass().getName() + ": " + getMessage();
	}
}
