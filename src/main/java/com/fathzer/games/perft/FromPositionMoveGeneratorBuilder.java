package com.fathzer.games.perft;

import com.fathzer.games.MoveGenerator;

/** A builder of move generators that converts a textual representation of a position to a move generator.
 * @param <M> The type of moves
 * @param <B> The type of move generator
 */
@FunctionalInterface
public interface FromPositionMoveGeneratorBuilder<M, B extends MoveGenerator<M>> {
	/** Builds a move generator that is initialized to the position represented by a given string.
	 * @param position the string representing the position.
	 * @return a move generator.
	 */
	B fromPosition(String position);
}
