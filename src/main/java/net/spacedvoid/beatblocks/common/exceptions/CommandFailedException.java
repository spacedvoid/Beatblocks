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
		String ret = "Command Failed: ";
		if(super.getMessage() != null) ret += super.getMessage();
		else ret += "Exception thrown";
		if(Beatblocks.getPlugin().getConfig().getBoolean("show-stacktrace", true)) {
			if(hasCause()) ret += "\n" + getCauseDetail();
			if(hasSuppressed()) ret += "\n" + getSuppressedDetail();
		}
		return ret;
	}

	private Throwable cause = null;
	private Throwable suppressed = null;

	public String getCauseDetail() {
		if(hasCause()) return "Caused by: " + Exceptions.getStackTrace(cause);
		else return "";
	}

	public String getSuppressedDetail() {
		if(hasSuppressed()) return "Suppressed: " + Exceptions.getStackTrace(suppressed);
		else return "";
	}

	public CommandFailedException addSuppressedException(Throwable exception) {
		this.suppressed = exception;
		return this;
	}

	public boolean hasCause() {
		return this.cause != null;
	}

	public boolean hasSuppressed() {
		return this.suppressed != null;
	}
}
