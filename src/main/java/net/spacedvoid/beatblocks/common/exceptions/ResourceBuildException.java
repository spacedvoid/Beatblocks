package net.spacedvoid.beatblocks.common.exceptions;

public class ResourceBuildException extends BeatblocksException {
	public ResourceBuildException(String message) {
		super(message);
	}

	public ResourceBuildException(String message, Throwable cause) {
		super(message, cause);
	}

	@Override
	public ResourceBuildException suppress(Throwable... suppressed) {
		return (ResourceBuildException)super.suppress(suppressed);
	}
}
