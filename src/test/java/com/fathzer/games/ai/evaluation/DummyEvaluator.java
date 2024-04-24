package com.fathzer.games.ai.evaluation;

public class DummyEvaluator<M,B> implements StaticEvaluator<M, B> {
	@Override
	public int evaluate(B board) {
		return 0;
	}
}