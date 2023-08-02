package com.fathzer.games.ai;

import java.util.List;

/** An AI able to find the best move(s) during a game.
 * @param <M> Implementation of the Move interface to use
 */
public interface AI<M> {
    
    /**
     * Gets best moves evaluations at the given search depth
     * @param depth The search depth (must be &gt; 0)
     * @param size How many best moves are requested to have an exact value (Integer.MAX_VALUE to have all moves).
     * @param accuracy The minimum gap under which a move is considered having the same value than another.
     * 				This allows to obtain moves that are almost equivalent to the last strictly best move.
     * @return The search result
     */
    SearchResult<M> getBestMoves(final int depth, int size, int accuracy);

    /**
     * Gets best moves evaluations at the given search depth
     * <br>This methods evaluates provided moves in the list order. In order to maximize cutoff in some algorithm (like {@link Negamax}),
     * you should order the list in from what is estimated to be the best move to the worst one.
     * @param possibleMoves A list of moves to evaluate. If one of these moves is impossible, result is not specified (It may crash or return a wrong result, etc...).
     * @param depth The search depth (must be &gt; 0)
     * @param size How many best moves is requested (Integer.MAX_VALUE to have all moves evaluated).
     * @param accuracy The minimum gap under which a move is considered having the same value than another.
     * 				This allows to obtain moves that are almost equivalent to the last strictly best move.
     * @return The search result. 
     */
    SearchResult<M> getBestMoves(List<M> possibleMoves, int depth, int size, int accuracy);
    
	public void interrupt();

	public boolean isInterrupted();
}
