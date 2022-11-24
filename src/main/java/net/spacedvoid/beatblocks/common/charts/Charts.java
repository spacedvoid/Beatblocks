package net.spacedvoid.beatblocks.common.charts;

import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.common.chart.Chart;
import net.spacedvoid.beatblocks.common.exceptions.UncheckedThrowable;
import net.spacedvoid.beatblocks.common.parser.DefaultParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static net.spacedvoid.beatblocks.util.consumer.WrapperConsumer.*;

public class Charts {
	static {
		try {
			Files.createDirectories(Path.of(Beatblocks.getPlugin().getDataFolder().getAbsolutePath() ,"charts"));
		} catch (IOException e) {
			throw new UncheckedThrowable(e);
		}
	}

	public static final Path chartFolderPath = Path.of(Beatblocks.getPlugin().getDataFolder().getAbsolutePath() ,"charts");

	public static final Map<String, AbstractMap.SimpleEntry<Path, ChartStatus>> CHARTS = Collections.synchronizedMap(new HashMap<>());

	public static Path getChartPath(String chartName) {
		return CHARTS.get(chartName).getKey();
	}

	public static ChartStatus getChartStatus(String chartName) {
		return CHARTS.get(chartName).getValue();
	}

	/**
	 * May not point an absolute sound file.
	 */
	public static Path getSoundPath(String chartName, String soundFileName) {
		return Path.of(chartFolderPath.toString(), chartName, soundFileName + ".ogg");
	}

	/**
	 * The param {@code chartPath} should point a chart file, valid or not. Caution when using.
	 */
	public static void setFileStatus(Path chartPath, ChartStatus status) {
		List<Map.Entry<String, AbstractMap.SimpleEntry<Path, ChartStatus>>> entries =
			CHARTS.entrySet().stream().filter(entry -> entry.getValue().getKey().equals(chartPath)).toList();
		if(entries.size() == 1 || entries.size() == 0) {
			CHARTS.put(entries.get(0).getKey(), new AbstractMap.SimpleEntry<>(entries.get(0).getValue().getKey(), status));
		}
	}

	/**
	 * Loads all charts except those that could not be loaded. This method blocks using {@link Charts#listCharts()}
	 */
	public static void loadAll() {
		Bukkit.getLogger().info("Loading all charts");
		listCharts();
		DefaultParser parser = new DefaultParser();
		List<AbstractMap.SimpleEntry<String, CompletableFuture<Chart>>> futures = new ArrayList<>();
		CHARTS.keySet().forEach(consumer(key -> futures.add(new AbstractMap.SimpleEntry<>(key, parser.readChartAsync(getChartPath(key))))));
		futures.forEach(entry -> {
			try {
				entry.getValue().get();
			} catch (ExecutionException e) {
				throw new UncheckedThrowable(e.getCause());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public static void clearChartList() {
		CHARTS.clear();
	}

	public static final String BIC_EXTENSION = ".bic";

	/**
	 * Silently ignores unexpected files.
	 */
	public static CompletableFuture<Void> listChartsAsync() {
		return CompletableFuture.runAsync(Charts::listCharts);
	}

	/**
	 * Silently ignores unexpected files.
	 */
	public static void listCharts() {
		try (Stream<Path> listOuter = Files.list(chartFolderPath)) {
			listOuter.filter(Files::isDirectory).forEach(consumer(pathOuter -> {
				try (Stream<Path> listInner = Files.list(pathOuter)) {
					List<Path> chartPaths = listInner.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().endsWith(BIC_EXTENSION)).toList();
					if(chartPaths.size() == 1) {
						chartPaths.forEach(consumer(pathInner -> {
							Bukkit.getLogger().info("Found chart file " + chartFolderPath.relativize(pathInner));
							CHARTS.put(pathOuter.getFileName().toString(), new AbstractMap.SimpleEntry<>(pathInner, ChartStatus.NOT_LOADED));
						}));
					}
					else if(chartPaths.size() == 0) {
						Bukkit.getLogger().info("No bic files at " + pathOuter.toAbsolutePath());
					}
					else {
						Bukkit.getLogger().warning("Two or more chart files at " + pathOuter + "; skipping");
					}
				}
			}));
		} catch (IOException e) {
			throw new UncheckedThrowable(e);
		}
	}

	public enum ChartStatus {
		/**
		 * This does not assure that the current chart file is valid; this just shows that this chart file has once been read successfully.
		 */
		LOADED("Loaded", ChatColor.GREEN),
		NEEDS_REWRITE(LOADED),
		INVALID_FORMAT("Invalid chart file", ChatColor.RED),
		VERSION_MISMATCH("Version does not match", ChatColor.RED),
		NO_SOUND_FILE("No sound file", ChatColor.RED),
		NOT_LOADED("Not loaded", ChatColor.GRAY);

		public final String display;
		public final ChatColor color;

		ChartStatus(String display, ChatColor color) {
			this.display = display;
			this.color = color;
		}

		ChartStatus(ChartStatus parent) {
			this.display = parent.display;
			this.color = parent.color;
		}
	}
}
