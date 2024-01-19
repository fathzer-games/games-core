package com.fathzer.games.ai.evaluation;

public interface ColorSensitiveEvaluator<B> {
	/** Evaluates a board's position.
	 * @return An integer
	 */
	int evaluate(B board);
}
