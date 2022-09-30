package net.spacedvoid.beatblocks.common.exceptions;

import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.util.Exceptions;

public class CommandFailedException extends RuntimeException {
	public CommandFailedException(String msg) {
		super(msg);
	}

	public CommandFailedException(Exception e) {
		this.cause = e;
	}

	public CommandFailedException(String message, Throwable cause) {
		super(message);
		this.cause = cause;
	}

	@Override
	public String getMessage() {
		StringBuilder ret = new StringBuilder();
		if(super.getMessage() != null) ret.append(super.getMessage());
		else ret.append("Exception thrown");
		if(Beatblocks.getPlugin().getConfig().getBoolean("show-stacktrace", true)) {
			Throwable currentCause;
			if(hasCause()) {
				ret.append("\n").append(Exceptions.getCauseDetail(this.cause));
				currentCause = this.cause;
				while(currentCause.getCause() != null) {
					currentCause = currentCause.getCause();
					ret.append("\n").append(Exceptions.getCauseDetail(currentCause));
				}
			}
		}
		return ret.toString();
	}

	private Throwable cause = null;

	public boolean hasCause() {
		return this.cause != null;
	}
}
