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

	private final CompletableFuture<Void> stages;
	public NgrokClient ngrokClient;
	public Tunnel tunnel;
	public String publicUrl;

	public PackServer(Audience sender) {
		stages = CompletableFuture.runAsync(() -> {
			Logger.getLogger(String.valueOf(NgrokClient.class)).setLevel(Level.OFF);
			Logger.getLogger(String.valueOf(NgrokProcess.class)).setLevel(Level.OFF);
			Path ngrokPath = Beatblocks.getPlugin().getDataFolder().toPath().toAbsolutePath().resolve("ngrok");
			if(!Files.isRegularFile(ngrokPath.resolve("ngrok.exe"))) {
				sender.sendMessage(Component.text("Downloading ngrok. This will take some time."));
				logger.info("Installing ngrok...");
			}
			ngrokClient = new NgrokClient.Builder().withNgrokProcess(new NgrokProcess(
				new JavaNgrokConfig.Builder()
					.withNgrokPath(ngrokPath.resolve("ngrok.exe"))
					.withConfigPath(ngrokPath.resolve("ngrok.yml"))
					.withRegion(Region.JP)
					.build(),
				new NgrokInstaller())
			).build();
			try {
				Files.delete(ngrokPath.resolve("ngrok.zip"));
			} catch (IOException e) {
				throw new UncheckedThrowable("Failed to delete downloaded ngrok zip file", e);
			}
			CreateTunnel createTunnel = new CreateTunnel.Builder().withAddr("file:///" + ResourceBuilder.OutPath.getParent()).withBindTls(false).build();
			try {
				tunnel = ngrokClient.connect(createTunnel);
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
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
		publicUrl = tunnel.getPublicUrl();
		String packURL = publicUrl + "/" + ResourceBuilder.RPName;
		logger.info("Resource pack download URL: " + packURL);
		return packURL;
	}

	public void close() {
		Bukkit.getScheduler().runTaskAsynchronously(Beatblocks.getPlugin(), () -> {
			RPAppliedEvent.awaitDownload();
			logger.info("Server closing");
			ngrokClient.disconnect(tunnel.getPublicUrl());
			ngrokClient.kill();
		});
	}
}
