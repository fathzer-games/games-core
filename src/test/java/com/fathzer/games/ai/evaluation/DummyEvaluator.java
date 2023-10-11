package com.fathzer.games.ai.evaluation;

import com.fathzer.games.Color;

public class DummyEvaluator implements Evaluator<Object> {
	@Override
	public void setViewPoint(Color color) {
	}

	@Override
	public int evaluate(Object board) {
		return 0;
	}
}