package net.spacedvoid.beatblocks;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import net.spacedvoid.beatblocks.charts.Charts;
import net.spacedvoid.beatblocks.commands.CommandFlag;
import net.spacedvoid.beatblocks.commands.Commands;
import net.spacedvoid.beatblocks.events.NotePressedEvent;
import net.spacedvoid.beatblocks.events.PlayerJoinedEvent;
import net.spacedvoid.beatblocks.events.RPAppliedEvent;
import net.spacedvoid.beatblocks.events.ServerLoadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class Beatblocks extends JavaPlugin {
    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIConfig().verboseOutput(true));
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable(this);
        Commands.registerCommands();
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
        if(Beatblocks.getPlugin().getConfig().getBoolean(Config.DEBUG, false)) CommandFlag.setFlag(CommandFlag.DEBUG, true);
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
            saveDefaultConfig();
        }
        FileConfiguration config = getConfig();
        if(!config.isBoolean(Config.DEBUG)) {
            config.set(Config.DEBUG, false);
            Bukkit.getLogger().info("Key \"" + Config.DEBUG + "\" at config.yml was set to default value \"false\"");
        }
        if(!config.isBoolean(Config.SHOW_STACKTRACE)) {
            config.set(Config.SHOW_STACKTRACE, true);
            Bukkit.getLogger().info("Key \"" + Config.SHOW_STACKTRACE + "\" at config.yml was set to default value \"true\"");
        }
        if(!config.isString(Config.NGROK_AUTHTOKEN)) {
            Bukkit.getLogger().warning("To perform resource hosting, the ngrok authtoken must be set.");
            Bukkit.getLogger().warning("Go to https://dashboard.ngrok.com/get-started/your-authtoken and copy your authtoken to the config.");
        }
    }

    public static class Config {
        public static final String DEBUG = "debug";
        public static final String SHOW_STACKTRACE = "show-stacktrace";
        public static final String NGROK_AUTHTOKEN = "ngrok-authtoken";
    }
}