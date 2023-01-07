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
	/**
	 * @return {@code true} if the {@link EndStatus EndStatus} is {@link EndStatus#IN_PROGRESS IN_PROGRESS} or {@link EndStatus#STOPPED STOPPED}.
	 */
	boolean hasEnded();
	int getCurrentTiming();
	List<Player> getPlayers();
	Judgement.JudgementCounter getCounter();
	Board getBoard();
	EndStatus getEndStatus();
	
	/**
	 * @return true if the instance contained the note entity; false otherwise
	 */
	boolean removeNote(NoteEntity noteEntity);

	enum EndStatus {
		NONE, IN_PROGRESS, STOPPED
	}
}
