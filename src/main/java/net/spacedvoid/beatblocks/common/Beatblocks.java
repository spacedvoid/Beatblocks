package net.spacedvoid.beatblocks.common;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import net.spacedvoid.beatblocks.common.charts.Charts;
import net.spacedvoid.beatblocks.common.commands.CommandFlag;
import net.spacedvoid.beatblocks.common.commands.Commands;
import net.spacedvoid.beatblocks.common.events.NotePressedEvent;
import net.spacedvoid.beatblocks.common.events.PlayerJoinedEvent;
import net.spacedvoid.beatblocks.common.events.RPAppliedEvent;
import net.spacedvoid.beatblocks.common.events.ServerLoadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
//TODO: Reconstruct exception handling, adapt UncheckedIOEs to UncheckedThrowables
public class Beatblocks extends JavaPlugin {
    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIConfig().verboseOutput(true));
        Commands.registerCommands();
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable(this);
        Bukkit.getPluginManager().registerEvents(new NotePressedEvent(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinedEvent(), this);
        Bukkit.getPluginManager().registerEvents(new ServerLoadedEvent(), this);
        Bukkit.getPluginManager().registerEvents(new RPAppliedEvent(), this);
        if(Files.notExists(getDataFolder().toPath())) {
            try {
                Files.createDirectory(this.getDataFolder().toPath());
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to create data folder for Beatblocks", e);
            }
        }
        manageConfig();
        if(Beatblocks.getPlugin().getConfig().getBoolean("debug", false)) CommandFlag.setFlag(CommandFlag.DEBUG, true);
        try {
            Charts.listChartsAsync().get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to load chart list", e.getCause());
        }
    }

    public static JavaPlugin getPlugin() {
        return Beatblocks.getPlugin(Beatblocks.class);
    }

    private void manageConfig() {
        if(Files.notExists(Path.of(getDataFolder().getPath() + "/config.yml"))) {
            this.saveDefaultConfig();
        }
        checkConfig(getConfig());
    }

    private void checkConfig(FileConfiguration config) {
        if(!config.isBoolean("debug")) {
            config.set("debug", false);
            Bukkit.getLogger().info("Key \"debug\" at config.yml was set to default value \"false\"");
        }
        if(!config.isBoolean("show-stacktrace")) {
            config.set("show-stacktrace", true);
            Bukkit.getLogger().info("Key \"show-stacktrace\" at config.yml was set to default value \"true\"");
        }
    }
}