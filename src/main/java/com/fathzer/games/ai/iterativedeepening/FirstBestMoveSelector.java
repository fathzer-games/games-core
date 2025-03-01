package com.fathzer.games.ai.iterativedeepening;

import java.util.List;

import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.moveselector.MoveSelector;

/** A {@link MoveSelector} that, in case of ties, selects moves that have already been found as best for lower depths.
 * <br>It starts at the last depth searched in the history. If some of the candidates moves were already in best moves list at this lower depth, it retains only them.
 * It none were in this list, it ignores this depth and keep the whole candidates list.
 * <br>Then it continues untils the first depth in history is reached.
 * @param <M> The type of moves
 */
public class FirstBestMoveSelector<M> extends MoveSelector<M, SearchHistory<M>> {
	
	@Override
	public List<EvaluatedMove<M>> select(SearchHistory<M> history, List<EvaluatedMove<M>> result) {
		return super.select(history, filter(history, result));
	}

	protected List<EvaluatedMove<M>> filter(SearchHistory<M> history, List<EvaluatedMove<M>> bestMoves) {
		for (int i=history.length()-1;i>=0;i--) {
			final List<EvaluatedMove<M>> best = SearchResult.getBestMoves(history.getList(i), history.getSearchParameters());
			final List<M> cut = best.stream().map(EvaluatedMove::getMove).toList();
			bestMoves = getCandidates(bestMoves, cut);
			log(i, cut, bestMoves);
		}
		return bestMoves;
	}
	
	protected void log(int index, List<M> cut, List<EvaluatedMove<M>> result) {
		// Does nothing by default
	}
	
	private List<EvaluatedMove<M>> getCandidates(List<EvaluatedMove<M>> bestMoves, List<M> moves) {
		final List<EvaluatedMove<M>> alreadyBest = bestMoves.stream().filter(em -> moves.contains(em.getMove())).toList();
		return alreadyBest.isEmpty() ? bestMoves : alreadyBest;
	}
}
