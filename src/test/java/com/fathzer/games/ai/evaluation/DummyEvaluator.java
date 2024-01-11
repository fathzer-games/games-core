package com.fathzer.games.ai.evaluation;

import com.fathzer.games.Color;
import com.github.bhlangonijr.chesslib.move.Move;

public class DummyEvaluator implements StaticEvaluator<Move, Object> {
	@Override
	public void setViewPoint(Color color) {
	}

	@Override
	public int evaluate(Object board) {
		return 0;
	}
}