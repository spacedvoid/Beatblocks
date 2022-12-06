package net.spacedvoid.beatblocks.common.chart;

public class NoteInfo {
	public final int timing;
	public final int lane;

	public NoteInfo(int timing, int lane) {
		this.timing = timing;
		this.lane = lane;
	}

	@Override
	public String toString() {
		return "{" + "timing=" + timing + ", lane=" + lane + "}";
	}
}
