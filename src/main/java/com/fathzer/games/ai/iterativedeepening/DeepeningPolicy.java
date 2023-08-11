package com.fathzer.games.ai.iterativedeepening;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fathzer.games.ai.evaluation.EvaluatedMove;

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

	/** This method is called every time a search is made to determine which moves should be deepened.
	 * @param <M> The type of moves
	 * @param depth The depth that just finished
	 * @param evaluations The evaluations obtained at this depth
	 * @param ended A unordered list of moves for which evaluation is already done.
	 * @return A list of moves to deepen, an empty list to stop deepening. Ended is updated with evaluations of moves that are not returned.
	 * <br>The default implementation returns an empty list if first move has a win or loose evaluation. If not, all moves that are not ended, but custom policy can use this data,
	 * for instance, to decide that result is stable enough to stop deepening before max depth is reached.
	 */
	default <M> List<M> getMovesToDeepen(int depth, List<EvaluatedMove<M>> evaluations, List<EvaluatedMove<M>> ended) {
		if (evaluations.get(0).isEnd()) {
			// if best move is a win/loose, continuing analysis is useless
			return Collections.emptyList();
		}
		// Separate moves that lead to loose (put in finished). These moves do not need to be deepened. Store others in toDeepen
		// We can put 'finished' moves in ended because their order doesn't matter
		final List<M> toDeepen = new ArrayList<>(evaluations.size());
		evaluations.stream().forEach(e -> {
			if (e.isEnd()) {
				ended.add(e);
			} else {
				toDeepen.add(e.getContent());
			}
		});
		return toDeepen;
	}

}
