package com.fathzer.games.ai.iterativedeepening;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;

/** A policy that manages how to deepen the search.
 * <br>Typically, it decides at which depth to start, what increment to add at each step, and if we should end prematurely. 
 */
public class DeepeningPolicy extends SearchParameters {
	private long maxTime;
	private long start;
	private boolean deepenOnForced;
	
	public DeepeningPolicy(int maxDepth) {
		super(maxDepth);
		this.maxTime = Long.MAX_VALUE;
		this.start = -1;
		this.deepenOnForced = true;
	}
	
	public void start() {
		this.start = System.currentTimeMillis();
	}
	
	public long getSpent() {
		if (start<0) {
			throw new IllegalStateException("Not yet started");
		}
		return System.currentTimeMillis()-start;
	}
	
	public void setMaxTime(long maxTime) {
		this.maxTime = maxTime;
	}

	public long getMaxTime() {
		return maxTime;
	}
	
	public boolean isDeepenOnForced() {
		return deepenOnForced;
	}

	public void setDeepenOnForced(boolean deepenOnForced) {
		this.deepenOnForced = deepenOnForced;
	}

	/** Gets the start depth.
	 * @return the start depth, default is min(2,maxDepth).
	 */
	public int getStartDepth() {
		return Math.min(2, getDepth());
	}

	/** Get next depth.
	 * @param currentDepth currentDepth
	 * @return next depth, a negative value to stop deepening. Default is currentDepth+1 
	 */
	public int getNextDepth(int currentDepth) {
		return currentDepth+1;
	}
	
	public boolean isEnoughTimeToDeepen(int depth) {
		return getSpent()>maxTime/2;
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
	public <M> List<M> getMovesToDeepen(int depth, List<EvaluatedMove<M>> evaluations, List<EvaluatedMove<M>> ended) {
		if (evaluations.get(0).isEnd() || !isEnoughTimeToDeepen(depth)) {
			// if best move is a win/loose, or we have not enough time to complete analysis, continuing analysis is useless.
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

	/** This method is called when a search is interrupted by timeout.
	 * <br>When a timeout occurs during a search at depth n, the results of the search is usually partial (some moves are missing).
	 * <br>This method allows this policy to decide if partial results should be inserted in final results or discarded. For instance, if there's no quiescence
	 * implemented in the search (which is a bad idea) merging the partial results of depth n with result of depth n-1 may lead to merge optimistic with pessimistic results ... which
	 * is the guarantee for ... strange results
	 * <br>This method returns nothing but partial results can be returned in best moves passed in first argument.
	 * <br>Default implementation merges the results   
	 * @param <M> The class of moves
	 * @param bestMoves The best moves obtained by previous not interrupted search
	 * @param bestMovesDepth The depth at which the bestMoves where obtained
	 * @param partialList The best moves obtained by interrupted search.
	 * @param interruptionDepth The depth at which interruption occurred.
	 */
	public <M> void mergeInterrupted(SearchResult<M> bestMoves, int bestMovesDepth, List<EvaluatedMove<M>> partialList, int interruptionDepth) {
		for (EvaluatedMove<M> ev : partialList) {
			bestMoves.update(ev.getContent(), ev.getEvaluation());
		}
	}
}
