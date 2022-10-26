package net.spacedvoid.beatblocks.resourcepack;

import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.conf.JavaNgrokConfig;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import net.spacedvoid.beatblocks.singleplayer.exceptions.ResourceBuildException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

public class PackServer {
	private NgrokClient ngrokClient;
	private final CompletableFuture<Void> stages = new CompletableFuture<>();
	private String URL;
	private Tunnel tunnel;

	public PackServer() {
		//noinspection SpellCheckingInspection
		stages.thenRunAsync(() -> ngrokClient = new NgrokClient.Builder().withJavaNgrokConfig(
			new JavaNgrokConfig.Builder().withAuthToken("1mUcg7t3PiZeuuj7plxKZUrUqNZ_6CMZi9sz8C1Tz5sfW2RWC").withNgrokPath(Path.of("C:/Personal/ngrok/ngrok.exe")).build()
		).build());
	}

	public PackServer supplyPath(Path packPath) {
		if(!Files.isRegularFile(packPath)) throw new ResourceBuildException("Failed to find resource pack");
		stages.thenRunAsync(() -> {
			CreateTunnel createTunnel = new CreateTunnel.Builder().withAddr("file:///" + packPath.toAbsolutePath()).build();
			tunnel = ngrokClient.connect(createTunnel);
			URL = tunnel.getPublicUrl();
		});
		return this;
	}

	public String getPublicURL() {
		if(!stages.isDone()) stages.join();
		return URL;
	}

	public void close() {
		new Timer().schedule(new TimerTask() {
			public void run() {
				ngrokClient.disconnect(URL);
			}
		}, 10000);
	}
}
