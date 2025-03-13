package com.fathzer.games.ai.experimental;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.ai.SearchContext;

/**
 * A stack of {@link TreeSearchState}.
 * <br>It is used to store the search state at each depth.
 * @param <M> The type of the moves
 * @param <B> The type of the {@link MoveGenerator} to use
 */
public class TreeSearchStateStack<M, B extends MoveGenerator<M>> {
	private final List<TreeSearchState<M>> states;
	private int currentDepth;
	public final SearchContext<M,B> context;
	public final int maxDepth;
	
	/**
	 * Creates a new instance.
	 * @param context The search context
	 * @param maxDepth The maximum depth
	 */
	public TreeSearchStateStack(SearchContext<M,B> context, int maxDepth) {
		this.context = context;
		this.maxDepth = maxDepth;
		states = new ArrayList<>(maxDepth+1);
		for (int i = 0 ; i<=maxDepth; i++) {
			states.add(new TreeSearchState<>(i, 0, 0));
		}
		currentDepth = maxDepth; 
	}
	
	/**
	 * Returns the current search state.
	 * @return A {@link TreeSearchState} instance
	 */
	public TreeSearchState<M> getCurrent() {
		return states.get(currentDepth);
	}

	/**
	 * Returns the search state at the given depth.
	 * @param depth The depth
	 * @return A {@link TreeSearchState} instance
	 */
	public TreeSearchState<M> get(int depth) {
		return states.get(depth);
	}

	void init(final TreeSearchState<M> result, int alpha, int beta) {
		result.alphaOrigin = alpha;
		result.alpha = alpha;
		result.betaOrigin = beta;
		result.beta = beta;
		result.bestMove = null;
		result.value = -Integer.MAX_VALUE;
		result.lastMove = null;
	}
	
	/**
	 * Makes a move.
	 * @param move The move to make
	 * @param confidence The confidence of the move
	 * @return true if the move was valid, false otherwise
	 */
	public boolean makeMove(M move, MoveConfidence confidence) {
		final boolean validMove = context.makeMove(move, confidence);
		if (validMove) {
			get(currentDepth).lastMove = move;
			next();
		}
		return validMove;
	}

	private TreeSearchState<M> next() {
		final TreeSearchState<M> current = get(currentDepth);
		currentDepth--;
		final TreeSearchState<M> result = get(currentDepth);
		init(result, -current.beta, -current.alpha);
		return result;
	}

	/**
	 * Unmakes the last move.
	 */
	public void unmakeMove() {
		context.unmakeMove();
		currentDepth++;
	}

	/**
	 * Returns the current search depth.
	 * @return The current depth
	 */
	public int getCurrentDepth() {
		return currentDepth;
	}

	/**
	 * Gets the search context.
	 * @return The search context
	 */
	public SearchContext<M,B> getSearchContext() {
		return context;
	}

	/**
	 * Gets the maximum depth.
	 * @return The maximum depth
	 */
	public int getMaxDepth() {
		return maxDepth;
	}
	
	/**
	 * Gets stack of moves made.
	 * @return The move stack
	 */
	public List<M> getMoveStack() {
		int depth = getCurrent().getDepth();
		List<M> result = new LinkedList<>();
		for (int i = maxDepth; i > depth; i--) {
			result.add(get(i).lastMove);
		}
		return result;
	}
}
