package com.fathzer.games.ai;

import com.fathzer.games.util.exec.Interruptible;

/** An {@link AI} based on <a href="https://en.wikipedia.org/wiki/Depth-first_search">depth-first search</a> algorithm.
 * @param <M> Implementation of the Move interface to use
 * @param <P> Implementation of the SearchParameters interface to use
 */
public interface DepthFirstAI<M, P extends DepthFirstSearchParameters> extends AI<M,P>, Interruptible {

	/** Gets the statistic related to last search call.
	 * @return The statistics
	 */
	SearchStatistics getStatistics();
}
