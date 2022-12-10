package net.spacedvoid.beatblocks.resourcepack;

import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.conf.JavaNgrokConfig;
import com.github.alexdlaird.ngrok.installer.NgrokInstaller;
import com.github.alexdlaird.ngrok.process.NgrokProcess;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Region;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.common.events.RPAppliedEvent;
import net.spacedvoid.beatblocks.common.exceptions.UncheckedThrowable;
import net.spacedvoid.beatblocks.util.BLogger;
import net.spacedvoid.beatblocks.util.ExceptionUtil;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PackServer {
	private final BLogger logger = new BLogger("PackServer");
	public final Path ROOT = Beatblocks.getPlugin().getDataFolder().toPath().resolve("packserver").toAbsolutePath();

	private final CompletableFuture<Void> stages;
	public NgrokClient ngrokClient;
	public Tunnel tunnel;
	public String packName;

	public PackServer(Audience sender, String packName) {
		this.packName = packName;
		this.stages = CompletableFuture.runAsync(() -> {
			Logger.getLogger(String.valueOf(NgrokClient.class)).setLevel(Level.OFF);
			Logger.getLogger(String.valueOf(NgrokProcess.class)).setLevel(Level.OFF);
			Path ngrokPath = Beatblocks.getPlugin().getDataFolder().toPath().toAbsolutePath().resolve("ngrok");
			Path binary = ngrokPath.resolve("ngrok.exe");
			if(!Files.isRegularFile(binary)) {
				sender.sendMessage(Component.text("Downloading ngrok. This will take some time."));
				logger.info("Installing ngrok...");
			}
			ngrokClient = new NgrokClient.Builder().withNgrokProcess(new NgrokProcess(
				new JavaNgrokConfig.Builder()
					.withNgrokPath(binary)
					.withConfigPath(ngrokPath.resolve("ngrok.yml"))
					.withRegion(Region.JP)
					.build(),
				new NgrokInstaller())
			).build();
			try {
				Files.delete(ngrokPath.resolve("ngrok.zip"));
			} catch (IOException e) {
				sender.sendMessage(Component.text("" + ExceptionUtil.getFullMessage(e, true)));
			}
			CreateTunnel createTunnel = new CreateTunnel.Builder().withAddr("file:///" + ROOT).withBindTls(false).build();
			tunnel = ngrokClient.connect(createTunnel);
		}).exceptionallyAsync(exception -> {
			ngrokClient.kill();
			logger.warn("Build failed - See exception below");
			throw new UncheckedThrowable(exception);
		});
	}

	public String getPublicURL() {
		try {
			stages.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new UncheckedThrowable(e.getCause());
		}
		String packURL = tunnel.getPublicUrl() + "/" + packName;
		logger.info("Resource pack download URL: " + packURL);
		return packURL;
	}

	public void close() {
		Bukkit.getScheduler().runTaskAsynchronously(Beatblocks.getPlugin(), () -> {
			RPAppliedEvent.awaitDownload();
			logger.info("Pack server closing");
			ngrokClient.disconnect(tunnel.getPublicUrl());
			ngrokClient.kill();
		});
	}
}
