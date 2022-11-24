package net.spacedvoid.beatblocks.common.structures;

import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.common.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.common.exceptions.UncheckedThrowable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.structure.Structure;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;

/**
 * Structures should be saved facing north.
 */
public class Board {
	private Board(Board.Type type, Location noteAnchor, BlockFace face, Location playerLocation) {
		this.type = type;
		this.noteAnchor = noteAnchor;
		this.face = face;
		this.playerLocation = playerLocation;
	}

	public final Board.Type type;
	/**
	 * Anchor point of where notes should be spawned.
	 */
	public final Location noteAnchor;
	public final Location playerLocation;
	/**
	 * Direction of where notes move to.
	 */
	public final BlockFace face;

	/**
	 * The location of the returned Board points the base of where notes should be spawned.
	 */
	public static Board createSinglePlayer(Location location, BlockFace face) {
		Type type = Type.SINGLEPLAYER;
		Location boardLocation = location.clone();
		Structure structure;
		InputStream in = Beatblocks.getPlugin().getResource(type.path);
		if(in == null) throw new BeatblocksException("Cannot find predefined resource " + type.path);
		try {
			structure = Bukkit.getStructureManager().loadStructure(in);
		} catch (IOException e) {
			throw new UncheckedThrowable(e);
		}
		boardLocation.setY(location.getBlockY() > location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ())?
			(location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()) + 1) : location.getBlockY()
		);
		StructureRotation rotation = StructureRotation.NONE;
		switch(face) {
			case NORTH -> {
				boardLocation.setX(location.getX() - type.offset_right);
				boardLocation.setZ(location.getZ() - type.offset_back);
			}
			case EAST -> {
				rotation = StructureRotation.CLOCKWISE_90;
				boardLocation.setX(location.getX() + type.offset_back);
				boardLocation.setZ(location.getZ() - type.offset_right);
			}
			case SOUTH -> {
				rotation = StructureRotation.CLOCKWISE_180;
				boardLocation.setX(location.getX() + type.offset_right);
				boardLocation.setZ(location.getZ() + type.offset_back);
			}
			case WEST -> {
				rotation = StructureRotation.COUNTERCLOCKWISE_90;
				boardLocation.setX(location.getX() - type.offset_back);
				boardLocation.setZ(location.getZ() + type.offset_right);
			}
			default -> throw new IllegalArgumentException("Direction is not cardinal");
		}
		Location noteLocation = getNoteSpawnLocation(location, face);
		structure.place(boardLocation, false, rotation, Mirror.NONE, 0, 1.0f, new Random());
		return new Board(type, noteLocation, face.getOppositeFace(), location);
	}

	private static Location getNoteSpawnLocation(Location location, BlockFace direction) {
		int dx = 13, dz = -4;
		switch(direction) {
			case NORTH: break;
			case EAST: dx = dx ^ dz ^ (dz = dx); dx = -dx; break;
			case SOUTH: dx = -dx; dz = -dz; break;
			case WEST: dx = dx ^ dz ^ (dz = dx); dz = -dz; break;
		}
		Location result = location.clone();
		result.setX(location.getX() + dx);
		result.setZ(location.getZ() + dz);
		return result;
	}

	/**
	 * The list of locations of the returned board are the base points of where the notes should spawn.
	 */
	public static Board createMultiPlayer(Location location) {
		throw new UnsupportedOperationException("Multiplayer not supported yet :(");
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
}
