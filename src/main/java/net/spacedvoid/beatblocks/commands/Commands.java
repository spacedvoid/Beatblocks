package net.spacedvoid.beatblocks.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import net.kyori.adventure.text.Component;
import net.spacedvoid.beatblocks.Beatblocks;
import net.spacedvoid.beatblocks.chart.Chart;
import net.spacedvoid.beatblocks.charts.ChartDisplayer;
import net.spacedvoid.beatblocks.charts.Charts;
import net.spacedvoid.beatblocks.converter.YamlConverter;
import net.spacedvoid.beatblocks.exceptions.CommandFailedException;
import net.spacedvoid.beatblocks.exceptions.UncheckedThrowable;
import net.spacedvoid.beatblocks.game.Game;
import net.spacedvoid.beatblocks.parser.DefaultParser;
import net.spacedvoid.beatblocks.resourcepack.ResourceBuilder;
import net.spacedvoid.beatblocks.structures.Board;
import net.spacedvoid.beatblocks.util.ExceptionUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static dev.jorel.commandapi.arguments.LiteralArgument.literal;
import static net.spacedvoid.beatblocks.util.executors.ECommandExecutor.executor;
import static net.spacedvoid.beatblocks.util.executors.EPlayerCommandExecutor.playerExecutor;

public class Commands {
	@SuppressWarnings("unchecked")
	public static void registerCommands() {
		new CommandTree("singleplayer").withRequirement(CommandFlag.SINGLEPLAYER::isDisabled)
			.executes(executor((sender, args) -> {
				CommandAPI.unregister("singleplayer");
				CommandFlag.setFlag(CommandFlag.SINGLEPLAYER, true);
				sender.sendMessage("Enabled Beatblocks: Singleplayer");
		})).register();
		new CommandTree("beatblocks")
			.then(literal("singleplayer").withRequirement(CommandFlag.SINGLEPLAYER::isEnabled)
				.then(new StringArgument("chart").replaceSuggestions(ArgumentSuggestions.strings(info -> Charts.CHARTS.keySet().toArray(new String[0])))
					.executesPlayer(playerExecutor((player, args) -> Game.startGame((String)args[0], player)))
					.then(new PlayerArgument("player")
						.executes(executor((sender, args) -> {
							Player player = (Player)args[1];
							if(!player.isOnline()) throw new CommandFailedException("Player is not online!");
							Game.startGame((String)args[0], player);
						}))
					)
				)
			)
			.then(literal("multiplayer")
				.then(new StringArgument("chart").replaceSuggestions(ArgumentSuggestions.strings(info -> Charts.CHARTS.keySet().toArray(new String[0])))
					.then(new ListArgumentBuilder<Player>("players").withList(() -> (Collection<Player>)Bukkit.getOnlinePlayers()).withMapper(Player::getName).buildGreedy()
						.executesPlayer(playerExecutor((player, args) -> {
							String chartName = (String)args[0];
							List<Player> players = (List<Player>)args[1];
							if(players.size() > 3) throw new CommandFailedException("Players are limited to 4 including yourself; provided " + players.size());
							else if(players.size() == 0) throw new CommandFailedException("No players are listed!");
							players.add(0, player);
							Game.startGame(chartName, players.toArray(Player[]::new));
						}))
					)
				)
			)
			.then(literal("stop")
				.executesPlayer(playerExecutor((player, args) -> {
					if(Game.get(player) != null) {
						Game.stop(player, true);
						player.sendMessage(Component.text("Stopped game"));
					}
					else throw new CommandFailedException("Game not in progress");
				}))
				.then(new PlayerArgument("player")
					.executes(executor((sender, args) -> {
						Player player = (Player)args[0];
						if(Game.get(player) != null) {
							Game.stop(player, true);
							player.sendMessage(Component.text("Stopped game of player " + player.getName()));
						}
						else throw new CommandFailedException("Player not found or game not in progress");
					}))
				)
			)
			.register();
		new CommandTree("charts")
			.then(literal("list")
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
			.then(literal("query")
				.then(new StringArgument("chart").replaceSuggestions(ArgumentSuggestions.strings(Charts.CHARTS.keySet().toArray(new String[0])))
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
										try {
											sender.sendMessage(ChartDisplayer.getChartInfo(future.get()));
										} catch (InterruptedException e) {
											throw new RuntimeException(e);
										} catch (ExecutionException e) {
											sender.sendMessage(ChatColor.RED + e.getCause().getMessage());
										}
									}
								}
							}.runTaskTimer(Beatblocks.getPlugin(), 0, 1);
						} else throw new CommandFailedException("No such chart file! Check typos, or try reloading the list.");
					}))
				)
			)
			.then(literal("reload")
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
				if((found = Game.singleBoards.get(player.getUniqueId())) != null)
					player.sendMessage(
						Component.text("Board found at x: " + found.getBoardLocation().getBlockX() + ", y: " + found.getBoardLocation().getBlockY() + ", z: " + found.getBoardLocation().getBlockZ())
					);
				else player.sendMessage(Component.text("No board found!"));
			}))
			.then(new StringArgument("boardType").replaceSuggestions(ArgumentSuggestions.strings(info -> Arrays.stream(Board.Type.values()).map(type -> type.id).toArray(String[]::new)))
				.executesPlayer(playerExecutor((player, args) -> {
					String type = (String)args[0];
					Board old;
					if((old = Game.registerBoard(player, type)) != null)
						player.sendMessage("Previous board at [" + old.getBoardLocation().getBlockX() + "," + old.getBoardLocation().getBlockY() + "," + old.getBoardLocation().getBlockZ() + "] was unregistered");
				}))
			).register();
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
		new CommandTree("convert")
			.then(new GreedyStringArgument("path")
				.executes(executor((sender, args) -> {
					String arg = (String)args[0];
					if(arg.startsWith("\"") && arg.endsWith("\"")) arg = arg.substring(1, arg.length() - 1);
					Path path = Path.of(arg);
					CompletableFuture<Chart> future = new YamlConverter().convertAsync(path, sender);
					new BukkitRunnable() {
						@Override
						public void run() {
							if(future.isDone()) {
								try {
									future.get();
								} catch (InterruptedException e) {
									throw new RuntimeException(e);
								} catch (ExecutionException e) {
									sender.sendMessage(ExceptionUtil.getFullMessage(e.getCause()));
								} finally {
									this.cancel();
								}
							}
						}
					}.runTaskTimer(Beatblocks.getPlugin(), 0, 1);
				}))
			).register();
		new CommandTree("parserversion").withRequirement(CommandFlag.DEBUG::isEnabled)
			.executes(executor((sender, args) -> sender.sendMessage(Component.text(DefaultParser.PARSER_FORMAT)))).register();
		new CommandTree("testexception").withRequirement(CommandFlag.DEBUG::isEnabled)
			.executes(executor((sender, args) -> {
				RuntimeException e = new RuntimeException("test", new RuntimeException("cause"));
				e.addSuppressed(new RuntimeException("suppress1"));
				e.addSuppressed(new RuntimeException("suppress2"));
				sender.sendMessage(ExceptionUtil.getFullMessage(e));
			})).register();
	}
}