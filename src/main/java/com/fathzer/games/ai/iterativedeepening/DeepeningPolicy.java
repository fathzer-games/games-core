package com.fathzer.games.ai.iterativedeepening;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
	
	/** Sets the maximum time to spend in the search.
	 * <br>The default value is Long.MAX_VALUE.
	 * @param maxTime The number of milliseconds to spend in the search
	 */
	public void setMaxTime(long maxTime) {
		this.maxTime = maxTime;
	}

	/** Gets the maximum time to spend in the search.
	 * @return a number of milliseconds to spend in the search
	 */
	public long getMaxTime() {
		return maxTime;
	}
	
	public boolean isDeepenOnForced() {
		return deepenOnForced;
	}

	/** Sets if the search should be deepened when only one move remains to evaluate.
	 * <br>Please note, that if this attribute to false (The default value), searching for two best moves (size=2), and
	 * you having n possible moves and only one that isn't yet evaluate to win or loose, will stop the evaluation
	 * @param deepenOnForced true to force the deepening until max depth or maxtime is reached.
	 */
	public void setDeepenOnForced(boolean deepenOnForced) {
		this.deepenOnForced = deepenOnForced;
	}

	/** Gets the start depth.
	 * @return the start depth, default is min(2,maxDepth).
	 */
	public int getStartDepth() {
		return Math.min(2, getDepth());
	}

	/** Gets next depth.
	 * @param currentDepth currentDepth
	 * @return next depth, a negative value to stop deepening. Default is currentDepth+1 
	 */
	public int getNextDepth(int currentDepth) {
		return currentDepth+1;
	}
	
	/** Guess whether there is enough time to deepen the search.
	 * @param depth the last finished depth
	 * @return true if it remains enough time. The default implementation returns true if less than half of {@link #getMaxTime()} was spent since {@link #start()} was called.
	 */
	public boolean isEnoughTimeToDeepen(int depth) {
		return getSpent()<maxTime/2;
	}

	/** Determines which moves should be deepened.
	 * <br>This method is called every time a search is made except when {@link #isEnoughTimeToDeepen(int)} returns false.
	 * @param <M> The type of moves
	 * @param depth The depth that just finished
	 * @param evaluations The evaluations obtained at this depth that are not move that ends the game (no need to deepen such moves)
	 * @param history The search history, including the result at {@code depth}
	 * @return A list of moves to deepen, an empty list to stop deepening.
	 * <br>The default implementation returns an empty list if attribute is false and it remains only one move in {@code evaluations}
	 * If not, all moves are returned.
	 * <br>By overriding this method, a custom policy can, for instance, decide that result is stable enough to stop deepening before max depth is reached,
	 * or stop deepening some moves that appears too bad.
	 */
	public <M> List<M> getMovesToDeepen(int depth, List<EvaluatedMove<M>> evaluations, SearchHistory<M> history) {
		// Stop deepening if not in deepenOnForced mode and there's only one move to deepen
		return deepenOnForced || evaluations.size()>1 ? evaluations.stream().map(EvaluatedMove::getContent).toList() : Collections.emptyList();
	}

	/** This method is called when a search is interrupted by timeout.
	 * <br>When a timeout occurs during a search at depth n, the results of the search is usually partial (some moves are missing).
	 * <br>This method allows this policy to decide if partial results should be inserted in final results or discarded. For instance, if there's no quiescence
	 * implemented in the search (which is usually a bad idea) merging the partial results of depth n with result of depth n-1 may lead to merge optimistic with
	 * pessimistic results ... which is the guarantee for ... strange results.
	 * <br>This method returns nothing but partial results can be returned in best moves passed in first argument.
	 * <br><br>Default implementation carefully merges the interrupted search result in the search history;
	 * It means it guarantees that non evaluated moves will never become better than an evaluated one.
	 * <br>The classical case is search history contains [(move x,50),(move y, 45),(move z, 44)] and interrupted search is [(move x, 40), (move y, 42)].
	 * Adding [(move z, 44),(move y, 42),(move x,40)] would be a very, very bad idea. In fact, the z score is probably just the result of an alpha/beta cut and may be very, very far
	 * from the exact move's score, making a bad move to seem better than than best moves.
	 * <br>In this case the default implementation will return [(move y,42),(move x, 40),(move z, ?&lt;40)]
	 * <br>More precisely, if all the moves contained in history.cut() are in the interrupted search, the results are merged and 
	 * this method ensures the score of moves absent from the interrupted search will remain out of new history.getCut().
	 * @param <M> The class of moves
	 * @param history The search history that concern previous not interrupted searches.
	 * @param interruptedSearch The result of the interrupted search.
	 * @param interruptionDepth The depth at which interruption occurred.
	 * @return The search result to add in history if the merge can be done. An empty optional if the interrupted search should be discarded. 
	 */
	public <M> Optional<SearchResult<M>> mergeInterrupted(SearchHistory<M> history, SearchResult<M> interruptedSearch, int interruptionDepth) {
		final List<EvaluatedMove<M>> partialList = interruptedSearch.getList();
		if (partialList.isEmpty() || !areMergeable(history.getBestMoves(), partialList)) {
			return Optional.empty();
		}
		final List<EvaluatedMove<M>> historyMoves = history.getList();
		final int previousLow = SearchResult.getLow(historyMoves, getSize(), getAccuracy());
		final boolean trap = partialList.get(partialList.size()-1).getScore()<=previousLow;
		final SearchResult<M> mergedResult = new SearchResult<>(getSize(), getAccuracy());
		if (trap) {
			// Warning, some approximatively scored moves have a better value than some of partialList
			// => Replace all scores with a score lower than the lower score in partialList
		    final int unkownScore = partialList.get(partialList.size()-1).getEvaluation().getScore()-1;
			historyMoves.stream().map(EvaluatedMove::getContent).collect(Collectors.toCollection(ArrayDeque::new)).
			descendingIterator().forEachRemaining(m->mergedResult.update(m, Evaluation.score(unkownScore)));
		} else {
			historyMoves.forEach(em -> mergedResult.add(em.getContent(), em.getEvaluation()));
		}
		partialList.forEach(ev -> mergedResult.update(ev.getContent(), ev.getEvaluation()));
		return Optional.of(mergedResult);
	}
	
	private <M> boolean areMergeable(List<EvaluatedMove<M>> cut, List<EvaluatedMove<M>> partialList) {
		for (EvaluatedMove<M> cutEvalMove : cut) {
			final boolean notFound = partialList.stream().filter(em->em.getContent().equals(cutEvalMove.getContent())).findAny().isEmpty();
			if (notFound) {
				return false;
			}
		}
		return true;
	}
}
