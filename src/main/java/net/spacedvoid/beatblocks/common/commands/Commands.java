package net.spacedvoid.beatblocks.common.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.common.Board;
import net.spacedvoid.beatblocks.common.charts.ChartDisplayer;
import net.spacedvoid.beatblocks.common.charts.Charts;
import net.spacedvoid.beatblocks.common.exceptions.CommandFailedException;
import net.spacedvoid.beatblocks.singleplayer.SinglePlayer;
import net.spacedvoid.beatblocks.singleplayer.chart.Chart;
import net.spacedvoid.beatblocks.singleplayer.game.Game;
import net.spacedvoid.beatblocks.singleplayer.parser.Parsers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static net.spacedvoid.beatblocks.util.executors.ECommandExecutor.executor;
import static net.spacedvoid.beatblocks.util.executors.EPlayerCommandExecutor.playerExecutor;

public class Commands {
	public static void registerCommands() {
		new CommandTree("singleplayer").withRequirement(sender -> CommandFlag.isDisabled(CommandFlag.SINGLEPLAYER))
			.executes(executor((sender, args) -> {
				CommandAPI.unregister("singleplayer");
				SinglePlayer.get().enable();
				CommandFlag.setFlag(CommandFlag.SINGLEPLAYER, true);
				sender.sendMessage("Enabled Beatblocks: Singleplayer");
			})).register();
		new CommandTree("beatblocks")
			.then(new LiteralArgument("game").withRequirement(sender -> SinglePlayer.isEnabled && CommandFlag.isEnabled(CommandFlag.SINGLEPLAYER))
				.then(new LiteralArgument("start")
					.then(new StringArgument("chart")
						.executesPlayer(playerExecutor((player, args) -> {
							if(Game.activeGames.get(player) != null) throw new CommandFailedException("The game is already running!");
							Game.startGame(player, (String)args[0]);
						}))
						.then(new PlayerArgument("player")
							/*.executes(executor((sender, args) -> {
								throw CommandAPI.fail("This command is not supported yet :(");
								//if(Game.activeGames.get(player) != null) throw new CommandFailedException(ChatColor.RED + "The game is already running!");
								//TODO - multiplayer
							}))*/
						)
					)
				)
				/*.then(new LiteralArgument("stop")
					.executesPlayer(playerExecutor((player, args) -> {
						//TODO: When force stopping game
						throw CommandAPI.fail("This command is not supported yet :(");
					}))
				)*/
			)
			.then(new LiteralArgument("charts")
				.then(new LiteralArgument("list")
					.executes(executor((sender, args) -> {
						try {
							ChartDisplayer.listChartsAsync().get();
						} catch (InterruptedException | ExecutionException e) {
							throw new CommandFailedException("An error occurred while listing charts.", e);
						}
						sender.sendMessage(ChartDisplayer.getListDisplay());
					}))
				)
				.then(new LiteralArgument("query")
					.then(new StringArgument("chart")
						.executes(executor((sender, args) -> {
							String chartFileName = (String)args[0];
							if(Charts.CHARTS.get(chartFileName) != null) {
								Chart chart;
								sender.sendMessage(Component.text("Querying data of chart file \"" + chartFileName + ".cht\"..."));
								chart = Parsers.getParser().readChart(chartFileName);
								/*try {
									chart = Parsers.getParser().readChartAsync(chartFileName).get();
								} catch(InterruptedException | ExecutionException e) {
									throw new CommandFailedException("An error occurred while reading the chart file.", e.getCause());
								}*/
								sender.sendMessage(ChartDisplayer.getChartInfo(chart));
							}
							else {
								throw new CommandFailedException("No such chart file! Check typos, or try reloading the list.");
							}
						}))
					)
				)
				.then(new LiteralArgument("reload")
					.executes(executor((sender, args) -> {
						sender.sendMessage(Component.text("Reloading chart files."));
						Charts.clearChartList();
						CompletableFuture<Void> listChartsTask = ChartDisplayer.listChartsAsync();
						Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(), () -> {
							if(!listChartsTask.isDone()) sender.sendMessage(Component.text("Blocking server thread for result. This might cause lag..."));
							try {
								listChartsTask.get();
							} catch (InterruptedException | ExecutionException e) {
								throw new CommandFailedException("An error occurred whilst loading the chart list:", e.getCause());
							}
							sender.sendMessage(Component.text(ChatColor.GREEN + "Successfully reloaded the list!"));
						}, 5);
					}))
				)
			)
			.then(new LiteralArgument("board")
				.then(new StringArgument("boardType").replaceSuggestions(ArgumentSuggestions.strings("singleplayer", "multiplayer"))
					.executesPlayer(playerExecutor((player, args) -> {
						switch((String)args[0]) {
							case "singleplayer" -> Game.registerBoard(player, Board.SINGLEPLAYER);
							case "multiplayer" -> throw new CommandFailedException("Multiplayer is not supported yet.");
							default -> throw new CommandFailedException("No such board!");
						}
					}))
				)
			)
			.then(new LiteralArgument("parserversion").withRequirement(sender -> SinglePlayer.isEnabled && CommandFlag.isEnabled(CommandFlag.DEBUG))
				.executes(executor((sender, args) -> sender.sendMessage(Component.text(Parsers.getParser().getVersion()))))
			).register();
		//noinspection SpellCheckingInspection,CommentedOutCode
		new CommandTree("buildresource").withRequirement(sender -> sender instanceof ConsoleCommandSender || sender.getName().equals("CompiledNode"))
			.executes(executor((sender, args) -> {
				throw CommandAPI.fail("Not currently supported :( Maybe at later versions..?");
				/*sender.sendMessage("Building the resource pack...");
				ResourceBuilder.buildAsync(sender);*/
			})).register();
		//noinspection SpellCheckingInspection
		new CommandTree("testexception").withRequirement(sender -> CommandFlag.isEnabled(CommandFlag.DEBUG))
			.executes(executor((sender, args) -> {
				throw new RuntimeException("Testing Exception", new RuntimeException("Cause exception"));
			})).register();
	}
}