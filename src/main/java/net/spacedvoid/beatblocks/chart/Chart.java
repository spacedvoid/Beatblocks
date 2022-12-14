package net.spacedvoid.beatblocks.chart;

import net.spacedvoid.beatblocks.exceptions.ChartFileException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/*
    Limitations: The folder structure must be like:
    beatblocks
        charts
            chartA
                *.cht
                *.ogg
            chartB
                *.cht
                *.ogg
    The parent folder of each chart file(chartA, chartB) will be the name of the chart file specified at the keys of ChartDisplayer.CHARTS to make sure that all chart files' name are all different.

    how keys work:
        Lane values are 0 to 7. For each key values,
        2: 3,4
        4: 2,3,4,5
        6: 1,2,3,4,5,6
        8: 0,1,2,3,4,5,6,7
 */

public class Chart {
    public static final Map<String, ChartValue<?>> chartValues = new HashMap<>();
    public static final Map<ChartKey<?>, String> keyIDs = new HashMap<>();

    public static final ChartKey<StringValue> format = register("format", new StringValue());
    public static final ChartKey<StringValue> soundFile = register("sound-file", new StringValue());
    public static final ChartKey<StringValue> song = register("song", new StringValue());
    public static final ChartKey<StringValue> artist = register("artist", new StringValue());
    public static final ChartKey<StringValue> creator = register("creator", new StringValue());
    public static final ChartKey<DoubleValue> difficulty = register("difficulty",  new DoubleValue());
    public static final ChartKey<DoubleValue> bpm = register("bpm", new DoubleValue());
    public static final ChartKey<TimeValue> time = register("time", new TimeValue());
    public static final ChartKey<IntegerValue> offset = register("offset", new IntegerValue());
    public static final ChartKey<IntegerValue> keys = register("keys", new IntegerValue());

    public final List<ChartNote> notes  = new ArrayList<>();

    public final Map<String, ChartValue<?>> values = newValues();

    private static <T extends ChartValue<?>> ChartKey<T> register(String id, ChartValue<T> value) {
        ChartKey<T> key = new ChartKey<>(id);
        chartValues.put(id, value);
        keyIDs.put(key, id);
        return key;
    }

    public static ChartKey<?> getKey(String id) {
        Stream<?> stream = keyIDs.entrySet().stream().filter(entry -> id.equals(entry.getValue())).map(Map.Entry::getKey);
        return (ChartKey<?>)stream.findFirst().orElse(null);
    }

    private static Map<String, ChartValue<?>> newValues() {
        Map<String, ChartValue<?>> copy = new HashMap<>();
        chartValues.forEach((string, value) -> {
            if(value instanceof DoubleValue) {
                copy.put(string, new DoubleValue());
            }
            else if(value instanceof StringValue) {
                copy.put(string, new StringValue());
            }
            else if(value instanceof TimeValue) {
                copy.put(string, new TimeValue());
            }
            else if(value instanceof IntegerValue) {
                copy.put(string, new IntegerValue());
            }
        });
        return copy;
    }

    /**
     * @return String list of invalid categories, or null if none found
     */
    public String validate() {
        List<String> list = new ArrayList<>();
        if((getStringValue(soundFile)).isInvalid()) list.add(soundFile.id);
        if((getStringValue(song)).isInvalid()) list.add(song.id);
        if((getStringValue(artist)).isInvalid()) list.add(artist.id);
        if((getStringValue(creator)).isInvalid()) list.add(creator.id);
        if(getDouble(difficulty) == null || !(getDouble(difficulty) >= 0.1 && getDouble(difficulty) <= 10)) list.add(difficulty.id);
        if(getDouble(bpm) == null || getDouble(bpm) <= 0) list.add(bpm.id);
        if(getTime(time) != null) try { getTime(time).validate(); } catch (IllegalArgumentException e) { list.add(time.id); }
        else list.add(time.id);
        if(getInteger(keys) != null) {
            int keys = getInteger(Chart.keys);
            if(!(keys == 2 || keys == 4 || keys == 6 || keys == 8)) list.add(Chart.keys.id);
        }
        else list.add(keys.id);
        if(notes.size() == 0) list.add("notes");
        if(list.size() == 0) return null;
        return String.join(",", list);
    }

    public ChartValue<?> getValue(ChartKey<?> key) {
        return this.values.get(keyIDs.get(key));
    }

    public Integer getInteger(ChartKey<IntegerValue> key) {
        return ((IntegerValue)getValue(key)).getValue();
    }

    public Double getDouble(ChartKey<DoubleValue> key) {
        return ((DoubleValue)getValue(key)).getValue();
    }

    public String getString(ChartKey<StringValue> key) {
        return ((StringValue)getValue(key)).getValue();
    }

    public Time getTime(ChartKey<TimeValue> key) {
        return ((TimeValue)getValue(key)).getValue();
    }

    /*public IntegerValue getIntegerValue(ChartKey<IntegerValue> key) {
        return (IntegerValue)getValue(key);
    }*/

    /*public DoubleValue getDoubleValue(ChartKey<DoubleValue> key) {
        return (DoubleValue)getValue(key);
    }*/

    public StringValue getStringValue(ChartKey<StringValue> key) {
        return (StringValue)getValue(key);
    }

    /*public TimeValue getTimeValue(ChartKey<TimeValue> key) {
        return (TimeValue)getValue(key);
    }*/

    @SuppressWarnings("unused")
    public static class ChartKey<T> {
        public final String id;
        ChartKey(String id) {
            this.id = id;
        }
    }

    @SuppressWarnings("unused")
    public static abstract class ChartValue<T extends ChartValue<?>> {
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public abstract boolean serialize(@NotNull String s);
    }

    private static class IntegerValue extends ChartValue<IntegerValue> {
        private Integer value = null;

        @Override
        public boolean serialize(@NotNull String s) {
            if(s.matches("^-?[0-9]+$")) {
                this.value = Integer.parseInt(s);
                return true;
            }
            return false;
        }

        Integer getValue() {
            return value;
        }
    }

    private static class DoubleValue extends ChartValue<DoubleValue> {
        private Double value = null;

        @Override
        public boolean serialize(@NotNull String s) {
            if(s.matches("^[-+]?[0-9]+(.[0-9]+)?$")) {
                this.value = Double.parseDouble(s);
                return true;
            }
            return false;
        }

        Double getValue() {
            return value;
        }

    }

    private static class StringValue extends ChartValue<StringValue> {
        private String value = null;

        @Override
        public boolean serialize(@NotNull String s) {
            this.value = s;
            return true;
        }

        String getValue() {
            return value;
        }

        /**
         * Also returns {@code false} when length is 0.
         */
        boolean isInvalid() {
            if(value == null) return true;
            else return value.length() == 0;
        }
    }

    private static class TimeValue extends ChartValue<TimeValue> {
        private Time value = null;

        @Override
        public boolean serialize(@NotNull String s) {
            if(s.matches("[0-9]{1,2}:[0-9]{2}")) {
                String[] split = s.split(":");
                int min = Integer.parseInt(split[0]), sec = Integer.parseInt(split[1]);
                if(!(min >= 0 && min < 60) || !(sec >= 0 && sec < 60) || (min == 0 && sec == 0)) return false;
                this.value = Time.parseString(s);
                return true;
            }
            return false;
        }

        Time getValue() {
            return value;
        }

    }

    public static class ChartNote {
        public final NoteInfo info;
        public final boolean isDoubleAccuracy;

        ChartNote(NoteInfo info, boolean isDoubleAccuracy) {
            this.info = info;
            this.isDoubleAccuracy = isDoubleAccuracy;
        }

        public static ChartNote of(int tick, int lane, boolean isDoubleAccuracy) {
            if(tick < 0) {
                throw new ChartFileException("The tick must be more than or equal to 0");
            }
            if(!(lane >= 0 && lane <= 7)) {
                throw new ChartFileException("The lane must be between 0 to 3 or 5 to 8, inclusive");
            }
            return new ChartNote(new NoteInfo(tick, lane), isDoubleAccuracy);
        }
    }
}
