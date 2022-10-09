package net.spacedvoid.beatblocks.common.exceptions;

import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.util.Exceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetailedException extends RuntimeException {
	public DetailedException(String message) {
		this.message = message;
	}

	public DetailedException(Exception e) {
		this.cause = e;
	}

	public DetailedException(String message, Throwable cause) {
		this.message = message;
		this.cause = cause;
	}

	private Throwable cause = null;
	private String message = null;
	private List<Throwable> suppressed = null;

	@Override
	public String getMessage() {
		StringBuilder messageBuilder = new StringBuilder();
		if(this.cause != null) {
			messageBuilder.append("Exception thrown");
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
		}
		else if(this.message != null) messageBuilder.append(this.message);
		return messageBuilder.toString();
	}

	public DetailedException suppress(Throwable... suppressed) {
		if(this.suppressed == null) this.suppressed = new ArrayList<>();
		this.suppressed.addAll(Arrays.asList(suppressed));
		return this;
	}

	public boolean hasCause() {
		return this.cause != null;
	}

	public boolean hasSuppressed() {
		return this.suppressed != null && this.suppressed.size() != 0;
	}
}
