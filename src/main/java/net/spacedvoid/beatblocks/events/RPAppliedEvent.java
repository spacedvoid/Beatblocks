package net.spacedvoid.beatblocks.events;

import net.spacedvoid.beatblocks.exceptions.UncheckedThrowable;
import net.spacedvoid.beatblocks.util.BLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class RPAppliedEvent implements Listener {
	private static final BLogger logger = new BLogger("PackWaiter");

	private static final Set<UUID> requestedPlayers = Collections.synchronizedSet(new HashSet<>());
	private static CountDownLatch latch;

	@EventHandler
	public void onRPApply(PlayerResourcePackStatusEvent event) {
		if(isDone(event.getStatus())) untrack(event.getPlayer(), event.getStatus());
	}

	private boolean isDone(PlayerResourcePackStatusEvent.Status status) {
		return switch(status) {
			case FAILED_DOWNLOAD, SUCCESSFULLY_LOADED, DECLINED -> true;
			default -> false;
		};
	}

	public static void track(Player player) {
		requestedPlayers.add(player.getUniqueId());
		logger.info("Tracking player " + player.getName());
	}

	public static boolean isTracked(Player player) {
		return requestedPlayers.contains(player.getUniqueId());
	}

	@SuppressWarnings("SpellCheckingInspection")
	public static void untrack(Player player, @Nullable PlayerResourcePackStatusEvent.Status status) {
		requestedPlayers.remove(player.getUniqueId());
		String message = "Untracking player " + player.getName();
		if(status != null) message += " - Status: " + status;
		logger.info(message);
		if(latch != null) latch.countDown();
	}

	public static void awaitDownload() {
		latch = new CountDownLatch(requestedPlayers.size());
		logger.info("Waiting with " + latch.getCount() + " players");
		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new UncheckedThrowable(e);
		}
	}
}
