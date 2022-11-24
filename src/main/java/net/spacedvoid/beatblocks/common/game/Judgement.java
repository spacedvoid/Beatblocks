package net.spacedvoid.beatblocks.common.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/*
    Timings will be floored by ticks, one tick being 0.05 seconds(50 ms).
    FAST and SLOW are not individual judgements; they are a subclass of GOOD, and will be displayed after the game has ended - this behavior can be changed to directly display them.
    GOOD(including FAST and SLOW) is going to be removed if judgements are too loose.
    The first note will have judgement boosts, all judgements except MISS will be calculated as PERFECT since there are no bpm indicators. The display of this judgement can be set.
    Timings between 0.00 ~ 0.01 or 0.04 ~ 0.05 seconds will have 2 ticks of "perfect" judgements(in the perfect tick + the right after/before tick), 1 tick for others.
    Because each tick is too long for accurate judgements(50 ms), each tick beyond "perfect" will reduce accuracy.
    Accuracies will have two modes: one having all accuracies, the other having only PERFECT,(GOOD,) and MISS for tighter judgements.
 */

public enum Judgement {
	PERFECT(Component.text("P").color(TextColor.fromHexString("#FF1616"))
		.append(Component.text("E").color(TextColor.fromHexString("#FF914D")))
		.append(Component.text("R").color(TextColor.fromHexString("#FFDE59")))
		.append(Component.text("F").color(TextColor.fromHexString("#7ED957")))
		.append(Component.text("E").color(TextColor.fromHexString("#5271FF")))
		.append(Component.text("C").color(TextColor.fromHexString("#8C52FF")))
		.append(Component.text("T").color(TextColor.fromHexString("#7F00FF"))), 0),
	GREAT(Component.text("GREAT").color(TextColor.fromHexString("#FFDE59")), 1),
	GOOD(Component.text("GOOD").color(TextColor.fromHexString("#FFDE59")), 2),
	FAST(Component.text("FAST").color(TextColor.fromHexString("#0871DA")), GOOD, 3),
	SLOW(Component.text("SLOW").color(TextColor.fromHexString("#27CB0D")), GOOD, 4),
	MISS(Component.text("MISS", NamedTextColor.GRAY), 5);

	public final TextComponent text;
	private final Judgement parent;
	public final int ordinal;

	Judgement(TextComponent text, int ordinal) {
		this.text = text;
		this.parent = null;
		this.ordinal = ordinal;
	}

	Judgement(TextComponent text, Judgement parent, int ordinal) {
		this.text = text;
		this.parent = parent;
		this.ordinal = ordinal;
	}

	public static Judgement get(int currentTiming, int noteTiming) {
		if(isInRange(currentTiming, noteTiming, 0, 0)) return PERFECT;
		else if(isInRange(currentTiming, noteTiming, 1, 1)) return GREAT;
		else if(isInRange(currentTiming, noteTiming, 2, 0)) return FAST;
		else if(isInRange(currentTiming, noteTiming, 0, 2)) return SLOW;
		else return MISS;
	}

	public Judgement getParent() {
		return this.parent == null? this : this.parent;
	}

	private static boolean isInRange(int compare, int with, int left, int right) {
		return with - left <= compare && compare <= with + right;
	}
}
