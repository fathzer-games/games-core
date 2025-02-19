package com.fathzer.games.ai.evaluation;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.SearchContext;

/** An evaluator that performs quiescence search before evaluating.
 * @param <M> The type of moves 
 * @param <B> The type of the evaluator
 */
public interface QuiesceEvaluator<M, B extends MoveGenerator<M>> {
	/** Gets the position evaluation after performing a quiescence search. 
	 * @param ctx The search context (The position can be found using {@link SearchContext#getGamePosition()}
	 * @param depth The current depth
	 * @param alpha The current alpha value
	 * @param beta The current beta
	 * @return The evaluation
	 */
	int evaluate(SearchContext<M, B> ctx, int depth, int alpha, int beta);
}
