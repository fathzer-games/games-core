package com.fathzer.games.ai.iterativedeepening;

import java.util.ArrayList;
import java.util.List;

import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.moveselector.MoveSelector;

/** A history of search results in an iterative deepening search.
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
	 */
	public void add(List<EvaluatedMove<M>> result, int depth) {
		if (!isEmpty() && depth<depths.get(depths.size()-1)) {
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
	 * @return true if this history is empty
	 */
	public boolean isEmpty() {
		return results.isEmpty();
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

	/** Returns the best moves of a result added with {@link #add(List, int)} method.
	 * <br>This move is computed by {@link SearchResult#getBestMoves(List, SearchParameters)}
	 * @param index The index of the result
	 * @return a list of moves restricted to the size and accuracy of this history
	 */
	public List<EvaluatedMove<M>> getBestMoves(int index) {
		return SearchResult.getBestMoves(results.get(index), params);
	}

	/** Gets the depth of the last list of moves added to this history
	 * @return a positive integer (0 if no results has been added to this history)
	 */
	public int getLastDepth() {
		return isEmpty() ? 0 : depths.get(results.size()-1);
	}

	/** Gets the last list of moves added to this history
	 * @return a list of moves, or null if this history is empty
	 */
	public List<EvaluatedMove<M>> getLastList() {
		return isEmpty() ? null : results.get(results.size()-1);
	}
	
	/** Gets the last list of best moves added to this history
	 * @return a list of moves (restricted to the search parameters of this history), or null if this history is empty
	 * @see SearchResult#getBestMoves(List, SearchParameters)
	 */
	public List<EvaluatedMove<M>> getBestMoves() {
		final List<EvaluatedMove<M>> last = getLastList();
		return last==null ? null : SearchResult.getBestMoves(last, params);
	}

	/** Gets the best move according to the given selector
	 * <br>It calls {@link MoveSelector#get(D, List)} with this history as the first argument and {@link #getBestMoves()} as second to choose the move
	 * @param selector The selector to use
	 * @return the best move, or null if {@link #getBestMoves()} returns null or the selector returns no move.
	 */
	public EvaluatedMove<M> getBestMove(MoveSelector<M, SearchHistory<M>> selector) {
		final List<EvaluatedMove<M>> bestMoves = getBestMoves();
		return bestMoves==null ? null : selector.get(this, bestMoves).orElse(null);
	}
}