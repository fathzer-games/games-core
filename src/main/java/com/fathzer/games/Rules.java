package com.fathzer.games;

public interface Rules<T, M> {
	T newGame();
	GameState<M> getState(T board);
}
