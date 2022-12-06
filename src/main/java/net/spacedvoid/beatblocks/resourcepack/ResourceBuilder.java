package net.spacedvoid.beatblocks.resourcepack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.common.chart.Chart;
import net.spacedvoid.beatblocks.common.charts.Charts;
import net.spacedvoid.beatblocks.common.events.RPAppliedEvent;
import net.spacedvoid.beatblocks.common.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.common.exceptions.ResourceBuildException;
import net.spacedvoid.beatblocks.common.exceptions.UncheckedThrowable;
import net.spacedvoid.beatblocks.common.parser.DefaultParser;
import net.spacedvoid.beatblocks.util.ExceptionUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static net.spacedvoid.beatblocks.util.FileUtils.createFile;

public class ResourceBuilder {
	static final String RPName = "beatblocks-resource.zip";
	static final Path OutPath = Beatblocks.getPlugin().getDataFolder().toPath().toAbsolutePath().resolve("out");
	private static volatile boolean lock = false;

	public static void buildAsync(Audience sender, boolean includeUnloaded) {
		if(!lock) {
			lock = true;
			int players = Bukkit.getOnlinePlayers().size();
			CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> build(sender, includeUnloaded)).thenAcceptAsync(path -> {
				if(players > 0) hostPack(sender, path);
			});
			buildTask(future);
		} else throw new BeatblocksException("A build is currently on progress!", false);
	}

	private static Path build(Audience sender, boolean includedUnloaded) {
		File buildDir = new File(Beatblocks.getPlugin().getDataFolder().getAbsolutePath() + "/" + "resourcepack");
		if(!buildDir.exists()) {
			try {
				Files.createDirectories(buildDir.toPath());
			} catch (IOException e) {
				throw new ResourceBuildException("The build directory cannot be created", e);
			}
		}
		else {
			final ResourceBuildException[] thrown = {null};
			try (Stream<Path> walk = Files.walk(buildDir.toPath()).sorted(Comparator.reverseOrder())) {
				walk.forEach(path -> {
					try {
						Files.delete(path);
					} catch (IOException e) {
						throw thrown[0] = new ResourceBuildException("Failed to delete original file", e);
					}
				});
			} catch (IOException e) {
				if(thrown[0] != null) throw new ResourceBuildException("Failed to walk existing build directory", e).suppress(thrown[0]);
				else throw new ResourceBuildException("Failed to walk existing build directory", e);
			}
		}
		File mcmetaFile = new File(buildDir.getPath() + "/pack.mcmeta");
		File soundsFolder = new File(buildDir.getPath() + "/assets/beatblocks/sounds");
		File soundsJsonFile = new File(buildDir.getPath() + "/assets/beatblocks/sounds.json");
		try {
			Files.createDirectories(soundsFolder.toPath());
			createFile(mcmetaFile.toPath());
			createFile(soundsJsonFile.toPath());
		} catch (IOException e) {
			throw new ResourceBuildException("Failed to create files", e);
		}
		try (FileWriter writer = new FileWriter(mcmetaFile, StandardCharsets.UTF_8)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			//noinspection unused
			gson.toJson(new MetaData(), writer);
		} catch (JsonIOException | IOException e) {
			throw new ResourceBuildException("Failed to write mcmeta file", e);
		}
		getDefaultResources().forEach(path -> {
			Path resourcePath = Path.of(buildDir.getPath(), "assets", "minecraft", path.substring(11));
			try (InputStream fileStream = ResourceBuilder.class.getResourceAsStream(path)) {
				if(fileStream == null) throw new FileNotFoundException("Failed to find default resource " + path);
				Files.createDirectories(resourcePath.getParent());
				Files.copy(fileStream, resourcePath);
			} catch (IOException e) {
				throw new UncheckedThrowable("Failed to add default resources", e);
			}
		});
		if(includedUnloaded) Charts.loadAll();
		Map<String, SoundArray> soundMap = new HashMap<>();
		try (Writer writer = new FileWriter(soundsJsonFile, StandardCharsets.UTF_8)) {
			DefaultParser parser = new DefaultParser();
			Charts.CHARTS.entrySet().stream().filter(entry -> Charts.getChartStatus(entry.getKey()) == Charts.ChartStatus.LOADED).forEach(entry -> {
				Chart chart = parser.readChart(entry.getValue().getKey());
				Path soundFile = Charts.getSoundPath(entry.getKey(), chart.getString(Chart.soundFile));
				if(!Files.isRegularFile(soundFile)) {
					Bukkit.getLogger().warning("The sound file " + soundFile + " cannot be found or is not a file");
					return;
				}
				if(!soundFile.getFileName().toString().endsWith(".ogg")) Bukkit.getLogger().warning("Sound file " + soundFile + " is not an ogg file");
				String relativePath = Charts.chartFolderPath.relativize(soundFile).toString();
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
					Files.copy(soundFile, destPath, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					Bukkit.getLogger().warning("Failed to copy sound file to build dir:" + ExceptionUtil.getFullMessage(e));
					return;
				}
				soundMap.put(soundKey, new SoundArray(soundValue));
				Bukkit.getLogger().info("Included chart " + entry.getKey());
			});
			new GsonBuilder().setPrettyPrinting().create().toJson(soundMap, writer);
		} catch (JsonIOException | IOException e) {
			throw new ResourceBuildException("Failed to write sounds.json", e);
		}
		Path path = new ZipUtils().zip(buildDir.getPath(), OutPath.resolve(RPName).toString());
		Bukkit.getScheduler().runTask(Beatblocks.getPlugin(), () -> sender.sendMessage(Component.text(ChatColor.GREEN + "Finished building resources.")));
		return path;
	}

	private static void hostPack(Audience sender, Path path) {
		PackServer server = new PackServer(sender);
		Bukkit.getScheduler().runTask(Beatblocks.getPlugin(), () -> sender.sendMessage(Component.text(ChatColor.GREEN + "Starting ngrok http tunnel")));
		byte[] hash;
		try (DigestInputStream digestStream = new DigestInputStream(new BufferedInputStream(new FileInputStream(path.toFile())), MessageDigest.getInstance("SHA-1"))) {
			//noinspection StatementWithEmptyBody
			while(digestStream.read() != -1);
			hash = digestStream.getMessageDigest().digest();
		} catch (IOException e) {
			throw new UncheckedThrowable(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage() + "\nIf you see this message, report to me.");
		}
		String hash1 = bytesToHex(hash);
		Bukkit.getLogger().info(hash1);
		String url = server.getPublicURL();
		Bukkit.getScheduler().runTask(Beatblocks.getPlugin(), () -> {
			Bukkit.getOnlinePlayers().forEach(player -> {
				player.setResourcePack(url, hash);
				RPAppliedEvent.track(player);
			});
			server.close();
		});
	}

	@SuppressWarnings("SpellCheckingInspection")
	private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
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
				return walk.filter(Files::isRegularFile).map(path -> path.toString().substring(path.toString().lastIndexOf(resourcePath))).toList();
			} catch (IOException e) {
				throw new UncheckedThrowable(e);
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

	// -- Classes for pack.mcmeta json creation --

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
		public final String name;
		public final boolean stream = true;
		public final boolean preload = true;

		public SoundArrayElement(String name) {
			this.name = "beatblocks:" + name;
		}
	}
}
