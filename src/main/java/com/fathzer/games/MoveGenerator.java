package com.fathzer.games;

import java.util.List;
import java.util.stream.Collectors;

/** A class able to play moves and to compute the state of a game (the list of possible moves or who has won).
 * @param <M> The class that represents a move.
 */
public interface MoveGenerator<M> {
    /**
     * Plays the given move and modify the state of the game if the move is correct.
     * @param move The move to play
     * @return true if the move is correct and was played, false if it is not correct and has been ignored
     */
	boolean makeMove(M move);
	
    /**
     * Undo the last move and restore the state of the game.
     */
	void unmakeMove();
	
    /**
     * Lists every possible moves of the current player.
     * @param quiesce true if we want only moves that changes a lot the game state. These are the moves used in <a href="https://en.wikipedia.org/wiki/Quiescence_search">quiesce search</a>.
     * If you don't want to implement quiesce for a game, return an empty list when the argument is true.
     * @return a list of moves.
     * <br>Please note this list can:<ul>
     * <li>Be not empty in some end game situations (for example when a chess game ends because of insufficient material).</li>
     * <li>Contain illegal moves, that will return false when passed to {@link #makeMove(Object)}.
     * This allows the implementor to return <a href="https://www.chessprogramming.org/Pseudo-Legal_Move">pseudo-legal moves</a> instead of legal moves which is a classical optimization.</li>
     * <li>Contain null move if this move is legal (typically in <a href="https://en.wikipedia.org/wiki/Reversi">Reversi game</a>, some positions result in the only possible move to be a null move that capture no adverse pawn).</li>
     * </ul>
     * Some ai algorithm, like alpha-beta pruning can be greatly optimized when moves are sorted with the (a priori) best moves first. So implementors should be wise to return sorted lists.
     * @see <a href="https://www.chessprogramming.org/Move_Ordering">Move Ordering on Chess Programming Wiki</a>
     */
	List<M> getMoves(boolean quiesce);
	
	/**
	 * Lists every legal moves of the current player.
	 * <br>The default implementation uses {@link #getMoves(boolean)}, {@link #makeMove(Object)} and {@link #unmakeMove()}
	 * check if moves are valid. The implementor is free to override this method to implement an optimized computation.
	 * @return A move list. Please note that, unlike in {@link #getMoves(boolean)} the order of the moves doesn't matter.
	 */
	default List<M> getLegalMoves() {
		return getMoves(false).stream().filter(m -> {
			final boolean ok = makeMove(m);
			if (ok) {
				unmakeMove();
			}
			return ok;
		}).collect(Collectors.toList());
	}

	/** This method is called before evaluating a position or looking for a previous evaluation in a transposition table.
	 * <br>It allows to deal with position that have end game status caused, not by the position itself, but by the game context (or history).
	 * Typically, in Chess, a position that does not seem to be a draw can be a draw because of repetition.
	 * This method should then return this <i>contextual</i> status.
	 * @return Status.PLAYING, which is the default, if there's nothing particular due to the context or a contextual status.
	 * <br>Please note that it is recommended to return Status.PLAYING for end game positions that not depends on the game history (typically mat positions in Chess),
	 * this will allow the AI to store and recover the position's score in transposition table instead of computing it again and again.
	 */
	default Status getContextualStatus() {
		return Status.PLAYING;
	}
	
	/** This method is called when no valid move were returned by {@link #getMoves(boolean)}, which means the game is ended.
	 * @return The game status that should not be Status.PLAYING because game is ended.
	 */
	Status getEndGameStatus();
}