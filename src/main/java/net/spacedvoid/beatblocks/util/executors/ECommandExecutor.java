package net.spacedvoid.beatblocks.util.executors;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.spacedvoid.beatblocks.common.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.common.exceptions.UncheckedThrowable;
import net.spacedvoid.beatblocks.util.ExceptionUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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
		} catch (BeatblocksException exception) {
			sender.sendMessage(ChatColor.RED + exception.getLocalizedMessage());
		} catch (UncheckedThrowable exception) {
			sender.sendMessage(ChatColor.RED + ExceptionUtil.getFullMessage(exception.getCause()));
		} catch (RuntimeException exception) {
			sender.sendMessage(ChatColor.RED + ExceptionUtil.getFullMessage(exception));
		}
	}
}
