package net.spacedvoid.beatblocks.singleplayer.parser;

import net.spacedvoid.beatblocks.common.charts.Charts;
import net.spacedvoid.beatblocks.common.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.common.exceptions.CommandFailedException;
import net.spacedvoid.beatblocks.resourcepack.ResourceBuilder;
import net.spacedvoid.beatblocks.singleplayer.chart.Chart;
import net.spacedvoid.beatblocks.singleplayer.exceptions.ChartFileException;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

/* Structure:
format=(String)
sound-file=(String)
song=(String)
artist=(String)
creator=(String)
difficulty=(double)
bpm=(double)
time=(Time)
offset=(int)
keys=(int)
chart=
#,#,#   //tick,lane,hasDoubleAccuracy
*/

public class DefaultParser implements IParser {
    public static final String PARSER_VERSION = "Default-1.0";

    public String getVersion() {
        return PARSER_VERSION;
    }

    public Chart readChart(String chartName) throws CommandFailedException {
        ResourceBuilder.checkChartFolderLock();
        File chartFile = new File(Charts.getChartPath(chartName));
        if(!chartFile.exists()) throw new ChartFileException("The chart file could not be found, or is not listed");
        Chart chart = new Chart();
        Charts.ChartStatus status = Charts.ChartStatus.LOADED;
        try (FileChannel channel = FileChannel.open(chartFile.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
             Scanner chartScanner = new Scanner(channel, StandardCharsets.UTF_8);
             FileLock lock = channel.tryLock()) {
            if(lock == null) throw new RuntimeException("Chart file is locked");
            int line = 1;
            if(chartScanner.hasNextLine()) {
                String input = chartScanner.next();
                if(input.matches("^" + Chart.format.id + "=(Default|YAML)-\\d.\\d")) {
                    if(!chart.getValue(Chart.format).serialize(input.split("=")[1])) {
                        Charts.setFileStatus(chartName, Charts.ChartStatus.INVALID_FORMAT);
                        Bukkit.getLogger().warning("Format version of chart file " + chartFile.getPath() + " cannot be serialized");
                        throw new ChartFileException("Format version of the chart file cannot be serialized.");
                    }
                    if(!chart.formatMatchReaderVersion()) {
                        Charts.setFileStatus(chartName, Charts.ChartStatus.VERSION_MISMATCH);
                        Bukkit.getLogger().warning("Format version of chart file " + chartFile.getPath() + " does not match the reader version of Beatblocks");
                        throw new ChartFileException("Format version of the chart file does not match the reader version of Beatblocks.");
                    }
                }
                else if(!input.matches("^" + Chart.format.id)) {
                    Charts.setFileStatus(chartName, Charts.ChartStatus.INVALID_FORMAT);
                    Bukkit.getLogger().warning("Format key of file " + chartFile.getPath() + " is not appropriate");
                    throw new ChartFileException("Format key is not appropriate.");
                }
                else {
                    Charts.setFileStatus(chartName, Charts.ChartStatus.INVALID_FORMAT);
                    Bukkit.getLogger().warning("Format version of file " + chartFile.getPath() + " is not specified correctly");
                    throw new ChartFileException("Format version is not specified correctly.");
                }
            }
            else {
                Charts.setFileStatus(chartName, Charts.ChartStatus.INVALID_FORMAT);
                Bukkit.getLogger().warning("The chart file " + chartFile.getPath() + " is empty; no content to read");
                throw new ChartFileException("The chart file is empty; no content to read");
            }
            line++;
            while(chartScanner.hasNextLine()) {
                String input = chartScanner.next();
                if(input.matches("^[a-z-]+(=)[a-zA-Z0-9.:]+$")) {
                    if(!chart.getValue(Chart.getKey(input.split("=")[0])).serialize(input.split("=")[1])) {
                        status = Charts.ChartStatus.NEEDS_REWRITE;
                        Bukkit.getLogger().warning("Invalid format of chart data at file " + chartFile.getPath() + ", line " + line + "; ignoring");
                        Bukkit.getLogger().warning(input);
                    }
                }
                else if(input.matches("^chart=$")) {
                    while(chartScanner.hasNextLine()) {
                        input = chartScanner.next();
                        line++;
                        if(input.matches("\\d+,[0-5],[01]")) {
                            String[] split = input.split(",");
                            chart.notes.add(Chart.ChartNote.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]), split[2].equals("1")));
                        }
                        else if(input.matches("^[a-z-]+(=)[a-zA-Z0-9.]+$")) {
                            status = Charts.ChartStatus.NEEDS_REWRITE;
                            Bukkit.getLogger().warning("Unexpected chart data at chart file " + chartFile.getPath() + ", line " + line + "; trailing data will not be saved");
                            Bukkit.getLogger().warning(input);
                        }
                        else {
                            status = Charts.ChartStatus.NEEDS_REWRITE;
                            Bukkit.getLogger().warning("Invalid note format at chart file " + chartFile.getPath() + ", line " + line + "; ignoring");
                            Bukkit.getLogger().warning(input);
                        }
                    }
                }
                else {
                    status = Charts.ChartStatus.NEEDS_REWRITE;
                    Bukkit.getLogger().warning("Not a data format at chart file " + chartFile.getPath() + ", line " + line + "; ignoring");
                    Bukkit.getLogger().warning(input);
                }
                line++;
            }
        } catch (IOException e) {
            throw new BeatblocksException("IOException while reading chart file", e);
        }
        if(!chart.validate().equals("")) {
            Charts.setFileStatus(chartName, Charts.ChartStatus.INVALID_FORMAT);
            throw new ChartFileException("One or more values are missing in chart file:\n" + chart.validate());
        }
        String key = Charts.getSoundPath(chartName);
        if(!Path.of(key).getFileName().toString().equals(chart.getString(Chart.soundFile))) {
            Charts.setFileStatus(chartName, Charts.ChartStatus.NO_SOUND_FILE);
            throw new ChartFileException("The sound file " + chart.getString(Chart.soundFile) + " does not exist, or the value is not matching the sound file name");
        }
        //noinspection IntegerDivisionInFloatingPointContext
        if(chart.notes.stream().noneMatch(note -> (3.5 - chart.getInteger(Chart.keys) / 2) <= note.lane && note.lane <= (3.5 + chart.getInteger(Chart.keys) / 2))) {
            Charts.setFileStatus(chartName, Charts.ChartStatus.INVALID_FORMAT);
            throw new ChartFileException("One or more notes' lane exceeds the keys used at the chart");
        }
        Charts.setFileStatus(chartName, status);
        return chart;
    }
}