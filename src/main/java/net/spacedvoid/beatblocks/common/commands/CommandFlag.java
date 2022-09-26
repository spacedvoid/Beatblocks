package net.spacedvoid.beatblocks.common.commands;

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

	public static boolean isEnabled(@NotNull CommandFlag commandFlag) {
		return commandFlag.flag;
	}

	public static boolean isDisabled(@NotNull CommandFlag commandFlag) {
		return !commandFlag.flag;
	}
}
