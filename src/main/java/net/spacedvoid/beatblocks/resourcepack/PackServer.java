package net.spacedvoid.beatblocks.resourcepack;

import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.conf.JavaNgrokConfig;
import com.github.alexdlaird.ngrok.installer.NgrokInstaller;
import com.github.alexdlaird.ngrok.process.NgrokProcess;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.common.events.RPAppliedEvent;
import net.spacedvoid.beatblocks.common.exceptions.ResourceBuildException;
import net.spacedvoid.beatblocks.common.exceptions.UncheckedThrowable;
import net.spacedvoid.beatblocks.util.BLogger;
import org.bukkit.Bukkit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PackServer {
	private final BLogger logger = new BLogger("PackServer");

	private final Executor executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "pack-worker"));
	private final CompletableFuture<Void> stages;
	private int step;
	public NgrokClient ngrokClient;
	public Tunnel tunnel;
	public String publicUrl;
	public CountDownLatch latch = new CountDownLatch(1);

	public PackServer(Audience sender) {
		step = 1;
		stages = CompletableFuture.runAsync(() -> {
			Logger.getLogger(String.valueOf(NgrokClient.class)).setLevel(Level.OFF);
			Logger.getLogger(String.valueOf(NgrokProcess.class)).setLevel(Level.OFF);
			Path ngrokPath = Beatblocks.getPlugin().getDataFolder().toPath().toAbsolutePath().resolve("ngrok");
			if(!Files.isRegularFile(ngrokPath.resolve("ngrok.exe"))) {
				sender.sendMessage(Component.text("Downloading ngrok. This will take some time."));
				logger.info("Installing ngrok...");
			}
			//noinspection SpellCheckingInspection
			ngrokClient = new NgrokClient.Builder().withNgrokProcess(new NgrokProcess(
				new JavaNgrokConfig.Builder()
					.withNgrokPath(ngrokPath.resolve("ngrok.exe"))
					.withConfigPath(ngrokPath.resolve("ngrok.yml"))
					.withAuthToken("1mUcg7t3PiZeuuj7plxKZUrUqNZ_6CMZi9sz8C1Tz5sfW2RWC")
					.build(),
				new NgrokInstaller())
			).build();
			ngrokClient.getNgrokProcess().start();
		}, executor).exceptionallyAsync(exception -> {
			ngrokClient.kill();
			logger.exception("Build failed at stage 1", new UncheckedThrowable(exception));
			return null;
		}, executor);
	}

	public void supplyPath(Path packPath) {
		if(!Files.isRegularFile(packPath)) throw new ResourceBuildException("Failed to find resource pack");
		if(step != 1) throw new IllegalStateException();
		step = 2;
		stages.thenRunAsync(() -> {
			CreateTunnel createTunnel = new CreateTunnel.Builder().withAddr("file:///" + packPath.toAbsolutePath().getParent()).withBindTls(false).build();
			try {
				tunnel = ngrokClient.connect(createTunnel);
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			latch.countDown();
		}, executor).exceptionallyAsync(exception -> {
			ngrokClient.kill();
			logger.exception("Build failed at stage 2", new UncheckedThrowable(exception));
			return null;
		}, executor);
	}

	public String getPublicURL() {
		if(step != 2) throw new IllegalStateException();
		step = 3;
		try {
			stages.get();
			latch.await();
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
		if(step != 3) throw new IllegalStateException();
		step = 4;
		Bukkit.getScheduler().runTaskAsynchronously(Beatblocks.getPlugin(), () -> {
			RPAppliedEvent.awaitDownload();
			logger.info("Server closing");
			ngrokClient.disconnect(tunnel.getPublicUrl());
			ngrokClient.kill();
		});
	}

}
