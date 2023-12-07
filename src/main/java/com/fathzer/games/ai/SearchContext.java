package com.fathzer.games.ai;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.evaluation.Evaluator;

public class SearchContext<M, B extends MoveGenerator<M>> {
	private B gamePosition;
	private Evaluator<M, B> evaluator;
	
	public SearchContext(B gamePosition, Evaluator<M, B> evaluator) {
		this.gamePosition = gamePosition;
		this.evaluator = evaluator;
	}

	public B getGamePosition() {
		return gamePosition;
	}
	
	public Evaluator<M, B> getEvaluator() {
		return evaluator;
	}
}