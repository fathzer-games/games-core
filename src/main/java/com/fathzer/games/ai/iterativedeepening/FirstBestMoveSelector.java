package com.fathzer.games.ai.iterativedeepening;

import java.util.List;

import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.moveselector.MoveSelector;

/** A {@link MoveSelector} that, in case of ties, selects moves that have already been found as best for lower depths.
 * <br>see {@link #filter(SearchHistory, List)} to have a description of the selection process.
 * @param <M> The type of moves
 */
public class FirstBestMoveSelector<M> extends MoveSelector<M, SearchHistory<M>> {
	
	@Override
	public List<EvaluatedMove<M>> select(SearchHistory<M> history, List<EvaluatedMove<M>> result) {
		return super.select(history, filter(history, result));
	}

	/** Filters the best moves.
	 * <br>It starts at the last depth searched in the history. If some of the best moves were already in the best moves list at this depth, it retains only them.
	 * <br>If none were in this list, it ignores this depth and keep the whole best moves list.
	 * <br>Then it continues until the first depth in history is reached or only one move remains in best moves list.
	 * @param history The search history
	 * @param bestMoves The best moves
	 * @return The filtered best moves
	 */
	protected List<EvaluatedMove<M>> filter(SearchHistory<M> history, List<EvaluatedMove<M>> bestMoves) {
		for (int i=history.length()-1;i>=0;i--) {
			if (bestMoves.size()==1) {
				break;
			}
			final List<EvaluatedMove<M>> depthList = history.getList(i);
			final List<EvaluatedMove<M>> best = depthList.subList(0, history.getSearchParameters().getBestMovesCount(depthList));
			final List<M> cut = best.stream().map(EvaluatedMove::getMove).toList();
			bestMoves = filter(bestMoves, cut);
			log(history,i, cut, bestMoves);
		}
		return bestMoves;
	}
	
	/** Logs the selection process at a specified index of the SearchHistory.
	 * <br>Does nothing by default.
	 * @param history The search history
	 * @param index The depth index in the search history (0 is the last depth searched)
	 * @param cut The best moves at the index in search history
	 * @param result The moves after filtering with this history index
	 */
	protected void log(SearchHistory<M> history, int index, List<M> cut, List<EvaluatedMove<M>> result) {
		// Does nothing by default
	}
	
	private List<EvaluatedMove<M>> filter(List<EvaluatedMove<M>> bestMoves, List<M> moves) {
		final List<EvaluatedMove<M>> alreadyBest = bestMoves.stream().filter(em -> moves.contains(em.getMove())).toList();
		return alreadyBest.isEmpty() ? bestMoves : alreadyBest;
	}
}
