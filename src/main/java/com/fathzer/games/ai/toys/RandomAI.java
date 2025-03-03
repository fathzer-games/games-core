package com.fathzer.games.ai.toys;

import java.util.Random;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.evaluation.Evaluation;

/** A fake AI that randomly chooses a score for each move.
 */
public class RandomAI<M,B extends MoveGenerator<M>> extends BasicAI<M, B> {
	@SuppressWarnings("java:S2245") //Ignores Sonar security hot spot, here Random is safe
	/** A random engine instance. */
	private static final Random RND = new Random();
	
	/** Constructor.
	 * @param board The board on which the AI plays
	 */
	public RandomAI(B board) {
		super(board);
	}
	
	protected Evaluation getEvaluation(M move) {
		return Evaluation.score(RND.nextInt());
	}
}
