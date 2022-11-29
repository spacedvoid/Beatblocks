package net.spacedvoid.beatblocks.common.exceptions;

public class ChartFileException extends BeatblocksException {
    public ChartFileException(String msg) {
        super(msg, false);
    }

    @Override
    public String getLocalizedMessage() {
        return this.getMessage();
    }
}
