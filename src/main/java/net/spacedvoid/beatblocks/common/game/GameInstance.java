package net.spacedvoid.beatblocks.common.game;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.common.chart.Chart;
import net.spacedvoid.beatblocks.common.chart.NoteInfo;
import net.spacedvoid.beatblocks.common.events.NotePressedEvent;
import net.spacedvoid.beatblocks.common.exceptions.UncheckedThrowable;
import net.spacedvoid.beatblocks.common.structures.Board;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
	private boolean finished = false;
	private final List<NoteEntity> notes = new ArrayList<>();   // In case of NoteEntities being GCed
	private final int[] judgements = new int[]{0, 0, 0, 0, 0, 0};  // PERFECT, GREAT, GOOD, FAST, SLOW, MISS

	private final int START_DELAY = 40;

	private int currentTiming = 0;

	private GameInstance(Chart chart, Player player, Board board) {
		this.chart = chart;
		this.player = player;
		this.board = board;
	}

	public static GameInstance create(Player player, CompletableFuture<Chart> future, String chartName, Board board) {
		Location playerLocation = board.playerLocation;
		playerLocation.setY(playerLocation.getBlockY() + 5);
		switch(board.face) {
			case NORTH -> playerLocation.setYaw(0);
			case EAST -> playerLocation.setYaw(90);
			case SOUTH -> playerLocation.setYaw(180);
			case WEST -> playerLocation.setYaw(270);
		}
		playerLocation.setPitch(45);
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
		player.setGameMode(GameMode.CREATIVE);
		player.setFlying(true);
		player.teleport(playerLocation);
		NotePressedEvent.include(player);
		//TODO: Intro behavior
		player.showTitle(Title.title(Component.text(""), Component.text("준비"), Title.Times.times(Duration.ZERO, Duration.ofMillis(1500), Duration.ofMillis(500))));
		player.playSound(Sound.sound(Key.key("ui.button.click"), Sound.Source.RECORD, 1, 1), Sound.Emitter.self());
		Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(), instance::spawnNotes, instance.START_DELAY);
		Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(),
			() -> instance.start(player, Sound.sound(Key.key("beatblocks:" + chartName + "." + chart.getString(Chart.soundFile)), Sound.Source.RECORD, 1.0f, 1.0f)),
			chart.getInteger(Chart.offset) + instance.START_DELAY
		);
		return instance;
	}

	private void start(Player player, Sound sound) {
		player.playSound(sound, Sound.Emitter.self());
		new BukkitRunnable() {
			@Override
			public void run() {
				if(hasEnded()) this.cancel();
				currentTiming++;
			}
		}.runTaskTimer(Beatblocks.getPlugin(), 1, 1);
	}

	private void spawnNotes() {
		new BukkitRunnable() {
			@Override
			public void run() {
				if(noteIndex >= chart.notes.size()) {
					Game.stop(player, false);
					this.cancel();
				}
				Chart.ChartNote chartNote = chart.notes.get(noteIndex);
				if(currentTiming == chartNote.info.timing - NoteEntity.TIME) {
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
		if(!force) showResults();
		NotePressedEvent.exclude(player);
		finished = true;
	}

	private void showResults() {
		StringJoiner joiner = new StringJoiner(" , ");
		Arrays.stream(Judgement.values()).map(judgement -> judgement.text + " : " + judgements[judgement.ordinal]).forEach(joiner::add);
		player.sendMessage(Component.text(joiner.toString()));
	}

	public boolean hasEnded() {
		return finished;
	}

	private class NoteEntity {
		private static final double LENGTH = 12;    // blocks
		private static final double TIME = 15;      // ticks

	    private static final double SPEED = LENGTH / TIME;    // block / tick -> 12 blocks for 15 ticks
		private static final int TIME_LIMIT = (int)(13 / SPEED) + 1;

	    private final Entity noteEntity;
	    private final BlockFace direction;
	    private final NoteInfo info;

		private boolean isDeleted = false;

	    public NoteEntity(Location location, BlockFace direction, NoteInfo info) {
		    if(!isCardinal(direction)) throw new IllegalArgumentException("Direction is not cardinal");
	        this.direction = direction;
	        this.info = info;
			Location noteLocation = getNoteLocation(location, direction, info.lane);
	        noteEntity = location.getWorld().spawnFallingBlock(noteLocation, getMaterial(info.lane).createBlockData());
	        noteEntity.setGravity(false);
	        noteEntity.setTicksLived(1);
			new BukkitRunnable() {
				@Override
				public void run() {
					if(isDeleted) this.cancel();
					move();
				}
			}.runTaskTimer(Beatblocks.getPlugin(), 0, 1);
	    }

		private Location getNoteLocation(Location anchor, BlockFace direction, int lane) {
			Location result = anchor.clone();
			result.add(getLeft(direction).getDirection().multiply(lane));
			return result;
		}

		private void move() {
			if(noteEntity.getTicksLived() >= TIME_LIMIT) {
				process(Judgement.MISS);
				return;
			}
	        noteEntity.teleport(noteEntity.getLocation().add(direction.getDirection().multiply(SPEED)));
	    }

	    public void process() {
		    process(Judgement.get(currentTiming, info.timing));
	    }

		private void process(Judgement judgement) {
			player.sendActionBar(judgement.text);
			judgements[judgement.ordinal]++;
			if(judgement.getParent() != null) judgements[judgement.getParent().ordinal]++;
			delete();
		}

	    public void delete() {
		    if(!notes.remove(this)) {
			    Bukkit.getLogger().warning("Failed to remove note entity from container at lane: " + this.info.lane + ", timing: " + this.info.timing);
		    }
	        noteEntity.remove();    // Requires testing
		    this.isDeleted = true;
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
	}
}
