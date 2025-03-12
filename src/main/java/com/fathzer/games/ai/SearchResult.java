package com.fathzer.games.ai;

import java.util.LinkedList;
import java.util.List;

import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation;
import com.fathzer.games.util.SortedUtils;

/** The result of a best move search.
 */
public final class SearchResult<M> {
	private final LinkedList<EvaluatedMove<M>> result;
	private final SearchParameters params;

	/**
	 * Constructor
	 * @param params The search parameters.
	 */
	public SearchResult(SearchParameters params) {
		this.params = params;
		this.result = new LinkedList<>();
	}
	
	/** Gets the search parameters.
	 * @return The search parameters.
	 */
	public SearchParameters getSearchParameters() {
		return params;
	}
	
	synchronized int getLow() {
		return params.getLowerBound(result);
	}
	
	/** Adds a new move evaluation.
	 * @param move The move
	 * @param value The evaluation of the move
	 */
	public synchronized void add(M move, Evaluation value) {
		SortedUtils.insert(this.result, new EvaluatedMove<M>(move, value));
	}
	
	/** Updates the evaluation of a move.
	 * @param move The move (if the move was not already in this search result, it will be added)
	 * @param value The evaluation
	 */
	public synchronized void update(M move, Evaluation value) {
		final int index = getIndex(move);
		if (index>=0) {
			result.remove(index);
		}
		add(move, value);
	}
	
	synchronized int getIndex(M move) {
		int index = 0;
		for (EvaluatedMove<M> ev : result) {
			if (ev.getMove().equals(move)) {
				return index;
			} else {
				index++;
			}
		}
		return -1;
	}
	
	/** Gets the sorted (best first) list of moves evaluation, truncated to the number of moves requested in this instance constructor.
	 * @return The sorted (best first) list of better moves
     * <br>Please note the list may have more than size elements in case of equivalent moves or almost equivalent moves.
     * It can also have less than size elements if there's less than size legal moves or search was interrupted before it finished. 
	 */
	public synchronized List<EvaluatedMove<M>> getCut() {
		return result.subList(0, params.getAccurateMovesCount(result));
	}
	
	/** Gets the list of moves evaluation.
	 * @return The sorted (best first) list  of all valid moves
     * <br>Please note the list may contain upper bounded evaluation (moves we determine they are not good enough to be selected in {@link #getCut()}).
     * <br>Please note this list may not contains all valid moves if search was interrupted before it finished.
	 */
	public List<EvaluatedMove<M>> getList() {
		return result;
	}
}