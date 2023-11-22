package com.fathzer.games.ai.experimental;

import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.AlphaBetaState;

public interface Spy<M, B extends MoveGenerator<M>> {
	public enum Event {EVAL, END_GAME, EXIT, TT}

	default void enter(TreeSearchStateStack<M,B> state) {}
	default void alphaBetaFromTT(TreeSearchStateStack<M,B> state, AlphaBetaState<M> abState) {}
	default void storeTT(TreeSearchStateStack<M,B> state, AlphaBetaState<M> abState, boolean store) {}
	default void cut(TreeSearchStateStack<M,B> state, M move) {}
	default void exit(TreeSearchStateStack<M,B> state, Event evt) {}
	default void moveUnmade(TreeSearchStateStack<M, B> state, List<M> moves, M move) {}
	default void movesComputed(TreeSearchStateStack<M, B> searchStack, List<M> moves) {}
	default void exception(TreeSearchStateStack<M, B> searchStack, RuntimeException e) {}
}