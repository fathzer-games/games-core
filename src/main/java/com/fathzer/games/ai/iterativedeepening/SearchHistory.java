package com.fathzer.games.ai.iterativedeepening;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.moveselector.MoveSelector;

/** A history of search results in an iterative deepening search.
 * @param <M> The type of moves
 * @see IterativeDeepeningEngine
 */
public class SearchHistory<M> {
	private final SearchParameters params;
	private final List<List<EvaluatedMove<M>>> results;
	private final List<Integer> depths;
	
	/** Constructor
	 * <br>The parameters should be the one used in {@link SearchParameters#SearchParameters(int, int)} constructor.
	 * @param params The search parameters.
	 */
	public SearchHistory(SearchParameters params) {
		if (params==null) {
			throw new IllegalArgumentException();
		}
		this.params = params;
		this.results = new ArrayList<>();
		this.depths = new ArrayList<>();
	}
	
	/** Adds a new search result at a specified depth.
	 * @param result The search result
	 * @param depth The depth at which the result was obtained
	 * @throws IllegalArgumentException if the depth is negative or &lt;= the last depth added
	 */
	public void add(List<EvaluatedMove<M>> result, int depth) {
		if (depth < 0 || (!isEmpty() && depth<=depths.get(depths.size()-1))) {
			throw new IllegalArgumentException();
		}
		results.add(result);
		depths.add(depth);
	}

	/** Gets the search parameters used in this history
	 * @return the search parameters
	 */
	public SearchParameters getSearchParameters() {
		return this.params;
	}

	/** Returns the number of results added with {@link #add(List, int)} method
	 * @return a positive integer
	 */
	public int length() {
		return results.size();
	}
	
	/** Checks whether this history is empty
	 * @return true if this history contains no result ({@link #add(List, int)} was never called or called with empty lists).
	 * <br><b>WARNING:</b>A history with an empty move list is not empty. 
	 */
	public boolean isEmpty() {
		return getLastList().isEmpty();
	}

	/** Returns the depth of a result added with {@link #add(List, int)} method
	 * @param index The index of the result
	 * @return a positive integer
	 */
	public int getDepth(int index) {
		return depths.get(index);
	}

	/** Returns the list of moves of a result added with {@link #add(List, int)} method
	 * @param index The index of the result
	 * @return a list of moves
	 */
	public List<EvaluatedMove<M>> getList(int index) {
		return results.get(index);
	}

	/** Returns the accurate moves of a result added with {@link #add(List, int)} method.
	 * @param index The index of the result
	 * @return a list of moves restricted to the size and accuracy of this history (its length is the result of {@link SearchParameters#getAccurateMovesCount(List)})
	 */
	public List<EvaluatedMove<M>> getAccurateMoves(int index) {
		return results.get(index).subList(0, params.getAccurateMovesCount(results.get(index)));
	}

	/** Gets the depth of the last list of moves added to this history
	 * @return a positive integer (0 if no results has been added to this history)
	 */
	public int getLastDepth() {
		return results.isEmpty() ? 0 : depths.get(results.size()-1);
	}

	/** Gets the last list of moves added to this history
	 * @return a list of moves, or and empty list if this history is empty
	 */
	public List<EvaluatedMove<M>> getLastList() {
		return results.isEmpty() ? Collections.emptyList() : results.get(results.size()-1);
	}
	
	/** Gets the last list of accurate moves added to this history
	 * @return a list of moves (restricted to the search parameters of this history), or an empty list if this history is empty
	 * @see #getAccurateMoves(int)
	 */
	public List<EvaluatedMove<M>> getAccurateMoves() {
		return results.isEmpty() ? Collections.emptyList() : getAccurateMoves(length()-1);
	}

	/** Gets the best move according to the given selector
	 * <br>It calls {@link MoveSelector#get(D, List)} with this history as the first argument and {@link #getAccurateMoves()} as second to choose the move
	 * @param selector The selector to use
	 * @return the best move, or null if {@link #getLastList()} returns an empty list or the selector returns no move.
	 */
	public EvaluatedMove<M> getBestMove(MoveSelector<M, SearchHistory<M>> selector) {
		final List<EvaluatedMove<M>> list = getLastList();
		if (list.isEmpty()) {
			return null;
		}
		final List<EvaluatedMove<M>> bestMoves = list.subList(0, params.getBestMovesCount(list));
		return selector.get(this, bestMoves).orElse(null);
	}
}