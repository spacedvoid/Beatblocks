package net.spacedvoid.beatblocks.util.executors;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.spacedvoid.beatblocks.common.exceptions.DetailedException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.UncheckedIOException;

public class ECommandExecutor implements CommandExecutor {
	private final CommandExecutor executor;

	private ECommandExecutor(CommandExecutor executor) {
		this.executor = executor;
	}

	public static ECommandExecutor executor(CommandExecutor executor) {
		return new ECommandExecutor(executor);
	}

	@Override
	public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
		try {
			executor.run(sender, args);
		} catch (DetailedException exception) {
			sender.sendMessage(ChatColor.RED + exception.getMessage());
		} catch (RuntimeException exception) {
			Throwable cause;
			if(exception instanceof UncheckedIOException) cause = exception.getCause();
			else cause = exception;
			sender.sendMessage(ChatColor.RED + "Command Failed: " + new DetailedException(cause).getMessage());
		}
	}
}
