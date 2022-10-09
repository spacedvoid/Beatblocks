package net.spacedvoid.beatblocks.util.executors;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.ConsoleCommandExecutor;
import net.spacedvoid.beatblocks.common.exceptions.DetailedException;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class EConsoleCommandExecutor implements ConsoleCommandExecutor {
	private final ConsoleCommandExecutor executor;

	private EConsoleCommandExecutor(ConsoleCommandExecutor executor) {
		this.executor = executor;
	}

	public static EConsoleCommandExecutor consoleExecutor(ConsoleCommandExecutor executor) {
		return new EConsoleCommandExecutor(executor);
	}

	@Override
	public void run(ConsoleCommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
		try {
			executor.run(sender,args);
		}
		catch (DetailedException exception) {
			sender.sendMessage(ChatColor.RED + exception.getMessage());
		} catch (RuntimeException exception) {
			sender.sendMessage(ChatColor.RED + new DetailedException(exception).getMessage());
		}
	}
}
