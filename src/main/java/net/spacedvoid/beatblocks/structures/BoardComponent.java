package net.spacedvoid.beatblocks.structures;

import org.bukkit.block.BlockFace;

/**
 * Represents that this {@link Board} consists with itself so its properties can be directly accessible.
 */
public interface BoardComponent extends Board {
	BlockFace getPlayerFace();
	Board.NoteBase getNoteBase();
}
