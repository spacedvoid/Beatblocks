package net.spacedvoid.beatblocks.resourcepack;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.singleplayer.exceptions.ResourceBuildException;
import org.bukkit.Bukkit;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PackServer {
	private static final int DEFAULT_PORT = 25555;
	private static HttpServer server = null;

	public static String create(Path pack) {
		if(!Files.isRegularFile(pack)) throw new ResourceBuildException("Failed to find built resource pack");
		int port = getPort();
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		server.createContext("/get", new PackHandler(pack));
		server.setExecutor(null);
		server.start();
		return "http://localhost:" + port + "/get";
	}

	public static void stop() {
		if(server != null) server.stop(10);
	}

	public static int getPort() {
		int port = getUncheckedPort();
		return checkPort(port)? port : DEFAULT_PORT;
	}

	public static int getUncheckedPort() {
		return Beatblocks.getPlugin().getConfig().getInt("pack-port");
	}

	private static boolean checkPort(int port) {
		if(port <= 0 || port > 65535) {
			Bukkit.getScheduler().runTask(Beatblocks.getPlugin(), () -> Bukkit.getLogger().warning("Port " + getUncheckedPort() + " exceeds its range; reset to default " + DEFAULT_PORT));
			Beatblocks.getPlugin().getConfig().set("pack-port", DEFAULT_PORT);
			return false;
		}
		return true;
	}

	static class PackHandler implements HttpHandler {
		private final Path packPath;

		public PackHandler(Path packPath) {
			this.packPath = packPath;
		}

		@Override
		public void handle(HttpExchange t) throws IOException {
			String response;
			try (InputStream stream = new FileInputStream(packPath.toFile())) {
				response = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
			}
			t.getResponseHeaders().set("Content-Type", "application/zip; charset=UTF-8");
			t.getResponseHeaders().set("Accept-Ranges", "bytes");
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes(StandardCharsets.UTF_8));
			os.close();
		}
	}
}
