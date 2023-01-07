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
import net.spacedvoid.beatblocks.structures.SingleplayerBoard;
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
	private final Player player;
	private final SingleplayerBoard board;
	private int noteIndex = 0;
	/** Includes finishing process. */
	private GameInstance.EndStatus endStatus = EndStatus.NONE;
	private final List<NoteEntity> notes = Collections.synchronizedList(new ArrayList<>());
	private final Judgement.JudgementCounter counter = Judgement.createCounter();
	
	private int currentTiming = 0;

	private SingleplayerGame(Player player, SingleplayerBoard board) {
		this.player = player;
		this.board = board;
	}
	
	public static SingleplayerGame create(CompletableFuture<Chart> future, SingleplayerBoard board, Player player) {
		if(board.getType() != Board.Type.SINGLEPLAYER) throw new IllegalArgumentException("Board not singleplayer");
		Location playerLocation = board.getBoardLocation().clone();
		playerLocation.setY(playerLocation.getBlockY() + 5);
		switch(board.getPlayerFace()) {
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
		SingleplayerGame instance = new SingleplayerGame(player, board);
		Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(), () ->
			player.playSound(Sound.sound(Key.key("beatblocks:" + chart.chartName + "." + chart.getString(Chart.soundFile)), Sound.Source.RECORD, 1.0f, 1.0f), Sound.Emitter.self()),
			chart.getInteger(Chart.offset) + instance.getStartDelay()
		);
		new BukkitRunnable() {
			@Override
			public void run() {
				if(instance.endStatus == EndStatus.STOPPED) {
					this.cancel();
					return;
				}
				if(instance.endStatus == EndStatus.NONE) {
					if(instance.noteIndex >= chart.notes.size()) {
						Game.stop(player, false);
					}
					else {
						Chart.ChartNote chartNote = chart.notes.get(instance.noteIndex);
						if(instance.currentTiming == chartNote.info.timing) {
							instance.addNoteEntity(NoteEntity.create(instance, chartNote.info));
							instance.noteIndex++;
						}
					}
				}
				instance.currentTiming++;
			}
		}.runTaskTimer(Beatblocks.getPlugin(), instance.getStartDelay(), 1);
		return instance;
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
		if(this.hasEnded()) return;
		endStatus = EndStatus.IN_PROGRESS;
		if(force) {
			synchronized (notes) {
				while(notes.size() > 0) {
					notes.get(0).delete();
				}
			}
			player.stopSound(SoundCategory.RECORDS);
			NotePressedEvent.exclude(player);
			endStatus = EndStatus.STOPPED;
		} else {
			new BukkitRunnable() {
				@Override
				public void run() {
					if(!notes.isEmpty()) return;
					showResults();
					NotePressedEvent.exclude(player);
					endStatus = EndStatus.STOPPED;
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
			if(judgement.parent != null) builder.append(Component.text("  "));
			builder.append(judgement.text);
			builder.append(Component.text(" : "));
			builder.append(Component.text(counter.get(judgement)));
			if(i != values.length - 1) builder.append(Component.text("\n"));
		}
		player.sendMessage(builder);
	}

	@Override
	public boolean hasEnded() {
		return endStatus == EndStatus.STOPPED || endStatus == EndStatus.IN_PROGRESS;
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
	public SingleplayerBoard getBoard() {
		return this.board;
	}

	@Override
	public EndStatus getEndStatus() {
		return endStatus;
	}

	@Override
	public boolean removeNote(NoteEntity noteEntity) {
		return notes.remove(noteEntity);
	}
}
