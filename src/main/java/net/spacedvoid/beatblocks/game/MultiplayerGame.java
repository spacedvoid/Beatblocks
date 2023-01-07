package net.spacedvoid.beatblocks.game;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.spacedvoid.beatblocks.Beatblocks;
import net.spacedvoid.beatblocks.chart.Chart;
import net.spacedvoid.beatblocks.events.NotePressedEvent;
import net.spacedvoid.beatblocks.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.exceptions.UncheckedThrowable;
import net.spacedvoid.beatblocks.structures.Board;
import net.spacedvoid.beatblocks.structures.BoardComponent;
import net.spacedvoid.beatblocks.structures.MultiplayerBoard;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MultiplayerGame implements GameInstance {
	private final Chart chart;
	private final MultiplayerBoard board;
	private final Map<Player, SingleplayerGame> slots = new HashMap<>(1, 1);    // 0: North, 1: South, 2: East, 3: West
	
	private int noteIndex = 0;
	private int currentTiming = 0;
	private boolean ended = false;
	private final List<NoteEntity> notes = Collections.synchronizedList(new ArrayList<>());
	private final Judgement.JudgementCounter counter = Judgement.createCounter();

	private MultiplayerGame(Chart chart, MultiplayerBoard board, Player... players) {
		this.chart = chart;
		this.board = board;
	}
	
	public static MultiplayerGame create(CompletableFuture<Chart> future, MultiplayerBoard board, Player... players) {
		if(board.getType() != Board.Type.MULTIPLAYER) throw new IllegalArgumentException("Board not multiplayer");
		checkCreatable(players);
		List<BoardComponent> components = board.toComponents();
		// TODO: Get player locations
		for(int i = 0; i < players.length; i++) {
			Location playerLocation = components.get(i).getBoardLocation().clone();
			int dx = 0, dz = 0;
			switch(i) {
				case 0 -> {
					playerLocation.setYaw(180);
				}
				case 1 -> {
					playerLocation.setYaw(270);
				}
				case 2 -> {
					playerLocation.setYaw(0);
				}
				case 3 -> {
					playerLocation.setYaw(90);
				}
			}
			playerLocation.add(dx, 0, dz);
			playerLocation.setPitch(45);
			players[i].setGameMode(GameMode.CREATIVE);
			players[i].setFlying(true);
			players[i].teleport(playerLocation);
			NotePressedEvent.include(players[i]);
			players[i].showTitle(Title.title(Component.text(""), Component.text("준비"), Title.Times.times(Duration.ZERO, Duration.ofMillis(1500), Duration.ofMillis(500))));
			players[i].playSound(Sound.sound(Key.key("ui.button.click"), Sound.Source.RECORD, 1, 1), Sound.Emitter.self());
		}
		Chart chart;
		if(!future.isDone()) players[0].sendMessage(Component.text("Force loading chart. This may cause server lag."));
		try {
			chart = future.get();
		} catch (ExecutionException e) {
			throw new UncheckedThrowable(e.getCause());
		} catch (InterruptedException e) {
			throw new RuntimeException("Failed to load chart", e);
		}
		MultiplayerGame instance = new MultiplayerGame(chart, board, players);
		Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(), instance::spawnNotes, instance.getStartDelay());
		Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(), () ->
			Arrays.stream(players).forEach(player ->
				player.playSound(Sound.sound(Key.key("beatblocks:" + chart.chartName + "." + chart.getString(Chart.soundFile)), Sound.Source.RECORD, 1, 1), Sound.Emitter.self())
			),
			chart.getInteger(Chart.offset) + instance.getStartDelay() + (long)NoteEntity.TIME
		);
		Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(), () ->
			new BukkitRunnable() {
				@Override
				public void run() {
					if(instance.hasEnded()) {
						this.cancel();
						return;
					}
					instance.currentTiming++;
				}
			}.runTaskTimer(Beatblocks.getPlugin(), 1, 1),
			chart.getInteger(Chart.offset) + instance.getStartDelay()
		);
		return instance;
	}
	
	private void spawnNotes() {
		// TODO
	}

	@Override
	public Game.Type getGameType() {
		return Game.Type.MULTIPLAYER;
	}
	
	@Override
	public void processNote(Player player, int lane) {
		// TODO
	}
	
	@Override
	public void endGame(boolean force) {
		// TODO
	}
	
	@Override
	public boolean hasEnded() {
		return ended;
	}
	
	@Override
	public int getCurrentTiming() {
		return currentTiming;
	}
	
	@Override
	public List<Player> getPlayers() {
		return slots.keySet().stream().toList();
	}
	
	@Override
	public Judgement.JudgementCounter getCounter() {
		return counter;
	}
	
	@Override
	public MultiplayerBoard getBoard() {
		return board;
	}

	@Override
	public EndStatus getEndStatus() {
		// TODO
		return null;
	}

	@Override
	public boolean removeNote(NoteEntity noteEntity) {
		return notes.remove(noteEntity);
	}
	
	/**
	 * @throws BeatblocksException If multiplayer game is not creatable
	 */
	public static void checkCreatable(@Nullable Player... players) {
		if(players == null || players.length <= 1) throw new BeatblocksException("Not enough players! (" + (players == null? 0 : players.length) + ")");
		else if(players.length > 4) throw new BeatblocksException("Too much players! (" + players.length + ")");
	}
}
