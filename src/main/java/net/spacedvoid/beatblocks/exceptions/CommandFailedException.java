package net.spacedvoid.beatblocks.exceptions;

public class CommandFailedException extends BeatblocksException{
	public CommandFailedException(String message) {
		super(message, false);
	}

	@Override
	public String getLocalizedMessage() {
		return this.getMessage();
	}
}
