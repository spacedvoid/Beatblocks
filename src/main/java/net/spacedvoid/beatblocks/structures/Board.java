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

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.bukkit.block.BlockFace.*;

/**
 * Structures should be saved facing north.
 */
public class Board {
	public abstract static class TypedBoard {
		public abstract List<ViewableBoard> getViewable();
		/**
		 * @return The NoteBase of this board, or <code>null</code> if this board is for multiplayer
		 */
		@Nullable
		public abstract NoteBase getNoteBase();
		/**
		 * @return The location of this board
		 */
		public abstract Location getBoardLocation();
		/**
		 * @return The direction of the player when this board was created, or <code>null</code> if this board is for multiplayer
		 */
		@Nullable
		abstract BlockFace getFace();
		public abstract Board.Type getType();
	}

	public static class ViewableBoard {
		private final TypedBoard board;

		static ViewableBoard of(TypedBoard board) {
			return new ViewableBoard(board);
		}

		private ViewableBoard(TypedBoard board) {
			this.board = board;
		}

		public NoteBase getNoteBase() {
			return board.getNoteBase();
		}

		/**
		 * @return The direction of the player when this board was created, or <code>null</code> if this board is for multiplayer
		 */
		public BlockFace getFace() {
			return board.getFace();
		}

		public Location getBoardLocation() {
			return board.getBoardLocation();
		}
	}

	static class SingleplayerBoard extends TypedBoard {
		/**
		 * Anchor point of where notes should be spawned.
		 */
		public final NoteBase noteBase;
		public final Location boardLocation;
		/**
		 * Player direction when this board was placed.
		 */
		public final BlockFace face;

		SingleplayerBoard(NoteBase base, Location boardLocation, BlockFace face) {
			this.noteBase = base;
			this.boardLocation = boardLocation;
			this.face = face;
		}

		@Override
		public List<ViewableBoard> getViewable() {
			return List.of(ViewableBoard.of(this));
		}

		@Override
		public NoteBase getNoteBase() {
			return noteBase;
		}

		@Override
		public Location getBoardLocation() {
			return boardLocation;
		}

		@Override
		@Nullable
		BlockFace getFace() {
			return face;
		}

		@Override
		public Type getType() {
			return Type.SINGLEPLAYER;
		}
	}

	static class MultiplayerBoard extends TypedBoard {
		private final List<SingleplayerBoard> boards = Arrays.asList(new SingleplayerBoard[4]);
		private final Location boardLocation;

		MultiplayerBoard(List<SingleplayerBoard> boards, Location boardLocation) {
			if(boards.size() != 4) throw new IllegalArgumentException("Board count does not match (expected 4, found " + boards.size() + ")");
			this.boardLocation = boardLocation;
		}

		@Override
		public List<ViewableBoard> getViewable() {
			return boards.stream().map(ViewableBoard::new).toList();
		}

		@Override
		public NoteBase getNoteBase() {
			return null;
		}

		@Override
		public Location getBoardLocation() {
			return boardLocation;
		}

		@org.jetbrains.annotations.Nullable
		@Override
		BlockFace getFace() {
			return null;
		}

		@Override
		public Type getType() {
			return Type.MULTIPLAYER;
		}
	}

	public static TypedBoard create(String type, Location playerLocation, BlockFace face) {
		Board.Type boardType = Type.of(type);
		if(boardType == null) throw new IllegalArgumentException("No such board \"" + type + "\"");
		return switch(boardType) {
			case SINGLEPLAYER -> createSinglePlayer(playerLocation, face);
			case MULTIPLAYER -> createMultiPlayer(playerLocation);
		};
	}

	/**
	 * The location of the returned Board points the base of where notes should be spawned.
	 */
	private static TypedBoard createSinglePlayer(Location playerLocation, BlockFace face) {
		Type type = Type.SINGLEPLAYER;
		Location boardLocation = playerLocation.clone().toCenterLocation();
		Structure structure;
		try (InputStream in = Beatblocks.getPlugin().getResource(type.path)) {
			if(in == null) throw new BeatblocksException("Cannot find predefined resource " + type.path);
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
		return new SingleplayerBoard(getNoteSpawnLocation(boardLocation, face), boardLocation, face);
	}

	/**
	 * The list of locations of the returned board are the base points of where the notes should spawn.
	 */
	private static TypedBoard createMultiPlayer(Location playerLocation) {
		List<SingleplayerBoard> boards = new ArrayList<>(4);
		Location centerLocation = playerLocation.clone().toCenterLocation();
		List<AbstractMap.SimpleEntry<Location, BlockFace>> boardLocations = List.of(
			new AbstractMap.SimpleEntry<>(centerLocation.clone().add(20, 0, 0), WEST),
			new AbstractMap.SimpleEntry<>(centerLocation.clone().add(-20, 0, 0), EAST),
			new AbstractMap.SimpleEntry<>(centerLocation.clone().add(0, 0, 20), NORTH),
			new AbstractMap.SimpleEntry<>(centerLocation.clone().add(0, 0, -20), SOUTH)
		);
		boards.addAll(boardLocations.stream().map(entry -> (SingleplayerBoard)createSinglePlayer(entry.getKey(), entry.getValue())).toList());
		return new MultiplayerBoard(boards, centerLocation);
	}//+-20
	
	/**
	 * For singleplayer.
	 */
	private static NoteBase getNoteSpawnLocation(Location boardLocation, BlockFace direction) {
		int dx = -4, dz = -14;
		switch(direction) {
			case NORTH: break;
			case EAST: dx = dx ^ dz ^ (dz = dx); dx = -dx; break;
			case SOUTH: dx = -dx; dz = -dz; break;
			case WEST: dx = dx ^ dz ^ (dz = dx); dz = -dz; break;
		}
		return new NoteBase(boardLocation.clone().add(dx, 1, dz), direction.getOppositeFace());
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

	/**
	 * @param location Location of this base
	 * @param direction BlockFace of where the note should move
	 */
	public record NoteBase(Location location, BlockFace direction) {}
}
