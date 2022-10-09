package net.spacedvoid.beatblocks.common.exceptions;

public class CommandFailedException extends DetailedException{
	public CommandFailedException(String message) {
		super(message);
	}

	public CommandFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandFailedException(Exception cause) {
		super(cause);
	}
}
