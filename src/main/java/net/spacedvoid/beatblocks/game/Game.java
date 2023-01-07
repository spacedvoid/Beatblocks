package net.spacedvoid.beatblocks.game;

import net.spacedvoid.beatblocks.charts.Charts;
import net.spacedvoid.beatblocks.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.parser.DefaultParser;
import net.spacedvoid.beatblocks.structures.Board;
import net.spacedvoid.beatblocks.util.LimitedMap;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Game {
    // Capacity of 6 and load factor of 1.0 to fix size to 5. Does not expect size to be 6 or more.
    public static final Map<UUID, Board.TypedBoard> singleBoards = new LimitedMap<>(6);
    public static final Map<UUID, Board.TypedBoard> multiBoards = new LimitedMap<>(6);
    private static final Map<UUID, GameInstance> activeGames = new LimitedMap<>(6);

    public static void startGame(String chartName, Player player) {
        Board.TypedBoard board;
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
        Board.TypedBoard board;
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
    @Nullable
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

    public static Board.TypedBoard registerBoard(Player player, String type) {
        Board.TypedBoard created = Board.create(type, player.getLocation(), player.getFacing());
        if(created.getType() == Board.Type.SINGLEPLAYER) return singleBoards.put(player.getUniqueId(), created);
        else    // type == Board.Type.MULTIPLAYER
            return multiBoards.put(player.getUniqueId(), created);
    }
    
    public enum Type {
        SINGLEPLAYER, MULTIPLAYER;
        
        public boolean matches(Board.Type boardType) {
            return (this == SINGLEPLAYER && boardType == Board.Type.SINGLEPLAYER) || (this == MULTIPLAYER && boardType == Board.Type.MULTIPLAYER);
        }
    }
}
