package net.spacedvoid.beatblocks.common.exceptions;

import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.util.Exceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandFailedException extends RuntimeException {
	public CommandFailedException(String message) {
		this.message = message;
	}

	public CommandFailedException(Exception e) {
		this.cause = e;
	}

	public CommandFailedException(String message, Throwable cause) {
		this.message = message;
		this.cause = cause;
	}

	@Override
	public String getMessage() {
		StringBuilder messageBuilder = new StringBuilder();
		if(this.message != null) messageBuilder.append(super.getMessage());
		else messageBuilder.append("Exception thrown");
		if(Beatblocks.getPlugin().getConfig().getBoolean("show-stacktrace", true)) {
			Throwable current;
			if(hasCause()) {
				messageBuilder.append("\n").append(Exceptions.getCauseDetail(this.cause));
				current = this.cause;
				while(current.getCause() != null) {
					current = current.getCause();
					messageBuilder.append("\n").append(Exceptions.getCauseDetail(current));
				}
			}
			if(hasSuppressed()) {
				for(Throwable throwable : suppressed) {
					messageBuilder.append("\n").append(Exceptions.getSuppressedDetail(throwable));
					current = throwable;
					while(current.getCause() != null) {
						current = current.getCause();
						messageBuilder.append("\n").append(Exceptions.getCauseDetail(current));
					}
				}
			}
		}
		return messageBuilder.toString();
	}

	public CommandFailedException suppress(Throwable... suppressed) {
		if(this.suppressed == null) this.suppressed = new ArrayList<>();
		this.suppressed.addAll(Arrays.asList(suppressed));
		return this;
	}

	private Throwable cause = null;
	private String message = null;
	private List<Throwable> suppressed = null;

	public boolean hasCause() {
		return this.cause != null;
	}

	public boolean hasSuppressed() {
		return this.suppressed != null && this.suppressed.size() != 0;
	}
}
