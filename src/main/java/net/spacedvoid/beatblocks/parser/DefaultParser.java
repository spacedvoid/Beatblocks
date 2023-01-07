package net.spacedvoid.beatblocks.parser;

import net.spacedvoid.beatblocks.chart.Chart;
import net.spacedvoid.beatblocks.charts.Charts;
import net.spacedvoid.beatblocks.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.exceptions.ChartFileException;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

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

public class DefaultParser implements Parser {
    public static final String PARSER_FORMAT = "Default-1.0";

    public CompletableFuture<Chart> readChartAsync(Path chartFile) {
        return CompletableFuture.supplyAsync(() -> readChart(chartFile));
    }

    public Chart readChart(Path chartPath) throws ChartFileException {
        if(!Files.isDirectory(chartPath)) throw new BeatblocksException(chartPath + " is not a directory");
        File chartFile = chartPath.toFile();
        if(!chartFile.exists()) throw new ChartFileException("The chart file could not be found, or is not listed");
        Chart chart = new Chart(chartPath.getFileName().toString());
        Charts.ChartStatus status = Charts.ChartStatus.LOADED;
        try (FileChannel channel = FileChannel.open(chartFile.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
             Scanner chartScanner = new Scanner(channel, StandardCharsets.UTF_8);
             FileLock lock = channel.tryLock()) {
            if(lock == null) throw new RuntimeException("Chart file is locked");
            int line = 1;
            if(chartScanner.hasNextLine()) {
                String input = chartScanner.next();
                if(input.matches("^" + Chart.format.id + "=(Default|YAML)-\\d.\\d$")) {
                    if(!chart.getValue(Chart.format).serialize(input.split("=")[1])) {
                        Charts.setFileStatus(chartPath, Charts.ChartStatus.INVALID_FORMAT);
                        throw new ChartFileException("Format version of the chart file cannot be serialized");
                    }
                    if(!chart.getString(Chart.format).equals(PARSER_FORMAT)) {
                        Charts.setFileStatus(chartPath, Charts.ChartStatus.VERSION_MISMATCH);
                        throw new ChartFileException("Format version of the chart file does not match the reader version of Parser - must be " + PARSER_FORMAT);
                    }
                }
                else {
                    Charts.setFileStatus(chartPath, Charts.ChartStatus.INVALID_FORMAT);
                    throw new ChartFileException("Format version is not specified correctly.");
                }
            }
            else {
                Charts.setFileStatus(chartPath, Charts.ChartStatus.INVALID_FORMAT);
                throw new ChartFileException("The chart file is empty; no content to read");
            }
            line++;
            while(chartScanner.hasNextLine()) {
                String input = chartScanner.next();
                if(input.matches("^[a-z-]+(=)[a-zA-Z0-9.: ]+$")) {
                    input = input.strip();
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
            throw new BeatblocksException("Exception while parsing chart", e);
        }
        chart.notes.sort(Comparator.comparingInt(note -> note.info.timing));
        if(chart.validate() != null) {
            Charts.setFileStatus(chartPath, Charts.ChartStatus.INVALID_FORMAT);
            throw new ChartFileException("One or more values are missing in chart file: " + chart.validate());
        }
        if(!new File(chartPath.toAbsolutePath().getParent().toString(), chart.getString(Chart.soundFile) + ".ogg").exists()) {
            Charts.setFileStatus(chartPath, Charts.ChartStatus.NO_SOUND_FILE);
            throw new ChartFileException("Sound file " + chart.getString(Chart.soundFile) + ".ogg does not exist, or the value is not matching the sound file name");
        }
        //noinspection IntegerDivisionInFloatingPointContext
        if(chart.notes.stream().noneMatch(note -> (3.5 - chart.getInteger(Chart.keys) / 2) <= note.info.lane && note.info.lane <= (3.5 + chart.getInteger(Chart.keys) / 2))) {
            Charts.setFileStatus(chartPath, Charts.ChartStatus.INVALID_FORMAT);
            throw new ChartFileException("One or more notes' lane exceeds the keys used at the chart");
        }
        Charts.setFileStatus(chartPath, status);
        return chart;
    }
}