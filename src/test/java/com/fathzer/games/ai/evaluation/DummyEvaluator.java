package com.fathzer.games.ai.evaluation;

import com.fathzer.games.Color;
import com.github.bhlangonijr.chesslib.move.Move;

public class DummyEvaluator implements Evaluator<Move, Object> {
	@Override
	public void setViewPoint(Color color) {
	}

	@Override
	public int evaluate() {
		return 0;
	}
}