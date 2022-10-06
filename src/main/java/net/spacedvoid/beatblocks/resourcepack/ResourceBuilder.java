package net.spacedvoid.beatblocks.resourcepack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.common.charts.Charts;
import net.spacedvoid.beatblocks.common.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.common.exceptions.CommandFailedException;
import net.spacedvoid.beatblocks.singleplayer.exceptions.ResourceBuildException;
import net.spacedvoid.beatblocks.util.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static net.spacedvoid.beatblocks.util.FileUtils.createFile;

//TODO: Fix resource building not working
public class ResourceBuilder {
	private static volatile boolean lock = false;

	public static void buildAsync(Audience sender, boolean includeUnloaded, boolean hostPack) {
		if(!lock) {
			lock = true;
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> build(sender, includeUnloaded, hostPack));
			buildTask(future);
		} else throw new BeatblocksException("A build is currently on progress!");
	}

	private static void build(Audience sender, boolean includedUnloaded, boolean hostPack) {
		File buildDir = new File(Beatblocks.getPlugin().getDataFolder().getAbsolutePath() + "/" + "resourcepack");
		if(!buildDir.exists()) {
			try {
				Files.createDirectories(buildDir.toPath());
			} catch (IOException e) {
				throw new ResourceBuildException("The build directory cannot be created", e);
			}
		}
		else {
			final CommandFailedException[] thrown = {null};
			try (Stream<Path> walk = Files.walk(buildDir.toPath()).sorted(Comparator.reverseOrder())) {
				walk.forEach(path -> {
					try {
						Files.delete(path);
					} catch (IOException e) {
						throw thrown[0] = new CommandFailedException("Failed to delete original file", e);
					}
				});
			} catch (IOException | CommandFailedException e) {
				thrown[0] = new CommandFailedException("Failed to walk existing build directory", e).suppress(thrown[0]);
			}
			if(thrown[0] != null) throw thrown[0];
		}
		File mcmetaFile = new File(buildDir.getPath() + "/pack.mcmeta");
		File soundsFolder = new File(buildDir.getPath() + "/assets/beatblocks/sounds");
		File soundsJsonFile = new File(soundsFolder.getPath() + "/sounds.json");
		try {
			Files.createDirectories(soundsFolder.toPath());
			createFile(mcmetaFile.toPath());
			createFile(soundsJsonFile.toPath());
		} catch (IOException e) {
			throw new ResourceBuildException("Failed to create files", e);
		}
		try (FileWriter writer = new FileWriter(mcmetaFile)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			//noinspection unused,FieldMayBeFinal
			gson.toJson(new MetaData(), writer);
		} catch (JsonIOException | IOException e) {
			throw new ResourceBuildException("Failed to write mcmeta file", e);
		}
		getDefaultResources().forEach(path -> {
			Path resourcePath = Path.of(buildDir.getPath(), "assets", "beatblocks", path);
			try {
				FileUtils.createFile(resourcePath);
			} catch (IOException e) {
				throw new RuntimeException("Failed to add default resources", e);
			}
		});
		if(includedUnloaded) Charts.loadAll();
		Map<String, SoundArray> soundMap = new HashMap<>();
		try (Writer writer = new FileWriter(soundsJsonFile)) {
			Charts.CHARTS.keySet().stream().filter(key -> Charts.getChartStatus(key) == Charts.ChartStatus.LOADED).forEach(key -> {
				File soundFile = new File(Charts.getSoundPath(key));
				if(!soundFile.exists() || !soundFile.isFile()) {
					Bukkit.getLogger().warning("The sound file " + soundFile.getPath() + " cannot be found or is not a file");
					return;
				}
				if(!soundFile.getName().endsWith(".ogg")) Bukkit.getLogger().warning("Sound file " + soundFile.getPath() + " is not an ogg file");
				Bukkit.getLogger().info(Charts.chartFolderPath + "\n" + soundFile.getPath());
				String relativePath = Charts.chartFolderPath.relativize(soundFile.toPath()).toString();
				relativePath = relativePath.substring(0, relativePath.lastIndexOf(".ogg"));
				String soundKey = relativePath.replace(File.separator, ".");
				String soundValue = relativePath.replace(File.separator, "/");
				if(!soundKey.matches("^[a-z0-9_.-]+$")) {
					Bukkit.getLogger().warning("Sound path " + soundValue + "contains illegal characters; skipping");
					return;
				}
				try {
					Path destPath = Path.of(soundsFolder.getPath() + "/" + relativePath + ".ogg");
					Files.createDirectories(destPath.getParent());
					Files.copy(soundFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					Bukkit.getLogger().warning("Failed to copy sound file to build dir:" + new CommandFailedException(e).getMessage());
					return;
				}
				soundMap.put(soundKey, new SoundArray(soundValue));
			});
			new GsonBuilder().setPrettyPrinting().create().toJson(soundMap, writer);
		} catch (JsonIOException | IOException e) {
			throw new ResourceBuildException("Failed to write sounds.json", e);
		}
		Path path = new ZipUtils().zip(buildDir.getPath(), Beatblocks.getPlugin().getDataFolder().getPath() + "/beatblocks-resource.zip");
		Bukkit.getScheduler().runTask(Beatblocks.getPlugin(), () -> sender.sendMessage(Component.text(ChatColor.GREEN + "Finished building resources.")));
		if(!hostPack) return;
		byte[] hash;
		try (DigestInputStream digestStream = new DigestInputStream(new BufferedInputStream(new FileInputStream(path.toFile())), MessageDigest.getInstance("SHA-1"))) {
			//noinspection StatementWithEmptyBody
			while(digestStream.read() != -1);
			hash = digestStream.getMessageDigest().digest();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage() + "\nIf you see this message, report to me.");
		}
		Bukkit.getScheduler().runTask(Beatblocks.getPlugin(), () -> sender.sendMessage(Component.text(ChatColor.GREEN + "Starting localhost server at port " + PackServer.getPort())));
		String url = PackServer.create(path);
		Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.setResourcePack(url, hash)), 5);
		PackServer.stop();
	}

	public static List<String> getDefaultResources() {
		URI uri;
		String resourcePath = "/resources";
		try {
			uri = Objects.requireNonNull(ResourceBuilder.class.getResource(resourcePath), "Cannot find resource directory").toURI();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		if(uri.getScheme().equals("jar")) {
			try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap()); Stream<Path> walk = Files.walk(fileSystem.getPath(resourcePath))) {
				return walk.filter(Files::isRegularFile).map(path -> path.toString().substring(resourcePath.length() + 1)).toList();
			} catch (IOException e) {
				throw new RuntimeException("Failed to get default resources", e);
			}
		} else {
			throw new IllegalStateException("Called from IDE");
		}
	}

	public static void buildTask(CompletableFuture<Void> future) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if(future.isDone()) {
					this.cancel();
					lock = false;
					future.join();
				}
			}
		}.runTaskTimer(Beatblocks.getPlugin(), 0, 1);
	}

	@SuppressWarnings("unused")
	private static class MetaData {
		public final Pack pack = new Pack();
	}

	@SuppressWarnings("unused")
	private static class Pack {
		public final int pack_format = 9;
		public final String description = "Resourcepack for Beatblocks";
	}

	@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
	private static class SoundArray {
		List<SoundArrayElement> sounds = new ArrayList<>();

		public SoundArray(String path) {
			sounds.add(new SoundArrayElement(path));
		}
	}

	@SuppressWarnings("unused")
	private static class SoundArrayElement {
		public final String path;
		public final boolean stream = true;
		public final boolean preload = true;

		public SoundArrayElement(String path) {
			this.path = path;
		}
	}
}
