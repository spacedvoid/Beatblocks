package net.spacedvoid.beatblocks.common.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandFlag {
	public static final CommandFlag SINGLEPLAYER = new CommandFlag(false);
	public static final CommandFlag DEBUG = new CommandFlag(false);

	private CommandFlag(boolean defaultValue) {
		this.flag = defaultValue;
	}

	private boolean flag;

	public static void setFlag(@NotNull CommandFlag commandFlag, boolean flag) {
		commandFlag.flag = flag;
	}

	public boolean isEnabled(CommandSender ignored) {
		return this.flag;
	}

	public boolean isDisabled(CommandSender ignored) {
		return !this.flag;
	}
}
