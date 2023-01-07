package net.spacedvoid.beatblocks.structures;

import net.spacedvoid.beatblocks.Beatblocks;
import net.spacedvoid.beatblocks.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.exceptions.UncheckedThrowable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.structure.Structure;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Structures should be saved facing north.
 */
public class Board {
	private Board(Type type, BlockFace face, Location boardLocation) {
		this.type = type;
		this.boardLocation = boardLocation;
		this.noteAnchors = this.type == Type.SINGLEPLAYER?
				getNoteSpawnLocation(this.boardLocation, face) : getNoteSpawnLocation(this.boardLocation);
		this.face = face.getOppositeFace();
	}

	public final Board.Type type;
	/**
	 * Anchor point of where notes should be spawned.
	 */
	public final List<NoteAnchor> noteAnchors;
	public final Location boardLocation;
	/**
	 * Player direction when this board was placed.
	 */
	public final BlockFace face;

	/**
	 * The location of the returned Board points the base of where notes should be spawned.
	 */
	public static Board createSinglePlayer(Location playerLocation, BlockFace face) {
		Type type = Type.SINGLEPLAYER;
		Location boardLocation = playerLocation.clone().toCenterLocation();
		Structure structure;
		InputStream in = Beatblocks.getPlugin().getResource(type.path);
		if(in == null) throw new BeatblocksException("Cannot find predefined resource " + type.path);
		try {
			structure = Bukkit.getStructureManager().loadStructure(in);
		} catch (IOException e) {
			throw new UncheckedThrowable(e);
		}
		boardLocation.setY(boardLocation.getBlockY() > boardLocation.getWorld().getHighestBlockYAt(boardLocation.getBlockX(), boardLocation.getBlockZ())?
			(boardLocation.getWorld().getHighestBlockYAt(boardLocation.getBlockX(), boardLocation.getBlockZ()) + 1) : boardLocation.getBlockY()
		);
		StructureRotation rotation = StructureRotation.NONE;
		Location structureLocation = boardLocation.clone();
		switch(face) {
			case NORTH -> {
				structureLocation.setX(structureLocation.getX() - type.offset_right);
				structureLocation.setZ(structureLocation.getZ() - type.offset_back);
			}
			case EAST -> {
				rotation = StructureRotation.CLOCKWISE_90;
				structureLocation.setX(structureLocation.getX() + type.offset_back);
				structureLocation.setZ(structureLocation.getZ() - type.offset_right);
			}
			case SOUTH -> {
				rotation = StructureRotation.CLOCKWISE_180;
				structureLocation.setX(structureLocation.getX() + type.offset_right);
				structureLocation.setZ(structureLocation.getZ() + type.offset_back);
			}
			case WEST -> {
				rotation = StructureRotation.COUNTERCLOCKWISE_90;
				structureLocation.setX(structureLocation.getX() - type.offset_back);
				structureLocation.setZ(structureLocation.getZ() + type.offset_right);
			}
			default -> throw new IllegalArgumentException("Direction is not cardinal");
		}
		structure.place(structureLocation, false, rotation, Mirror.NONE, 0, 1.0f, new Random());
		return new Board(type, face, boardLocation);
	}

	/**
	 * The list of locations of the returned board are the base points of where the notes should spawn.
	 */
	public static Board createMultiPlayer(Location location) {
		throw new UnsupportedOperationException("Multiplayer not supported yet :(");
	}
	
	/**
	 * For singleplayer.
	 */
	private static List<NoteAnchor> getNoteSpawnLocation(Location boardLocation, BlockFace direction) {
		int dx = -4, dz = -14;
		switch(direction) {
			case NORTH: break;
			case EAST: dx = dx ^ dz ^ (dz = dx); dx = -dx; break;
			case SOUTH: dx = -dx; dz = -dz; break;
			case WEST: dx = dx ^ dz ^ (dz = dx); dz = -dz; break;
		}
		return List.of(new NoteAnchor(boardLocation.clone().add(dx, 1, dz), direction.getOppositeFace()));
	}
	
	/**
	 * For multiplayer.
	 */
	private static List<NoteAnchor> getNoteSpawnLocation(Location boardLocation) {
		ArrayList<NoteAnchor> list = new ArrayList<>(4);
		// TODO: Get anchor position in-game
		return list;
	}

	public enum Type {
		SINGLEPLAYER("singleplayer", "structures/singleplayer_board.nbt", 5, 14),
		MULTIPLAYER("multiplayer", "structures/multiplayer_board.nbt", 0, 0);

		Type(String id, String path, int offset_right, int offset_back) {
			this.id = id;
			this.path = path;
			this.offset_right = offset_right;
			this.offset_back = offset_back;
		}

		public final String id, path;
		public final int offset_right, offset_back;

		public static Type of(String id) {
			for(Type type : Arrays.stream(Type.values()).toList()) {
				if(type.id.equals(id)) return type;
			}
			return null;
		}
	}

	@Override
	public String toString() {
		return "Board{" + "type=" + type + ", noteAnchor=" + noteAnchors + ", playerLocation=" + boardLocation + "}";
	}
	
	public record NoteAnchor(Location location, BlockFace direction) {}
}
