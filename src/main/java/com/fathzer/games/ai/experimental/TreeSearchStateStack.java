package com.fathzer.games.ai.experimental;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.ai.SearchContext;

public class TreeSearchStateStack<M, B extends MoveGenerator<M>> {
	private final List<TreeSearchState<M>> states;
	private int currentDepth;
	public final SearchContext<M,B> context;
	public final int maxDepth;
	
	public TreeSearchStateStack(SearchContext<M,B> context, int maxDepth) {
		this.context = context;
		this.maxDepth = maxDepth;
		states = new ArrayList<>(maxDepth+1);
		for (int i = 0 ; i<=maxDepth; i++) {
			states.add(new TreeSearchState<>(i, 0, 0));
		}
		currentDepth = maxDepth; 
	}
	
	public TreeSearchState<M> getCurrent() {
		return states.get(currentDepth);
	}

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

	public void unmakeMove() {
		context.unmakeMove();
		currentDepth++;
	}

	public int getCurrentDepth() {
		return currentDepth;
	}

	public SearchContext<M,B> getSearchContext() {
		return context;
	}

	public int getMaxDepth() {
		return maxDepth;
	}
	
	public List<M> getMoveStack() {
		int depth = getCurrent().depth;
		List<M> result = new LinkedList<>();
		for (int i = maxDepth; i > depth; i--) {
			result.add(get(i).lastMove);
		}
		return result;
	}
}
