package net.spacedvoid.beatblocks.util.executors;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.spacedvoid.beatblocks.common.exceptions.DetailedException;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class EPlayerCommandExecutor implements PlayerCommandExecutor {
	private final PlayerCommandExecutor executor;

	private EPlayerCommandExecutor(PlayerCommandExecutor executor) {
		this.executor = executor;
	}

	public static EPlayerCommandExecutor playerExecutor(PlayerCommandExecutor executor) {
		return new EPlayerCommandExecutor(executor);
	}

	@Override
	public void run(Player sender, Object[] args) throws WrapperCommandSyntaxException {
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
