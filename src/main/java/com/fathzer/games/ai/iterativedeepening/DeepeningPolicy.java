package com.fathzer.games.ai.iterativedeepening;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation;

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
		this.deepenOnForced = false;
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
		if (evaluations.isEmpty() || evaluations.get(0).isEnd() || !isEnoughTimeToDeepen(depth)) {
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
	 * is the guarantee for ... strange results.
	 * <br>This method returns nothing but partial results can be returned in best moves passed in first argument.
	 * <br><br>Default implementation carefully merges the results; It means it guarantees that non evaluated moves will never become better than an evaluated one.
	 * <br>The classical case is bestMoves contains [(move x,50),(move y, 45),(move z, 44)] and partialList [(move x, 40), (move y, 42)].
	 * Returning [(move z, 44),(move y, 42),(move x,40)] would be a very, very bad idea. In fact, the z score is probably just the result of an alpha/beta cut and may be very, very far
	 * from the exact move's score, making a bad move to seem better than than best moves.
	 * <br>In this case the default implementation will return [(move y,42),(move x, 40),(move z, ?&lt;40)]
	 * <br>More precisely, if all the moves contained in bestMoves.cut() are in partialList, the results are merged and 
	 * this method ensures the score of moves absent from partialList will remain out of bestMoves.getCut().
	 * @param <M> The class of moves
	 * @param bestMoves The best moves obtained by previous not interrupted search
	 * @param bestMovesDepth The depth at which the bestMoves where obtained
	 * @param partialList The best moves obtained by interrupted search, sorted best first.
	 * @param interruptionDepth The depth at which interruption occurred.
	 */
	public <M> void mergeInterrupted(SearchResult<M> bestMoves, int bestMovesDepth, List<EvaluatedMove<M>> partialList, int interruptionDepth) {
		if (partialList.isEmpty()) {
			return;
		}
		if (areMergeable(bestMoves, partialList)) {
			final int previousLow = bestMoves.getLow();
			final boolean trap = partialList.get(partialList.size()-1).getScore()<=previousLow;
			if (trap) {
				// Warning, some approximatively scored moves have a better value than some of partialList
				// => Replace all scores with a score lower than the lower score in partialList
			    final int unkownScore = partialList.get(partialList.size()-1).getEvaluation().getScore()-1;
				bestMoves.getList().stream().map(EvaluatedMove::getContent).collect(Collectors.toCollection(ArrayDeque::new)).
				descendingIterator().forEachRemaining(m->bestMoves.update(m, Evaluation.score(unkownScore)));
			}
			for (EvaluatedMove<M> ev : partialList) {
				bestMoves.update(ev.getContent(), ev.getEvaluation());
			}
		}
	}
	
	private <M> boolean areMergeable(SearchResult<M> bestMoves, List<EvaluatedMove<M>> partialList) {
		final List<EvaluatedMove<M>> cut = bestMoves.getCut();
		for (EvaluatedMove<M> cutEvalMove : cut) {
			final boolean notFound = partialList.stream().filter(em->em.getContent().equals(cutEvalMove.getContent())).findAny().isEmpty();
			if (notFound) {
				return false;
			}
		}
		return true;
	}
}
