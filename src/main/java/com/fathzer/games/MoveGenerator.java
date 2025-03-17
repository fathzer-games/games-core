package com.fathzer.games;

import java.util.List;

import com.fathzer.games.util.exec.Forkable;

/** A class able to play moves and to compute the state of a game (the list of possible moves or who has won).
 * @param <M> The class that represents a move.
 */
public interface MoveGenerator<M> extends Forkable<MoveGenerator<M>> {
	/** The level of confidence of a move.
	 * <br>Moves sent to makeMove can have different origins, a list of legal move, a list of peuso-legal moves or a move with no guarantees of validity at all.
	 * <br>Performing checks on a move can be time consuming, knowing the confidence to have on a move allow the move generator to skip some checks in {@link MoveGenerator#makeMove(Object, MoveConfidence)}
	 * @see MoveGenerator#makeMove(Object, MoveConfidence)
	 */
	enum MoveConfidence {
		/** The move is guaranteed to be legal (returned by getLegalMoves or checked by an external validator).*/
		LEGAL,
		/** The move is pseudo legal (returned by {@link #getMoves()}).*/
		PSEUDO_LEGAL,
		/** The move has not been checked.
		 * <br>typically, such a move is retrieved from a <a href="https://en.wikipedia.org/wiki/Transposition_table">transposition table</a>.
		 * Due to the hash mechanism of the transposition tables, the move can be be a valid move ... but for a different position with the same hash.
		 */
		UNSAFE}
	
	/** Checks whether white player should play.
	 * @return true if white has to play, false if it is black
	 */
	boolean isWhiteToMove();
	
    /**
     * Plays the given move and modify the state of the game if the move is correct.
     * @param move The move to play
     * @param confidence The move confidence (legal, pseudo legal or unsafe).
     * @return true if the move is correct and was played, false if it is not correct and has been ignored
     */
	boolean makeMove(M move, MoveConfidence confidence);
	
    /**
     * Undo the last move and restore the state of the game.
     */
	void unmakeMove();
	
    /**
     * Lists every possible moves of the current player.
     * @return a list of moves.
     * <br>Please note this list can:<ul>
     * <li>Be not empty in some end game situations (for example when a chess game ends because of insufficient material).</li>
     * <li>Contain illegal moves, that will return false when passed to {@link #makeMove(Object, MoveConfidence)}.
     * This allows the implementor to return <a href="https://www.chessprogramming.org/Pseudo-Legal_Move">pseudo-legal moves</a> instead of legal moves which is a classical optimization.</li>
     * <li>Contain null move if this move is legal (typically in <a href="https://en.wikipedia.org/wiki/Reversi">Reversi game</a>, some positions result in the only possible move to be a null move that capture no adverse pawn).</li>
     * </ul>
     * Some ai algorithm, like alpha-beta pruning can be greatly optimized when moves are sorted with the (a priori) best moves first. So implementors should be wise to return sorted lists.
     * @see <a href="https://www.chessprogramming.org/Move_Ordering">Move Ordering on Chess Programming Wiki</a>
     */
	List<M> getMoves();
	
	/**
	 * Lists every legal moves of the current player.
	 * <br>The default implementation uses {@link #getMoves()}, {@link #makeMove(Object, MoveConfidence)} and {@link #unmakeMove()}
	 * check if moves are valid. The implementor is free to override this method to implement an optimized computation.
	 * @return A move list. Please note that, unlike in {@link #getMoves()} the order of the moves doesn't matter.
	 */
	default List<M> getLegalMoves() {
		return getMoves().stream().filter(m -> {
			final boolean ok = makeMove(m, MoveConfidence.PSEUDO_LEGAL);
			if (ok) {
				unmakeMove();
			}
			return ok;
		}).toList();
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
	
	/** This method is called when no valid move were returned by {@link #getMoves()}, which means the game is ended.
	 * @return The game status that should not be Status.PLAYING because game is ended.
	 */
	Status getEndGameStatus();
}