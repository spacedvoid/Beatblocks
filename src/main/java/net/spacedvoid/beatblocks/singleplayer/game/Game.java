package net.spacedvoid.beatblocks.singleplayer.game;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import net.spacedvoid.beatblocks.common.Beatblocks;
import net.spacedvoid.beatblocks.common.Board;
import net.spacedvoid.beatblocks.common.charts.Charts;
import net.spacedvoid.beatblocks.common.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.singleplayer.chart.Chart;
import net.spacedvoid.beatblocks.singleplayer.parser.DefaultParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Game {
    public static final Map<Player, Map.Entry<Location, BlockFace>> boards = new HashMap<>();
    public static final Map<Player, GameInstance> activeGames = new HashMap<>();

    public static void startGame(Player player, String chartName) {
        if(!boards.containsKey(player)) throw new BeatblocksException("No singleplayer board for " + player.getName() + ".");
        if(Charts.CHARTS.get(chartName) == null)
            throw new BeatblocksException("No such chart! Check typos or try reloading the list.");
        activeGames.put(player, new GameInstance(player, new DefaultParser().readChartAsync(Charts.getChartPath(chartName))));
    }

    private static void finish(GameInstance instance) {
        if(activeGames.containsValue(instance)) activeGames.entrySet().removeIf(entry -> entry.getValue().equals(instance));
    }

    public static void registerBoard(Player player, Board structure) {
        boards.put(player, Board.createStructure(structure, player.getLocation(), player.getFacing()));
    }

    private static class GameInstance {
        public final Chart chart;
        private int noteIndex = 0;
        private boolean finished = false;

        public GameInstance(Player player, CompletableFuture<Chart> chart) {
            Location playerLocation = boards.get(player).getKey();
            switch(boards.get(player).getValue()) {
                case NORTH -> playerLocation.setYaw(180);
                case EAST -> playerLocation.setYaw(270);
                case SOUTH -> playerLocation.setYaw(0);
                case WEST -> playerLocation.setYaw(90);
            }
            playerLocation.setPitch(45);
            player.teleport(playerLocation);
            if(!chart.isDone()) player.sendMessage(Component.text("Force loading chart. This may cause server lag."));
            try {
                this.chart = chart.get();
                Bukkit.getLogger().info("Force loading chart. The \"server is too slow\" messages can be ignored.");
            } catch (ExecutionException e) {
                throw new BeatblocksException("Failed to load chart file", e.getCause());
            } catch (InterruptedException e) {
                throw new RuntimeException("There was an error while reading the chart file", e.getCause());
            }
            //TODO: Intro behavior
            player.sendTitlePart(TitlePart.TIMES, Title.Times.times(Duration.ZERO, Duration.ofMillis(1500), Duration.ofMillis(500)));
            player.sendTitlePart(TitlePart.SUBTITLE, Component.text("Get Ready").color(NamedTextColor.GREEN));
            player.sendTitlePart(TitlePart.TITLE, Component.text(""));
            player.playSound(Sound.sound(Key.key("beatblocks:" + this.chart.getString(Chart.soundFile)), Sound.Source.AMBIENT, 1.0f, 1.0f));
            Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(), this::spawnNotes, this.chart.getInteger(Chart.offset) + 40);
        }

        private void spawnNotes() {
            if(noteIndex >= chart.notes.size()) {
                endGame();
                return;
            }
            Bukkit.getScheduler().runTaskLater(Beatblocks.getPlugin(), this::spawnNotes, this.chart.notes.get(++this.noteIndex).tick);
        }

        private void endGame() {

        }
    }

    private static class NoteEntity {
        private static final double SPEED = 1.25;    // block / tick

        private final Entity noteEntity;
        private final BlockFace direction;

        public NoteEntity(Location location, BlockFace direction, int lane) {
            if(!(direction == BlockFace.NORTH || direction == BlockFace.SOUTH || direction == BlockFace.EAST || direction == BlockFace.WEST))
                throw new IllegalArgumentException("Direction is not cardinal");
            this.direction = direction;
            World overworld;
            if((overworld = Bukkit.getServer().getWorld("world")) != null)
                noteEntity = overworld.spawnFallingBlock(location, getMaterial(lane).createBlockData());
            else throw new BeatblocksException("Default overworld \"world\" cannot be found");
            noteEntity.setGravity(false);
            noteEntity.setTicksLived(1);
            Bukkit.getScheduler().runTaskTimer(Beatblocks.getPlugin(), this::move, 0, 1);
        }

        private void move() {
            noteEntity.teleport(noteEntity.getLocation().add(direction.getDirection().multiply(SPEED)));
        }

        public void delete() {

        }

        private Material getMaterial(int lane) {
            switch(lane) {
                case 0, 8 -> { return Material.RED_CONCRETE; }
                case 1, 7 -> { return Material.YELLOW_CONCRETE; }
                case 2, 6 -> { return Material.LIME_CONCRETE; }
                case 3, 5 -> { return Material.LIGHT_BLUE_CONCRETE; }
                default -> throw new IllegalArgumentException("No material for note lane " + lane);
            }
        }
    }
}
