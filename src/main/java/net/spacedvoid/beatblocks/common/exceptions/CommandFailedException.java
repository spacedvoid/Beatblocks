package net.spacedvoid.beatblocks.common.exceptions;

public class CommandFailedException extends BeatblocksException{
	public CommandFailedException(String message) {
		super(message, false);
	}

	@Override
	public String getLocalizedMessage() {
		return this.getMessage();
	}
}
