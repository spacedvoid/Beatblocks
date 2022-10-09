package net.spacedvoid.beatblocks.common;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Random;

/**
 * The relative position of a board must be at the -X, -Z and the bottom corner of the structure, while rotated north.
 * This structure-loading class may not work well with some structures.
 */
public class Board {
	private Board(String id, String path, int offset_right, int offset_back) {
		this.id = id;
		this.path = path;
		this.offset_right = offset_right;
		this.offset_back = offset_back;
	}

	public final String id, path;
	public final int offset_right, offset_back;

	public static final Board SINGLEPLAYER = new Board("singleplayer_board", "structures/singleplayer_board.nbt", 5, 13);

	public static Map.Entry<Location, BlockFace> createStructure(Board structure, Location location, BlockFace face) {
		try (InputStream is = Beatblocks.getPlugin().getResource(structure.path)) {
			if(is == null) throw new RuntimeException("The structure " + structure.id + " does not exist");
			org.bukkit.structure.Structure targetStructure = Bukkit.getServer().getStructureManager().loadStructure(is);
			StructureRotation rotation = StructureRotation.NONE;
			location.setY(location.getBlock().getType() == Material.AIR? location.getBlockY() : (location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()) + 1));
			switch(face) {
				case NORTH -> {
					location.setX(location.getX() - structure.offset_right);
					location.setZ(location.getZ() - structure.offset_back);
				}
				case EAST -> {
					rotation = StructureRotation.CLOCKWISE_90;
					location.setX(location.getX() + structure.offset_back);
					location.setZ(location.getZ() - structure.offset_right);
				}
				case SOUTH -> {
					rotation = StructureRotation.CLOCKWISE_180;
					location.setX(location.getX() + structure.offset_right);
					location.setZ(location.getZ() + structure.offset_back);
				}
				case WEST -> {
					rotation = StructureRotation.COUNTERCLOCKWISE_90;
					location.setX(location.getX() - structure.offset_back);
					location.setZ(location.getZ() + structure.offset_right);
				}
			}
			targetStructure.place(location, false, rotation, Mirror.NONE, 0, 1, new Random());
		}
		catch (IOException exception) {
			throw new UncheckedIOException("Failed to load board", exception);
		}
		return new AbstractMap.SimpleEntry<>(location, face);
	}
}
