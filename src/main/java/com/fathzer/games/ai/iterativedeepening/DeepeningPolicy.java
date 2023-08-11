package com.fathzer.games.ai.iterativedeepening;

import com.fathzer.games.ai.SearchResult;

/** A policy that manages how to deepen the search.
 * <br>Typically, it decides at which depth to start, what increment to add at each step, and if we should end prematurely. 
 */
public interface DeepeningPolicy {
	/** Gets the start depth.
	 * @return the start depth, default is 2.
	 */
	default int getStartDepth() {
		return 2;
	}

	/** Get next depth.
	 * @param currentDepth currentDepth
	 * @return next depth, a negative value to stop deepening. Default is currentDepth+1 
	 */
	default int getNextDepth(int currentDepth) {
		return currentDepth+1;
	}

	/** This method is called every time an search is made.
	 * <br>The default implementation does nothing, but custom policy can use this data,
	 * for instance, to decide that result is stable enough to stop deepening before max depth is reached.
	 * @param <M> The type of moves
	 * @param depth The depth that just finished
	 * @param result The results returned by the search
	 */
	default <M> void addSearchStage(int depth, SearchResult<M> result) {
		// Does Nothing by default
	}
}
