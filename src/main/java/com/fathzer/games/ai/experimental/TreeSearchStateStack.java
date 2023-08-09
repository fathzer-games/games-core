package com.fathzer.games.ai.experimental;

import java.util.ArrayList;
import java.util.List;

import com.fathzer.games.MoveGenerator;

public class TreeSearchStateStack<M, B extends MoveGenerator<M>> {
	private final List<TreeSearchState<M>> states;
	private int currentDepth;
	public final B position;
	public final int maxDepth;
	
	public TreeSearchStateStack(B position, int maxDepth) {
		this.position = position;
		this.maxDepth = maxDepth;
		states = new ArrayList<>(maxDepth+1);
		int who = maxDepth%2==0 ? 1 : -1;
		for (int i = 0 ; i<=maxDepth; i++) {
			states.add(new TreeSearchState<>(i, 0, 0, who));
			who = -who;
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
	
	public void makeMove(M move) {
		position.makeMove(move);
		get(currentDepth).lastMove = move;
		next();
	}

	private TreeSearchState<M> next() {
		final TreeSearchState<M> current = get(currentDepth);
		currentDepth--;
		final TreeSearchState<M> result = get(currentDepth);
		init(result, -current.beta, -current.alpha);
		return result;
	}

	public void unmakeMove() {
		position.unmakeMove();
		currentDepth++;
	}

	public int getCurrentDepth() {
		return currentDepth;
	}

	public B getPosition() {
		return position;
	}

	public int getMaxDepth() {
		return maxDepth;
	}
}
