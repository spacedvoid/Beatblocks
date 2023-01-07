package net.spacedvoid.beatblocks.game;

import net.spacedvoid.beatblocks.Beatblocks;
import net.spacedvoid.beatblocks.chart.NoteInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class NoteEntity {
	private static final double LENGTH = 12;                        // blocks => 12
	public static final double TIME = 15;                          // ticks => 15
	
	private static final double SPEED = LENGTH / TIME;              // block / tick -> 12 blocks for 15 ticks => 0.8
	private static final int TIME_LIMIT = (int)(13 / SPEED) + 1;    // 17
	
	private final Entity noteEntity;
	public final NoteInfo info;
	
	private int age = 0;
	
	public static List<NoteEntity> create(GameInstance game, NoteInfo info) {
		ArrayList<NoteEntity> list = new ArrayList<>(4);
		game.getBoard().noteAnchors.forEach(anchor -> list.add(new NoteEntity(game, anchor.location(), anchor.direction(), info)));
		return list;
	}
	
	private final GameInstance instance;
	
	private NoteEntity(GameInstance instance, Location location, BlockFace direction, NoteInfo info) {
		if(instance.getGameType() != Game.Type.SINGLEPLAYER) throw new IllegalArgumentException("Game type not matching (expected SINGLEPLAYER, found " + instance.getGameType() + ")");
		if(!isCardinal(direction)) throw new IllegalArgumentException("Direction is not cardinal");
		this.info = info;
		this.instance = instance;
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
	
	void process() {
		process(Judgement.get(instance.getCurrentTiming(), this.info.timing));
	}
	
	private void process(Judgement judgement) {
		instance.getPlayers().forEach(player -> player.sendActionBar(judgement.text));
		instance.getCounter().increase(judgement);
		delete();
	}
	
	public void delete() {
		if(!instance.removeNote(this)) {
			Bukkit.getLogger().warning("Failed to remove note entity from container: " + this);
		}
		noteEntity.remove();
	}
	
	private static Material getMaterial(int lane) {
		switch(lane) {
			case 0, 8 -> {
				return Material.RED_CONCRETE;
			}
			case 1, 7 -> {
				return Material.YELLOW_CONCRETE;
			}
			case 2, 6 -> {
				return Material.LIME_CONCRETE;
			}
			case 3, 5 -> {
				return Material.LIGHT_BLUE_CONCRETE;
			}
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
