package net.spacedvoid.beatblocks.singleplayer.chart;

public class Time {
	public final int minutes;
	public final int seconds;

	public Time(int minutes, int seconds) {
		validate();
		this.minutes = minutes;
		this.seconds = seconds;
	}

	public void validate() {
		if(minutes < 0 || minutes >= 60) throw new IllegalArgumentException("Minutes must be between 0 and 59, inclusive");
		if(seconds < 0 || seconds >= 60) throw new IllegalArgumentException("Seconds must be between 0 and 59, inclusive");
		if(minutes == 0 && seconds == 0) throw new IllegalArgumentException("Cannot create time with 0:00");
	}

	public static Time parseString(String s) {
		String[] split = s.split(":");
		return new Time(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
	}

	@Override
	public String toString() {
		return minutes + ":" + seconds;
	}
}
