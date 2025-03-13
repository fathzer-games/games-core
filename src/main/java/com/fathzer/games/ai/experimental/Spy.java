package com.fathzer.games.ai.experimental;

import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.transposition.AlphaBetaState;
import com.fathzer.games.ai.transposition.TranspositionTablePolicy;

/** A spy that can be used to monitor the search process of {@link Negamax3}.
 * <br>It is called by the search process at various points.
 * @param <M> The type of the moves
 * @param <B> The type of the {@link MoveGenerator} to use
 */
public interface Spy<M, B extends MoveGenerator<M>> {
	/** The events that triggers the end of search at a depth. */
	public enum Event {
		/** The search process reached the max depth and computed a new evaluation. */
		EVAL,
		/** The search process has reached a terminal game state. */
		END_GAME,
		/** The search process has exited because there are no more moves to play. */
		EXIT,
		/** The search process has found an evaluation from the transposition table. */
		TT
	}

	/** Called when the search process enters a new depth.
	 * @param state The state of the search process
	 */
	default void enter(TreeSearchStateStack<M,B> state) {}
	
	/** Called when alpha/beta value is found in the transposition table.
	 * @param state The state of the search process
	 * @param abState The alpha/beta state
	 */
	default void alphaBetaFromTT(TreeSearchStateStack<M,B> state, AlphaBetaState<M> abState) {}
	
	/** Called when a state was sent to the transposition table (even if it is rejected by its {@link TranspositionTablePolicy}
	 * @param state The state of the search process
	 * @param abState The alpha/beta state
	 * @param store true if the state is stored, false if it is rejected by the policy
	 */
	default void storeTT(TreeSearchStateStack<M,B> state, AlphaBetaState<M> abState, boolean store) {}
	
	/** Called when a tree cut occurs.
	 * @param state The state of the search process
	 * @param move The move that triggered the cut
	 */
	default void cut(TreeSearchStateStack<M,B> state, M move) {}
	
	/** Called when the search process exits a depth.
	 * @param state The state of the search process
	 * @param evt The event that triggered the exit
	 */
	default void exit(TreeSearchStateStack<M,B> state, Event evt) {}
	
	/** Called when a move is unmade.
	 * @param state The state of the search process
	 * @param moves The list of moves
	 * @param move The move that was unmade
	 */
	default void moveUnmade(TreeSearchStateStack<M, B> state, List<M> moves, M move) {}
	
	/** Called when the moves are computed.
	 * @param searchStack The search stack
	 * @param moves The list of moves
	 */
	default void movesComputed(TreeSearchStateStack<M, B> searchStack, List<M> moves) {}
	
	/** Called when an exception is thrown.
	 * @param searchStack The search stack
	 * @param e The exception
	 */
	default void exception(TreeSearchStateStack<M, B> searchStack, RuntimeException e) {}
}