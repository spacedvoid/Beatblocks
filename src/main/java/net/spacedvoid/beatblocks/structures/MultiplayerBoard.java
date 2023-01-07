package net.spacedvoid.beatblocks.structures;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import static org.bukkit.block.BlockFace.*;

public class MultiplayerBoard implements Board {
	List<SingleplayerBoard> components = new ArrayList<>(4);
	final Location boardLocation;

	private MultiplayerBoard(List<SingleplayerBoard> components, Location boardLocation) {
		this.components.addAll(components);
		this.boardLocation = boardLocation;
	}

	public static MultiplayerBoard create(Player player) {
		List<SingleplayerBoard> components = new ArrayList<>(4);
		Location centerLocation = player.getLocation().clone().toCenterLocation();
		List<AbstractMap.SimpleEntry<org.bukkit.Location, BlockFace>> boardLocations = List.of(
			new AbstractMap.SimpleEntry<>(centerLocation.clone().add(0, 0, 18), NORTH),
			new AbstractMap.SimpleEntry<>(centerLocation.clone().add(0, 0, -18), SOUTH),
			new AbstractMap.SimpleEntry<>(centerLocation.clone().add(-18, 0, 0), EAST),
			new AbstractMap.SimpleEntry<>(centerLocation.clone().add(18, 0, 0), WEST)
		);
		player.teleport(player.getLocation().add(0, player.getLocation().getY() + 2.5, 0));
		components.addAll(boardLocations.stream().map(entry -> SingleplayerBoard.create(entry.getKey(), entry.getValue())).toList());
		flatSquareFill(centerLocation.clone(), 4, Material.SMOOTH_STONE.createBlockData());
		flatSquareFill(centerLocation.clone().add(0, 1, 0), 3, Material.SMOOTH_STONE.createBlockData());
		flatSquareFill(centerLocation.clone().add(0, 2, 0), 3, Bukkit.getServer().createBlockData("minecraft:smooth_stone_slab[type=bottom]"));
		return new MultiplayerBoard(components, centerLocation);
	}

	private static void flatSquareFill(Location center, int distance, BlockData blockData) {
		for(int dx = -distance; dx <= distance; dx++) for(int dz = -distance; dz <= distance; dz++) center.getWorld().setBlockData(center.clone().add(dx, 0, dz), blockData);
	}

	@Override
	public List<BoardComponent> toComponents() {
		return this.components.stream().map(board -> (BoardComponent)board).toList();
	}

	@Override
	public Location getBoardLocation() {
		return boardLocation;
	}

	@Override
	public @NotNull Type getType() {
		return Type.MULTIPLAYER;
	}
}
