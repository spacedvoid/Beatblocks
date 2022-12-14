package net.spacedvoid.beatblocks.exceptions;

public class ResourceBuildException extends BeatblocksException {
	public ResourceBuildException(String message) {
		super(message);
	}

	public ResourceBuildException(String message, Throwable cause) {
		super(message, cause);
	}

	@Override
	public String getLocalizedMessage() {
		return this.getMessage();
	}

	public ResourceBuildException suppress(Throwable... suppressed) {
		for(Throwable suppress : suppressed) {
			this.addSuppressed(suppress);
		}
		return this;
	}
}
