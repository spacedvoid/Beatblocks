package net.spacedvoid.beatblocks.game;

import net.spacedvoid.beatblocks.structures.Board;
import org.bukkit.entity.Player;

import java.util.List;

public interface GameInstance {
	default int getStartDelay() {
		return 40;
	}
	Game.Type getGameType();
	void processNote(Player player, int lane);
	void endGame(boolean force);
	boolean hasEnded();
	int getCurrentTiming();
	List<Player> getPlayers();
	Judgement.JudgementCounter getCounter();
	Board getBoard();
	
	/**
	 * @return true if the instance contained the note entity; false otherwise
	 */
	boolean removeNote(NoteEntity noteEntity);
}
