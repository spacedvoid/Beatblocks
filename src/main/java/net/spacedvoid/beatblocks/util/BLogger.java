package net.spacedvoid.beatblocks.util;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public class BLogger {
	private final String prefix;

	public BLogger(String prefix) {
		this.prefix = "[Beatblocks/" + prefix + "] ";
	}

	public void info(String msg) {
		Bukkit.getLogger().info(prefix + msg);
	}

	/**
	 * @throws RuntimeException The passed <code>exception</code> is thrown.
	 */
	public void exception(String msg, RuntimeException exception) throws RuntimeException {
		Bukkit.getLogger().log(Level.WARNING, msg + " See exception log below.");
		throw exception;
	}

	public void warn(String msg) {
		Bukkit.getLogger().warning(prefix + msg);
	}
}
