package com.fathzer.games.ai;

import java.util.List;

/** An AI able to find the best move(s) during a game.
 * @param <M> Implementation of the Move interface to use
 */
public interface AI<M> {
    
    /**
     * Gets best moves evaluations with the given search parameters
     * <br>This method works on all possible moves for the position. If you want to work on reduced move set, you can use {@link #getBestMoves(List, SearchParameters)} methods
     * @param parameters The search parameters
     * @return The search result
     */
    SearchResult<M> getBestMoves(SearchParameters parameters);

    /**
     * Gets best moves evaluations at the given search parameters
     * <br>This methods evaluates provided moves in the list order. In order to maximize cutoff in some algorithm (like {@link Negamax}),
     * you should order the list in from what is estimated to be the best move to the worst one.
     * @param possibleMoves A list of moves to evaluate. If one of these moves is impossible, result is not specified (It may crash or return a wrong result, etc...).
     * @param parameters The search parameters
     * @return The search result. 
     */
    SearchResult<M> getBestMoves(List<M> possibleMoves, SearchParameters parameters);
    
    /** Interrupts the current search */
	void interrupt();

    /** Tests if the search was interrupted.
     * @return <code>true</code> if the search was interrupted, <code>false</code> otherwise
     */
	boolean isInterrupted();

	/** Gets the statistic related to last search call.
	 * @return The statistics
	 */
	SearchStatistics getStatistics();
}
