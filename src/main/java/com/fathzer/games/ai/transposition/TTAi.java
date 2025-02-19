package com.fathzer.games.ai.transposition;

import com.fathzer.games.ai.AI;
import com.fathzer.games.ai.evaluation.Evaluator;

/** An AI that uses a transposition table to speed up its search.
 *  <br>
 */
public interface TTAi<M> extends AI<M> {
	/** Sets the transposition table.
	 * @param table The new transposition table (null to not use any transposition table)
	 */
	void setTranspositonTable(TranspositionTable<M> table);
	
	/** Gets the transposition table.
	 * @return a transposition table or null
	 */
    TranspositionTable<M> getTranspositionTable();

	/** Converts the value used in the search algorithm to a value to be stored in the transposition table.
	 * <br>Typically, it replaces mat in n from root score to mat in k from here score.
	 * <br>See <a href="https://github.com/maksimKorzh/chess_programming/blob/master/src/bbc/tt_search_mating_scores/TT_mate_scoring.txt">this</a> to have more explanations of why this method exists
	 * @param score The score used in the search algorithm. 
	 * @param depth The current search depth.
	 * @param maxDepth The root search depth (always &gt;= depth).
	 * @param evaluator The evaluator used by the AI.
	 * @return The score to store in the transposition table
	 */
	default int scoreToTT(int score, int depth, int maxDepth, Evaluator<?, ?> evaluator) {
		if (evaluator.isWinLooseScore(score)) {
			final int nbHalfMoves = maxDepth - depth;
			return score>0 ? score + nbHalfMoves : score - nbHalfMoves;
		} else {
			return score;
		}
	}
	
	/** Converts the value stored in the transposition table to a value to be used in the search algorithm.
	 * <br>It is the inverse function of {@link #scoreToTT(int, int, int, Evaluator)}
	 * <br>See <a href="https://github.com/maksimKorzh/chess_programming/blob/master/src/bbc/tt_search_mating_scores/TT_mate_scoring.txt">this</a> to have more explanations of why this method exists
	 * @param encodedValue The value used store in the transposition table. 
	 * @param depth The current search depth.
	 * @param maxDepth The root search depth (always &gt;= depth).
	 * @param evaluator The evaluator used by the AI.
	 * @return The score to use in the search algorithm
	 */
	default int ttToScore(int encodedValue, int depth, int maxDepth, Evaluator<?, ?> evaluator) {
		if (evaluator.isWinLooseScore(encodedValue)) {
			final int nbHalfMoves = maxDepth - depth;
			return encodedValue>0 ? encodedValue - nbHalfMoves : encodedValue + nbHalfMoves;
		} else {
			return encodedValue;
		}
	}
}
