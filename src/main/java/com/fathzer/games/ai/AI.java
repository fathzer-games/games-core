package com.fathzer.games.ai;

import java.util.List;

import com.fathzer.games.util.Evaluation;

/** An AI able to find the best move(s) during a game.
 * @param <M> Implementation of the Move interface to use
 */
public interface AI<M> {
    
    /**
     * Gets best moves evaluations at the given search depth
     * @param depth The search depth (must be &gt; 0)
     * @param size How many best moves is requested (Integer.MAX_VALUE to have all moves).
     * @param accuracy The minimum gap under which a move is considered having the same value than another.
     * 				This allows to obtain moves that are almost equivalent to the last strictly best move.
     * @return The list of possible moves in descending evaluation order (best first)
     * <br>Please note the list may have more than size elements in case of equivalent moves or almost equivalent moves.
     * It can also have less than size elements if there's less than size legal moves. 
     */
    List<Evaluation<M>> getBestMoves(final int depth, int size, int accuracy);

    /**
     * Gets best moves evaluations at the given search depth
     * <br>This methods iterates over provided ordered moves to evaluate each and maximize cutoff.
     * @param depth The search depth (must be &gt; 0)
     * @param possibleMoves The list of possible moves
     * @param size How many best moves is requested (Integer.MAX_VALUE to have all moves evaluated).
     * @param accuracy The minimum gap under which a move is considered having the same value than another.
     * 				This allows to obtain moves that are almost equivalent to the last strictly best move.
     * @return The list of possible moves in descending evaluation order (best first)
     * <br>Please note the list may have more than size elements in case of equivalent moves or almost equivalent moves.
     * It can also have less than size elements if there's less than size elements in possibleMoves argument. 
     */
    List<Evaluation<M>> getBestMoves(final int depth, List<M> possibleMoves, int size, int accuracy);
    
	public void interrupt();

	public boolean isInterrupted();
}
