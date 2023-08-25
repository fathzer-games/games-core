package com.fathzer.games.ai.iterativedeepening;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.fathzer.games.ai.evaluation.EvaluatedMove;

public class RandomMoveSelector<M> extends MoveSelector<M> {
	public static final Random RND = new Random(); 
	
	@Override
	public List<EvaluatedMove<M>> select(IterativeDeepeningSearch<M> search, List<EvaluatedMove<M>> bestMoves) {
		return Collections.singletonList(bestMoves.get(RND.nextInt(bestMoves.size())));
	}
}
