package net.spacedvoid.beatblocks.game;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.title.Title;
import net.spacedvoid.beatblocks.Beatblocks;
import net.spacedvoid.beatblocks.chart.Chart;
import net.spacedvoid.beatblocks.chart.NoteInfo;
import net.spacedvoid.beatblocks.events.NotePressedEvent;
import net.spacedvoid.beatblocks.exceptions.UncheckedThrowable;
import net.spacedvoid.beatblocks.structures.Board;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GameInstance {
	public final Chart chart;
	private final Player player;
	public final Board board;
	private int noteIndex = 0;
	/** Includes finishing process. */
	private boolean finished = false;
	private final List<NoteEntity> notes = Collections.synchronizedList(new ArrayList<>());
	private final Judgement.JudgementCounter counter = Judgement.createCounter();

	private final int START_DELAY = 40;

	private int currentTiming = 0;

	private GameInstance(Chart chart, Player player, Board board) {
		this.chart = chart;
		this.player = player;
		this.board = board;
	}

	public static GameInstance create(Player player, CompletableFuture<Chart> future, String chartName, Board board) {
		Location playerLocation = board.boardLocation.clone();
		playerLocation.setY(playerLocation.getBlockY() + 5);
		switch(board.face) {
			case NORTH -> playerLocation.setYaw(0);
			case EAST -> playerLocation.setYaw(90);
			case SOUTH -> playerLocation.setYaw(180);
			case WEST -> playerLocation.setYaw(270);
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
		} catch (ExecutionException e) {
			throw new UncheckedThrowable(e.getCause());
		} catch (InterruptedException e) {
			throw new RuntimeException("Failed to load chart", e);
		}
		GameInstance instance = new GameInstance(chart, player, board);
		Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(), instance::spawnNotes, instance.START_DELAY);
		Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(),
			() -> player.playSound(Sound.sound(Key.key("beatblocks:" + chartName + "." + chart.getString(Chart.soundFile)), Sound.Source.RECORD, 1.0f, 1.0f), Sound.Emitter.self()),
			chart.getInteger(Chart.offset) + instance.START_DELAY + (long)NoteEntity.TIME
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
			chart.getInteger(Chart.offset) + instance.START_DELAY);
		return instance;
	}

	private void spawnNotes() {
		new BukkitRunnable() {
			@Override
			public void run() {
				if(finished) {
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
					addNoteEntity(new NoteEntity(board.noteAnchor, board.face, chartNote.info));
					noteIndex++;
				}
			}
		}.runTaskTimer(Beatblocks.getPlugin(), 0, 1);
	}

	public void processNote(int lane) {
		Optional<NoteEntity> optional = notes.stream().filter(entity -> entity.info.lane == lane).min(Comparator.comparingInt(entity -> entity.info.timing));
		optional.ifPresent(NoteEntity::process);
	}

	private void addNoteEntity(NoteEntity entity) {
		notes.add(entity);
	}

	void endGame(boolean force) {
		finished = true;
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

	public boolean hasEnded() {
		return finished;
	}

	private class NoteEntity {
		private static final double LENGTH = 12;                        // blocks => 12
		private static final double TIME = 15;                          // ticks => 15

	    private static final double SPEED = LENGTH / TIME;              // block / tick -> 12 blocks for 15 ticks => 0.8
		private static final int TIME_LIMIT = (int)(13 / SPEED) + 1;    // 17

	    private final Entity noteEntity;
		private final NoteInfo info;

		private int age = 0;

		public NoteEntity(Location location, BlockFace direction, NoteInfo info) {
		    if(!isCardinal(direction)) throw new IllegalArgumentException("Direction is not cardinal");
		    this.info = info;
		    Location noteLocation = getNoteSpawnLocation(location, direction, this.info.lane);
	        noteEntity = location.getWorld().spawnFallingBlock(noteLocation, getMaterial(info.lane).createBlockData());
	        noteEntity.setGravity(false);
			noteEntity.setVelocity(direction.getDirection().multiply(SPEED));
		    new BukkitRunnable() {
			    @Override
			    public void run() {
				    noteEntity.setVelocity(direction.getDirection().multiply(SPEED));
				    if(age >= TIME_LIMIT) {
						this.cancel();
						process(Judgement.MISS);
						return;
				    }
					age++;
			    }
		    }.runTaskTimer(Beatblocks.getPlugin(), 0, 1);
	    }

		private Location getNoteSpawnLocation(Location anchor, BlockFace direction, int lane) {
			return anchor.clone().add(getLeft(direction).getDirection().multiply(lane));
		}

		public void process() {
		    process(Judgement.get(currentTiming, this.info.timing));
	    }

		private void process(Judgement judgement) {
			player.sendActionBar(judgement.text);
			counter.increase(judgement);
			delete();
		}

	    public void delete() {
		    if(!notes.remove(this)) {
			    Bukkit.getLogger().warning("Failed to remove note entity from container: " + this);
		    }
	        noteEntity.remove();
	    }

		private static Material getMaterial(int lane) {
	        switch(lane) {
	            case 0, 8 -> { return Material.RED_CONCRETE; }
	            case 1, 7 -> { return Material.YELLOW_CONCRETE; }
	            case 2, 6 -> { return Material.LIME_CONCRETE; }
	            case 3, 5 -> { return Material.LIGHT_BLUE_CONCRETE; }
	            default -> throw new IllegalArgumentException("No material for note lane " + lane);
	        }
	    }

	    private static BlockFace getLeft(BlockFace face) {
	        return switch(face) {
	            case NORTH -> BlockFace.WEST;
	            case WEST -> BlockFace.SOUTH;
	            case SOUTH -> BlockFace.EAST;
	            case EAST -> BlockFace.NORTH;
	            default -> throw new IllegalArgumentException(face + " is not a cardinal value");
	        };
	    }

	    private static boolean isCardinal(BlockFace face) {
	        return switch(face) {
	            case NORTH, SOUTH, EAST, WEST -> true;
	            default -> false;
	        };
	    }

		@Override
		public String toString() {
			return "NoteEntity{" + "info=" + info + ",age=" + age + "}";
		}
	}
}
