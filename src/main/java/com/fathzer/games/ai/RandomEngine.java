package com.fathzer.games.ai;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

import com.fathzer.games.MoveGenerator;

/** A trivial engine that randomly plays a possible move.
 */
public class RandomEngine<M,B extends MoveGenerator<M>> implements Function<B, M> {
	@SuppressWarnings("java:S2245") //Ignores Sonar security hot spot, here Random is safe 
	private static final Random RND = new Random();
	
	@Override
	public M apply(B board) {
		final List<M> possibleMoves = board.getLegalMoves();
		return possibleMoves.isEmpty() ? null : possibleMoves.get(RND.nextInt(possibleMoves.size()));
	}
}
