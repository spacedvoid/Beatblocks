package net.spacedvoid.beatblocks.common;

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

public enum Judgements {
	PERFECT(Component.text("P").color(TextColor.fromHexString("#FF1616")).append(Component.text("E").color(TextColor.fromHexString("#FF914D"))).append(Component.text("R").color(TextColor.fromHexString("#FFDE59"))).append(Component.text("F").color(TextColor.fromHexString("#7ED957"))).append(Component.text("E").color(TextColor.fromHexString("#5271FF"))).append(Component.text("C").color(TextColor.fromHexString("#8C52FF"))).append(Component.text("T").color(TextColor.fromHexString("#7F00FF")))),
	GREAT(Component.text("GREAT").color(TextColor.fromHexString("#FFDE59"))),
	GOOD(Component.text("GOOD").color(TextColor.fromHexString("#FFDE59"))),
	FAST(Component.text("FAST").color(TextColor.fromHexString("#0871DA"))),
	SLOW(Component.text("SLOW").color(TextColor.fromHexString("#27CB0D"))),
	MISS(Component.text("MISS", NamedTextColor.GRAY));

	public final TextComponent text;

	Judgements(TextComponent text) {
		this.text = text;
	}
}
