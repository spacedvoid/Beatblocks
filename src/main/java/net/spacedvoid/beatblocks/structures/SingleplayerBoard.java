package net.spacedvoid.beatblocks.structures;

import net.spacedvoid.beatblocks.Beatblocks;
import net.spacedvoid.beatblocks.exceptions.UncheckedThrowable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.structure.Structure;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

public class SingleplayerBoard implements BoardComponent {
	private static final String resourcePath = "structures/singleplayer_board.nbt";
	private static final int offset_right = 5, offset_back = 14;

	private final Board.NoteBase noteBase;
	private final Location boardLocation;
	private final BlockFace playerFace;

	private SingleplayerBoard(Board.NoteBase noteBase, Location boardLocation, BlockFace playerFace) {
		this.noteBase = noteBase;
		this.boardLocation = boardLocation;
		this.playerFace = playerFace;
	}

	public static SingleplayerBoard create(Location playerLocation, BlockFace playerFace) {
		Location boardLocation = playerLocation.clone().toCenterLocation();
		Structure structure;
		try (InputStream in = Beatblocks.getPlugin().getResource(resourcePath)) {
			if(in == null) throw new RuntimeException("Cannot find predefined resource " + resourcePath);
			structure = Bukkit.getStructureManager().loadStructure(in);
		} catch (IOException e) {
			throw new UncheckedThrowable(e);
		}
		boardLocation.setY(boardLocation.getBlockY() > boardLocation.getWorld().getHighestBlockYAt(boardLocation.getBlockX(), boardLocation.getBlockZ())?
			(boardLocation.getWorld().getHighestBlockYAt(boardLocation.getBlockX(), boardLocation.getBlockZ()) + 1) : boardLocation.getBlockY()
		);
		StructureRotation rotation;
		Location structureLocation = boardLocation.clone();
		switch(playerFace) {
			case NORTH -> {
				rotation = StructureRotation.NONE;
				structureLocation.setX(structureLocation.getX() - offset_right);
				structureLocation.setZ(structureLocation.getZ() - offset_back);
			}
			case EAST -> {
				rotation = StructureRotation.CLOCKWISE_90;
				structureLocation.setX(structureLocation.getX() + offset_back);
				structureLocation.setZ(structureLocation.getZ() - offset_right);
			}
			case SOUTH -> {
				rotation = StructureRotation.CLOCKWISE_180;
				structureLocation.setX(structureLocation.getX() + offset_right);
				structureLocation.setZ(structureLocation.getZ() + offset_back);
			}
			case WEST -> {
				rotation = StructureRotation.COUNTERCLOCKWISE_90;
				structureLocation.setX(structureLocation.getX() - offset_back);
				structureLocation.setZ(structureLocation.getZ() + offset_right);
			}
			default -> throw new IllegalArgumentException("Direction is not cardinal");
		}
		structure.place(structureLocation, false, rotation, Mirror.NONE, 0, 1.0f, new Random());
		return new SingleplayerBoard(getNoteSpawnLocation(boardLocation, playerFace), boardLocation, playerFace);
	}

	private static Board.NoteBase getNoteSpawnLocation(Location boardLocation, BlockFace direction) {
		int dx = -4, dz = -14;
		switch(direction) {
			case NORTH: break;
			case EAST: dx = dx ^ dz ^ (dz = dx); dx = -dx; break;
			case SOUTH: dx = -dx; dz = -dz; break;
			case WEST: dx = dx ^ dz ^ (dz = dx); dz = -dz; break;
			default: throw new IllegalArgumentException("Direction " + direction + " is not cardinal");
		}
		return new Board.NoteBase(boardLocation.clone().add(dx, 1, dz), direction.getOppositeFace());
	}

	@Override
	public NoteBase getNoteBase() {
		return noteBase;
	}

	@Override
	public BlockFace getPlayerFace() {
		return playerFace;
	}

	@Override
	public Location getBoardLocation() {
		return boardLocation;
	}

	@Override
	@NotNull
	public Type getType() {
		return Type.SINGLEPLAYER;
	}

	@Override
	public List<BoardComponent> toComponents() {
		return List.of(this);
	}
}
