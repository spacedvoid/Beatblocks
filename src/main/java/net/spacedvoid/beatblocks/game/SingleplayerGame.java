package net.spacedvoid.beatblocks.game;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.title.Title;
import net.spacedvoid.beatblocks.Beatblocks;
import net.spacedvoid.beatblocks.chart.Chart;
import net.spacedvoid.beatblocks.events.NotePressedEvent;
import net.spacedvoid.beatblocks.exceptions.UncheckedThrowable;
import net.spacedvoid.beatblocks.structures.Board;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SingleplayerGame implements GameInstance {
	private final Chart chart;
	private final Player player;
	private final Board.TypedBoard board;
	private int noteIndex = 0;
	/** Includes finishing process. */
	private boolean ended = false;
	private final List<NoteEntity> notes = Collections.synchronizedList(new ArrayList<>());
	private final Judgement.JudgementCounter counter = Judgement.createCounter();
	
	private int currentTiming = 0;

	private SingleplayerGame(Chart chart, Player player, Board.TypedBoard board) {
		this.chart = chart;
		this.player = player;
		this.board = board;
	}
	
	public static SingleplayerGame create(CompletableFuture<Chart> future, Board.TypedBoard board, Player player) {
		if(board.getType() != Board.Type.SINGLEPLAYER) throw new IllegalArgumentException("Board not singleplayer");
		Board.ViewableBoard viewableBoard = board.getViewable().get(0);
		Location playerLocation = viewableBoard.getBoardLocation().clone();
		playerLocation.setY(playerLocation.getBlockY() + 5);
		switch(viewableBoard.getFace()) {
			case NORTH -> playerLocation.setYaw(180);
			case EAST -> playerLocation.setYaw(270);
			case SOUTH -> playerLocation.setYaw(0);
			case WEST -> playerLocation.setYaw(90);
		}
		playerLocation.setPitch(45);
		player.setGameMode(GameMode.CREATIVE);
		player.setFlying(true);
		player.teleport(playerLocation);
		NotePressedEvent.include(player);
		//TODO: Intro behavior
		player.showTitle(Title.title(Component.text(""), Component.text("준비"), Title.Times.times(Duration.ZERO, Duration.ofMillis(1500), Duration.ofMillis(500))));
		player.playSound(Sound.sound(Key.key("ui.button.click"), Sound.Source.RECORD, 1, 1), Sound.Emitter.self());
		Chart chart;
		if(!future.isDone()) player.sendMessage(Component.text("Force loading chart. This may cause server lag."));
		try {
			chart = future.get();
		} catch(ExecutionException e) {
			throw new UncheckedThrowable(e.getCause());
		} catch(InterruptedException e) {
			throw new RuntimeException("Failed to load chart", e);
		}
		SingleplayerGame instance = new SingleplayerGame(chart, player, board);
		Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(), instance::spawnNotes, instance.getStartDelay());
		Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(),
				() -> player.playSound(Sound.sound(Key.key("beatblocks:" + chart.chartName + "." + chart.getString(Chart.soundFile)), Sound.Source.RECORD, 1.0f, 1.0f), Sound.Emitter.self()),
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
				chart.getInteger(Chart.offset) + instance.getStartDelay());
		return instance;
	}
	
	private void spawnNotes() {
		new BukkitRunnable() {
			@Override
			public void run() {
				if(ended) {
					this.cancel();
					return;
				}
				if(noteIndex >= chart.notes.size()) {
					Game.stop(player, false);
					this.cancel();
					return;
				}
				Chart.ChartNote chartNote = chart.notes.get(noteIndex);
				if(currentTiming == chartNote.info.timing) {
					addNoteEntity(NoteEntity.create(SingleplayerGame.this, chartNote.info));
					noteIndex++;
				}
			}
		}.runTaskTimer(Beatblocks.getPlugin(), 0, 1);
	}
	
	@Override
	public Game.Type getGameType() {
		return Game.Type.SINGLEPLAYER;
	}
	
	@Override
	public void processNote(Player ignored, int lane) {
		Optional<NoteEntity> optional = notes.stream().filter(entity -> entity.info.lane == lane).min(Comparator.comparingInt(entity -> entity.info.timing));
		optional.ifPresent(NoteEntity::process);
	}

	private void addNoteEntity(List<NoteEntity> entities) {
		notes.addAll(entities);
	}

	@Override
	public void endGame(boolean force) {
		ended = true;
		NotePressedEvent.exclude(player);
		if(force) {
			synchronized (notes) {
				while(notes.size() > 0) {
					notes.get(0).delete();
				}
			}
			player.stopSound(SoundCategory.RECORDS);
		} else {
			new BukkitRunnable() {
				@Override
				public void run() {
					if(!notes.isEmpty()) return;
					showResults();
					this.cancel();
				}
			}.runTaskTimer(Beatblocks.getPlugin(), 0, 1);
		}
	}

	private void showResults() {
		TextComponent.Builder builder = Component.text().append(Component.text("Result: \n"));
		Judgement[] values = Judgement.values();
		for(int i = 0; i < values.length; i++) {
			Judgement judgement = values[i];
			builder.append(judgement.text);
			builder.append(Component.text(" : "));
			builder.append(Component.text(counter.get(judgement)));
			if(i != values.length - 1) builder.append(Component.text("\n"));
		}
		player.sendMessage(builder);
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
		return List.of(player);
	}
	
	@Override
	public Judgement.JudgementCounter getCounter() {
		return counter;
	}
	
	/**
	 * Ensures <code>{@link #getGameType()}.{@link Game.Type#matches(Board.Type) matches}(getBoard().type) == true</code>
	 */
	@Override
	public Board.TypedBoard getBoard() {
		return this.board;
	}
	
	@Override
	public boolean removeNote(NoteEntity noteEntity) {
		return notes.remove(noteEntity);
	}
}
