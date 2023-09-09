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
     * Lists every valid moves of the current player.
     * @return a list of moves. Please note this list can be not empty in some draw situations (for example when a chess game ends because of insufficient material).
     * <br>Some ai algorithm, like alpha-beta pruning can be greatly optimized when moves are sorted with the (a priori) best moves first.
     * @see <a href="https://www.chessprogramming.org/Move_Ordering">Move Ordering on Chess Programming Wiki</a>
     */
	List<M> getMoves();
    
    /**
     * Gets the current game status.
     * @return a status
     */
	Status getStatus();
}