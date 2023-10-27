package com.fathzer.games;

import java.util.List;

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
     * This allows the implementor to return <a href="https://www.chessprogramming.org/Pseudo-Legal_Move">pseudo-legal moves</a> instead of legal moves which is a classical optimization</li></ul>
     * </ul>
     * Some ai algorithm, like alpha-beta pruning can be greatly optimized when moves are sorted with the (a priori) best moves first. So implementors should be wise to return sorted lists, especially when onlyLegal is false.
     * @see <a href="https://www.chessprogramming.org/Move_Ordering">Move Ordering on Chess Programming Wiki</a>
     */
	List<M> getMoves(boolean quiesce);

	Status isRepetition();
	Status onNoValidMove();
}