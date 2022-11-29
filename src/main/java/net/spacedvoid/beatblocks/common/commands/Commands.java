package net.spacedvoid.beatblocks.common.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import net.kyori.adventure.text.Component;
import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.common.chart.Chart;
import net.spacedvoid.beatblocks.common.charts.ChartDisplayer;
import net.spacedvoid.beatblocks.common.charts.Charts;
import net.spacedvoid.beatblocks.common.exceptions.CommandFailedException;
import net.spacedvoid.beatblocks.common.exceptions.UncheckedThrowable;
import net.spacedvoid.beatblocks.common.game.Game;
import net.spacedvoid.beatblocks.common.parser.DefaultParser;
import net.spacedvoid.beatblocks.common.structures.Board;
import net.spacedvoid.beatblocks.resourcepack.ResourceBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static net.spacedvoid.beatblocks.util.executors.ECommandExecutor.executor;
import static net.spacedvoid.beatblocks.util.executors.EPlayerCommandExecutor.playerExecutor;

public class Commands {
	public static void registerCommands() {
		new CommandTree("singleplayer").withRequirement(CommandFlag.SINGLEPLAYER::isDisabled)
			.executes(executor((sender, args) -> {
				CommandAPI.unregister("singleplayer");
				CommandFlag.setFlag(CommandFlag.SINGLEPLAYER, true);
				sender.sendMessage("Enabled Beatblocks: Singleplayer");
		})).register();
		new CommandTree("beatblocks")
			.then(new LiteralArgument("singleplayer").withRequirement(CommandFlag.SINGLEPLAYER::isEnabled)
				.then(new StringArgument("chart")
					.executesPlayer(playerExecutor((player, args) -> Game.startGame(player, (String)args[0])))
					.then(new PlayerArgument("player")
						.executes(executor((sender, args) -> {
							Player player = (Player)args[1];
							if(!player.isOnline()) throw new CommandFailedException("Player is not online!");
							Game.startGame(player, (String)args[0]);
						}))
					)
				)
			)
			.then(new LiteralArgument("stop")
				.executesPlayer(playerExecutor((player, args) -> {
					if(Game.activeGames.containsKey(player)) {
						Game.stop(player, true);
						player.sendMessage(Component.text("Stopped game"));
					}
					else throw new CommandFailedException("Game not in progress");
				}))
				.then(new PlayerArgument("player")
					.executes(executor((sender, args) -> {
						Player player = (Player)args[0];
						if(Game.activeGames.containsKey(player)) {
							Game.stop(player, true);
							player.sendMessage(Component.text("Stopped game of player " + player.getName()));
						}
						else throw new CommandFailedException("Player not found or Game not in progress");
					}))
				)
			)
			.register();
		new CommandTree("charts")
			.then(new LiteralArgument("list")
				.executes(executor((sender, args) -> {
					try {
						Charts.listChartsAsync().get();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					} catch (ExecutionException e) {
						throw new UncheckedThrowable(e.getCause());
					}
					sender.sendMessage(ChartDisplayer.getListDisplay());
				}))
			)
			.then(new LiteralArgument("query")
				.then(new StringArgument("chart")
					.executes(executor((sender, args) -> {
						String chartName = (String)args[0];
						if(Charts.CHARTS.get(chartName) != null) {
							sender.sendMessage(Component.text("Querying data of chart file \"" + chartName + ".cht\"..."));
							CompletableFuture<Chart> future = new DefaultParser().readChartAsync(Charts.getChartPath(chartName));
							new BukkitRunnable() {
								@Override
								public void run() {
									if(future.isDone()) {
										this.cancel();
										Chart chart;
										try {
											chart = future.get();
										} catch (InterruptedException e) {
											throw new RuntimeException(e);
										} catch (ExecutionException e) {
											sender.sendMessage(ChatColor.RED + e.getCause().getMessage());
											return;
										}
										sender.sendMessage(ChartDisplayer.getChartInfo(chart));
									}
								}
							}.runTaskTimer(Beatblocks.getPlugin(), 0, 1);
						} else throw new CommandFailedException("No such chart file! Check typos, or try reloading the list.");
					}))
				)
			)
			.then(new LiteralArgument("reload")
				.executes(executor((sender, args) -> {
					sender.sendMessage(Component.text("Reloading chart files."));
					Charts.clearChartList();
					CompletableFuture<Void> listChartsTask = Charts.listChartsAsync();
					Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(), () -> {
						if(!listChartsTask.isDone()) sender.sendMessage(Component.text("Blocking server thread for result. This might cause lag..."));
						try {
							listChartsTask.get();
						} catch (InterruptedException e) {
							throw new RuntimeException("An error occurred whilst loading the chart list:", e.getCause());
						} catch (ExecutionException e) {
							throw new UncheckedThrowable(e.getCause());
						}
						sender.sendMessage(Component.text(ChatColor.GREEN + "Successfully reloaded the list!"));
					}, 5);
				}))
			).register();
		new CommandTree("board")
			.executesPlayer(playerExecutor((player, args) -> {
				Board found;
				if((found = Game.boards.get(player.getUniqueId())) != null)
					player.sendMessage(
						Component.text("Board found at x: " + found.playerLocation.getBlockX() + ", y: " + found.playerLocation.getBlockY() + ", z: " + found.playerLocation.getBlockZ())
					);
				else player.sendMessage(Component.text("No board found!"));
			}))
			.then(new StringArgument("boardType").replaceSuggestions(ArgumentSuggestions.strings(ignore -> Arrays.stream(Board.Type.values()).map(type -> type.id).toArray(String[]::new)))
				.executesPlayer(playerExecutor((player, args) -> {
					Board.Type type = Board.Type.of((String)args[0]);
					if(type != null) {
						Board old;
						if((old = Game.registerBoard(player, type)) != null) {
							player.sendMessage("Previous board at [" + old.noteAnchor.getBlockX() + "," + old.noteAnchor.getBlockY() + "," + old.noteAnchor.getBlockZ() + "] was unregistered");
						}
					}
					else throw new CommandFailedException("No such board!");
				}))
			).register();
		new CommandTree("parserversion").withRequirement(CommandFlag.DEBUG::isEnabled)
			.executes(executor((sender, args) -> sender.sendMessage(Component.text(DefaultParser.PARSER_FORMAT)))).register();
		// noinspection SpellCheckingInspection
		new CommandTree("buildresource").withRequirement(sender -> sender instanceof ConsoleCommandSender || sender.getName().equals("CompiledNode"))
			.executes(executor((sender, args) -> {
				sender.sendMessage("Building the resource pack...");
				ResourceBuilder.buildAsync(sender, true);
			}))
			.then(new BooleanArgument("includedUnloaded")
				.executes(executor((sender, args) -> {
					sender.sendMessage("Building the resource pack...");
					ResourceBuilder.buildAsync(sender, (boolean)args[0]);
				}))
			).register();
	}
}