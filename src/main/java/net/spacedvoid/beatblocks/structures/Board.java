package net.spacedvoid.beatblocks.structures;

import net.spacedvoid.beatblocks.exceptions.BeatblocksException;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a board which can be used for Beatblocks.
 * Structures for <code>Boards</code> must be saved with the player facing north.
 */
public interface Board {
	Location getBoardLocation();
	/**
	 * @return {@link Type#SINGLEPLAYER} or {@link Type#MULTIPLAYER} depending on the board type.
	 */
	@NotNull
	Board.Type getType();
	List<BoardComponent> toComponents();

	static Board create(String type, Player player) {
		Board.Type boardType = Board.Type.of(type);
		if(boardType == null) throw new BeatblocksException("No such board \"" + type + "\"!");
		return switch(boardType) {
			case SINGLEPLAYER -> SingleplayerBoard.create(player.getLocation(), player.getFacing());
			case MULTIPLAYER -> MultiplayerBoard.create(player);
		};
	}

	enum Type {
		SINGLEPLAYER("singleplayer"),
		MULTIPLAYER("multiplayer");

		Type(String id) {
			this.id = id;
		}

		public final String id;

		/**
		 * Replacement of {@link #valueOf(String)}.
		 * @return The {@link Board.Type} matching the {@code id} if exists, or {@code null} otherwise.
		 */
		public static Board.Type of(String id) {
			for(Board.Type type : Board.Type.values()) {
				if(type.id.equals(id)) return type;
			}
			return null;
		}
	}

	/**
	 * @param location Location of this base
	 * @param direction BlockFace of where the note should move
	 */
	record NoteBase(Location location, BlockFace direction) {}
}
