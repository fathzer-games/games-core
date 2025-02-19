package com.fathzer.games.perft;

public class Divide<M> {
	private final M move;
	private final long count;

	public Divide(M move, long count) {
		this.move = move;
		this.count = count;
	}

	public M getMove() {
		return move;
	}

	public long getCount() {
		return count;
	}

	@Override
	public String toString() {
		return move.toString()+": "+count;
	}
}

