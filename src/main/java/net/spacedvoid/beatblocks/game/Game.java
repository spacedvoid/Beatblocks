package net.spacedvoid.beatblocks.game;

import net.spacedvoid.beatblocks.charts.Charts;
import net.spacedvoid.beatblocks.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.parser.DefaultParser;
import net.spacedvoid.beatblocks.structures.Board;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Game {
    public static final Map<UUID, Board> boards = new HashMap<>(5, 1);
    public static final Map<UUID, GameInstance> activeGames = new HashMap<>(5, 1);

    public static void startGame(Player player, String chartName) {
        if(!boards.containsKey(player.getUniqueId())) throw new BeatblocksException("No singleplayer board for " + player.getName());
        if(activeGames.containsKey(player.getUniqueId())) throw new BeatblocksException("There is already a game instance for " + player.getName());
        if(Charts.CHARTS.get(chartName) == null) throw new BeatblocksException("Chart not listed. Try reloading chart list.");
        GameInstance instance = GameInstance.create(player, new DefaultParser().readChartAsync(Charts.getChartPath(chartName)), chartName, boards.get(player.getUniqueId()));
        activeGames.put(player.getUniqueId(), instance);
    }

    public static void stop(Player player, boolean force) {
        GameInstance instance = activeGames.get(player.getUniqueId());
        if(instance != null) {
            if(!instance.hasEnded()) instance.endGame(force);
            activeGames.remove(player.getUniqueId());
        }
    }

    public static Board registerBoard(Player player, Board.Type type) {
        Board created;
        if(type == Board.Type.SINGLEPLAYER) created = Board.createSinglePlayer(player.getLocation(), player.getFacing());
        else created = Board.createMultiPlayer(player.getLocation());
        return boards.put(player.getUniqueId(), created);
    }
}
