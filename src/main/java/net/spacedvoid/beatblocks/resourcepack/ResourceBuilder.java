package net.spacedvoid.beatblocks.resourcepack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.common.charts.Charts;
import net.spacedvoid.beatblocks.common.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.common.charts.ChartDisplayer;
import net.spacedvoid.beatblocks.singleplayer.exceptions.ResourceBuildException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static net.spacedvoid.beatblocks.resourcepack.FileUtils.createFile;

public class ResourceBuilder {
	private static boolean lock = false;

	public static void buildAsync(Audience sender) {
		if(!lock) {
			lock = true;
			Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(),
				new BuildTask(CompletableFuture.runAsync(ResourceBuilder::build).exceptionally(thrown -> { throw new BeatblocksException("Build failed", thrown); }), sender),
				1);
		}
		else throw new BeatblocksException("A build is currently on progress!");
	}

	private static void build() {
		File buildDir = new File(Beatblocks.getPlugin().getDataFolder().getPath() + "/" + "resourcepack");
		try {
			Files.createDirectories(buildDir.toPath());
		} catch (IOException e) {
			throw new ResourceBuildException("The build directory cannot be created", e);
		}
		File soundsFolder = new File(buildDir.getPath() + "/assets/beatblocks/sounds");
		File mcmeta = new File(buildDir.getPath() + "/pack.mcmeta");
		File soundsJson = new File(soundsFolder.getPath() + "/sounds.json");
		try (FileChannel buildChannel = FileChannel.open(buildDir.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE); FileLock buildLock = buildChannel.tryLock()) {
			if(buildLock == null) throw new ResourceBuildException("Failed to acquire lock because another program is having a lock for " + buildDir.getPath());
			try {
				Files.createDirectories(soundsFolder.toPath());
				createFile(mcmeta.toPath());
				createFile(soundsJson.toPath());
			} catch (IOException e) {
				throw new ResourceBuildException("Failed to create files", e);
			}
			try (Writer writer = new FileWriter(mcmeta)) {
				Gson gson = new GsonBuilder().create();
				//noinspection unused,FieldMayBeFinal
				gson.toJson(new Object() {
					Object pack = new Object() {
						int pack_format = 9;
						String description = "Sound pack generated by Beatblocks";
					};
				}, writer);
			} catch (JsonIOException e) {
				throw new ResourceBuildException("Failed to write mcmeta file", e);
			}
			try (FileChannel chartsChannel = FileChannel.open(Path.of(Charts.chartFolderPath), StandardOpenOption.READ); FileLock chartsLock = chartsChannel.tryLock()) {
				if(chartsLock == null) throw new ResourceBuildException("Failed to acquire lock because another program is having a lock for " + Charts.chartFolderPath);
				ChartDisplayer.listCharts();
				HashMap<String, String> sounds = new HashMap<>();
				Charts.CHARTS.entrySet().stream().filter(entry -> entry.getValue().getValue() == Charts.ChartStatus.LOADED)
					.forEach(entry -> {
						File soundFile = new File(entry.getValue().getKey().getValue());
						if(!soundFile.exists() || !soundFile.isFile()) throw new ResourceBuildException("The sound file " + soundFile.getPath() + " cannot be found or is not a file");
						//TODO: Write sounds.json, check if each sound is a valid ogg file
						String key = soundFile.getPath().substring(soundFile.getPath().indexOf(Charts.chartFolderPath) + Charts.chartFolderPath.length())
							.replace(File.separator, ".");
						String value = soundFile.getPath().substring(soundFile.getPath().indexOf(Charts.chartFolderPath) + Charts.chartFolderPath.length())
							.replace(File.separator, "/");
						sounds.put(key, value);
						try {
							Files.copy(soundFile.toPath(), Path.of(soundsFolder.getPath() + "/" + soundFile.getName()), StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							throw new ResourceBuildException("Failed to copy sound file to build dir", e);
						}
					});
				try (Writer writer = new FileWriter(soundsJson)) {
					new GsonBuilder().create().toJson(sounds, writer);
				} catch (JsonIOException e) {
					throw new ResourceBuildException("Failed to write sounds.json", e);
				}
			}
			new ZipUtils().zip(buildDir.getPath(), Beatblocks.getPlugin().getDataFolder().getPath() + "/beatblocks-resource.zip");
		} catch (OverlappingFileLockException e) {
			throw new ResourceBuildException("The build is currently on progress");
		} catch (IOException e) {
			throw new ResourceBuildException("Failed to build resource pack", e);
		}
	}

	public static void checkChartFolderLock() throws BeatblocksException {
		if(lock) throw new BeatblocksException("Lock is enabled at chart folder.");
	}

	private static class BuildTask implements Runnable {
		public BuildTask(CompletableFuture<Void> future, Audience sender) {
			this.future = future;
			this.sender = sender;
		}

		private final CompletableFuture<Void> future;
		private final Audience sender;

		@Override
		public void run() {
			if(future.isDone()) {
				lock = false;
				sender.sendMessage(Component.text(ChatColor.GREEN + "Finished building resources!"));
			}
			else Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(), new BuildTask(future, sender), 1);
		}
	}
}


