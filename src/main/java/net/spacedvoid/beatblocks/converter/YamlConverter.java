package net.spacedvoid.beatblocks.converter;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.spacedvoid.beatblocks.chart.Chart;
import net.spacedvoid.beatblocks.exceptions.BeatblocksException;
import net.spacedvoid.beatblocks.printer.Printer;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class YamlConverter {
	public CompletableFuture<Chart> convertAsync(Path chartPath, Audience sender) {
		return CompletableFuture.supplyAsync(() -> convert(chartPath, sender));
	}

	public Chart convert(Path chartPath, Audience sender) {
		if(!Files.isRegularFile(chartPath)) throw new BeatblocksException("File cannot be found: " + chartPath);
		Constructor constructor = new Constructor(Format.class);
		constructor.setPropertyUtils(new PropertyUtils() {
			@Override
			public Property getProperty(Class<?> type, String name){
				if (name.indexOf('-') > -1) {
					name = camelize(name);
				}
				return super.getProperty(type, name);
			}
		});
		Yaml yaml = new Yaml(constructor);
		Chart chart = new Chart(chartPath.getFileName().toString());
		Format data;
		try (InputStream is = new BufferedInputStream(new FileInputStream(chartPath.toFile()))) {
			data = yaml.load(is);
		} catch (IOException e) {
			throw new BeatblocksException("Failed to load file", e);
		} catch (YAMLException e) {
			throw new BeatblocksException("Failed to parse yaml. Likely caused by an invalid type for key.", e);
		}
		data.map(chart);
		Double bpm = chart.getDouble(Chart.bpm);
		if(bpm == null || bpm <= 0) throw new BeatblocksException("Invalid BPM: " + (bpm == null ? "null" : bpm.toString()), false);
		String format = chart.getString(Chart.format);
		if(format == null) throw new BeatblocksException("Invalid format: null");
		else if(!format.matches("^RAW-\\d.\\d$")) throw new BeatblocksException("Invalid format: " + format, false);
		List<Note> notes = data.chart;
		Iterator<Note> iterator = notes.iterator();
		if(!iterator.hasNext()) throw new BeatblocksException("No chart information!", false);
		Note current = iterator.next();
		int baseBeats = data.unit;
		if(iterator.hasNext()) {
			Note next = iterator.next();
			double currentSeconds = 0;
			double spb = 1 / bpm * 60;     // Seconds Per Beat
			while(true) {
				double nextSeconds = currentSeconds + spb * current.getBeats() / baseBeats;
				chart.notes.add(Chart.ChartNote.of((int)(currentSeconds / 0.05), current.getLane(), isDoubleAccuracy(currentSeconds, nextSeconds)));
				currentSeconds = nextSeconds;
				current = next;
				if(iterator.hasNext()) next = iterator.next();
				else {
					chart.notes.add(Chart.ChartNote.of((int)(currentSeconds / 0.05), current.getLane(), isDoubleAccuracy(currentSeconds)));
					break;
				}
			}
		} else {
			chart.notes.add(Chart.ChartNote.of(0, current.getLane(), true));
		}
		new Printer().print(chart, chartPath.getParent());
		sender.sendMessage(Component.text("Successfully converted chart file."));
		return chart;
	}

	private String camelize(String input) {
		StringBuilder builder = new StringBuilder(input);
		for(int i = 0; i < builder.length(); i++)
			if(builder.charAt(i) == '-' || builder.charAt(i) == ' ') builder.delete(i, i + 1).replace(i, i + 1, String.valueOf(builder.charAt(i)).toUpperCase());
		return builder.toString();
	}

	private boolean isDoubleAccuracy(double current) {
		return current - 0.01 < (int)current || current + 0.01 >= (int)current;
	}

	private boolean isDoubleAccuracy(double current, double next) {
		if(current - 0.01 < (int)current || current + 0.01 >= (int)current) return (int)next > (int)current + 1;
		return false;
	}

	@SuppressWarnings("unused")
	private static class Format {
		public String format, soundFile, song, artist, creator, time;
		public Integer offset, keys, unit;
		public Double difficulty, bpm;
		public List<Note> chart;

		public void map(Chart dest) {
			Map<String, Boolean> verify = new HashMap<>(10);
			verify.put(Chart.format.id, dest.getValue(Chart.format).serialize(format));
			verify.put(Chart.soundFile.id, dest.getValue(Chart.soundFile).serialize(soundFile));
			verify.put(Chart.song.id, dest.getValue(Chart.song).serialize(song));
			verify.put(Chart.artist.id, dest.getValue(Chart.artist).serialize(artist));
			verify.put(Chart.creator.id, dest.getValue(Chart.creator).serialize(creator));
			verify.put(Chart.time.id, dest.getValue(Chart.time).serialize(time));
			verify.put(Chart.offset.id, dest.getIntegerValue(Chart.offset).setValue(offset));
			verify.put(Chart.keys.id, dest.getIntegerValue(Chart.keys).setValue(keys));
			verify.put(Chart.difficulty.id, dest.getDoubleValue(Chart.difficulty).setValue(difficulty));
			verify.put(Chart.bpm.id, dest.getDoubleValue(Chart.bpm).setValue(bpm));
			verify.put("unit", unit != null);
			StringJoiner joiner = new StringJoiner(", ");
			verify.entrySet().stream().filter(entry -> !entry.getValue()).map(Map.Entry::getKey).forEach(joiner::add);
			if(joiner.length() != 0) throw new BeatblocksException("Mapping failed! Invalid keys: " + joiner, false);
		}
	}

	/**
	 * Use getters instead of directly accessing the variables.
	 */
	@SuppressWarnings("unused")
	private static class Note {
		public Integer b, beats;
		public Integer l, lane;

		public int getBeats() {
			if(b == null && beats != null) return beats;
			else if(b != null && beats == null) return b;
			else if(b == null) throw new BeatblocksException("No value for beats!", false);
			else throw new BeatblocksException("Two conflicting values for beats (" + b + " and " + beats + ")", false);
		}

		public int getLane() {
			if(l == null && lane != null) return lane;
			else if(l != null && lane == null) return l;
			else if(l == null) throw new BeatblocksException("No value for lane!", false);
			else throw new BeatblocksException("Two conflicting values for lane (" + l + " and " + lane + ")", false);
		}
	}
}
