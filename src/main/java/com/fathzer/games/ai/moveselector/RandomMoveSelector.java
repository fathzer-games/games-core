package com.fathzer.games.ai.moveselector;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.fathzer.games.ai.evaluation.EvaluatedMove;

public class RandomMoveSelector<M,D> extends MoveSelector<M,D> {
	public static final Random RND = new Random(); 
	
	@Override
	public List<EvaluatedMove<M>> select(D data, List<EvaluatedMove<M>> bestMoves) {
		return Collections.singletonList(bestMoves.get(RND.nextInt(bestMoves.size())));
	}
}
