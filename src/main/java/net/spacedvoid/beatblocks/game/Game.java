package net.spacedvoid.beatblocks.game;

import net.spacedvoid.beatblocks.charts.Charts;
import net.spacedvoid.beatblocks.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.parser.DefaultParser;
import net.spacedvoid.beatblocks.structures.Board;
import net.spacedvoid.beatblocks.structures.MultiplayerBoard;
import net.spacedvoid.beatblocks.structures.SingleplayerBoard;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Game {
    public static final Map<UUID, SingleplayerBoard> singleBoards = new HashMap<>(1, 1);
    public static final Map<UUID, MultiplayerBoard> multiBoards = new HashMap<>(1, 1);
    private static final Map<UUID, GameInstance> activeGames = new HashMap<>(1, 1);

    public static void startGame(String chartName, Player player) {
        SingleplayerBoard board;
        if((board = singleBoards.get(player.getUniqueId())) == null) throw new BeatblocksException("No singleplayer board for " + player.getName());
        if(activeGames.containsKey(player.getUniqueId())) throw new BeatblocksException("There is already a game instance for " + player.getName());
        if(Charts.CHARTS.get(chartName) == null) throw new BeatblocksException("No such chart!");
        SingleplayerGame instance = SingleplayerGame.create(new DefaultParser().readChartAsync(Charts.getChartPath(chartName)), board, player);
        activeGames.put(player.getUniqueId(), instance);
    }
    
    /**
     * @param players Requires <code>[0]</code> to be the host(board owner), and <code>length > 1</code>
     */
    public static void startGame(String chartName, Player... players) {
        MultiplayerGame.checkCreatable(players);
        MultiplayerBoard board;
        if((board = multiBoards.get(players[0].getUniqueId())) == null) throw new BeatblocksException("No singleplayer board for " + players[0].getName());
        for(Player player : players)
            if(activeGames.containsKey(player.getUniqueId())) throw new BeatblocksException("Player " + player.getName() + " already has an active game");
        if(Charts.CHARTS.get(chartName) == null) throw new BeatblocksException("No such chart!");
        MultiplayerGame instance = MultiplayerGame.create(new DefaultParser().readChartAsync(Charts.getChartPath(chartName)), board, players);
        activeGames.put(players[0].getUniqueId(), instance);
    }
    
    /**
     * @return <code>null</code> if the player has no associated game instances
     */
    public static GameInstance get(Player player) {
        Optional<GameInstance> optional = activeGames.values().stream().filter(instance -> instance.getPlayers().contains(player)).findFirst();
        return optional.orElse(null);
    }

    public static void stop(Player player, boolean force) {
        GameInstance instance = activeGames.get(player.getUniqueId());
        if(instance != null) {
            if(!instance.hasEnded()) instance.endGame(force);
            activeGames.remove(player.getUniqueId());
        }
    }

    public static Board registerBoard(Player player, String type) {
        Board created = Board.create(type, player);
        if(created.getType() == Board.Type.SINGLEPLAYER) return singleBoards.put(player.getUniqueId(), (SingleplayerBoard)created);
        else    // type == Board.Type.MULTIPLAYER
            return multiBoards.put(player.getUniqueId(), (MultiplayerBoard)created);
    }
    
    public enum Type {
        SINGLEPLAYER, MULTIPLAYER;
        
        public boolean matches(Board.Type boardType) {
            return (this == SINGLEPLAYER && boardType == Board.Type.SINGLEPLAYER) || (this == MULTIPLAYER && boardType == Board.Type.MULTIPLAYER);
        }
    }
}
