package com.fathzer.games.ai.evaluation;

import com.fathzer.games.Color;

public interface ColorSensitiveEvaluator<B> {
	/** Sets the point of view from which the evaluation should be made. 
	 * @param color The color from which the evaluation is made, null to evaluate the position from the point of view of the current player.
	 */
	void setViewPoint(Color color);

	/** Evaluates a board's position.
	 * @return An integer
	 */
	int evaluate(B board);
}
