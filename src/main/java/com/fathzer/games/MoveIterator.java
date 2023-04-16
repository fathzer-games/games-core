package com.fathzer.games;

import java.util.Iterator;

class MoveIterator<M> implements Iterator<M> {
	private GameState<M> moves;
	private int next;

	public MoveIterator(GameState<M> list) {
		this.moves = list;
		this.next = 0;
	}

	@Override
	public boolean hasNext() {
		return next<moves.size();
	}

	@Override
	public M next() {
		final M result = moves.get(next);
		next++;
		return result;
	}
}

