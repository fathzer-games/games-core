package com.fathzer.games.ai.moveselector;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.fathzer.games.ai.evaluation.EvaluatedMove;

/** A move selector that selects moves randomly.
 * @param <M> The type of moves
 * @param <D> The type of data passed by the engine to the MoveSelector
 */
public class RandomMoveSelector<M,D> extends MoveSelector<M,D> {
	@SuppressWarnings("java:S2245") //Ignores Sonar security hot spot, here Random is safe 
	private static final Random RND = new Random(); 
	
	@Override
	public List<EvaluatedMove<M>> select(D data, List<EvaluatedMove<M>> bestMoves) {
		return bestMoves.isEmpty() ? Collections.emptyList() : Collections.singletonList(bestMoves.get(RND.nextInt(bestMoves.size())));
	}
}
