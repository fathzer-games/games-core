package com.fathzer.games;

import java.util.List;

/** A class able to play moves and to compute the state of a game (the list of possible moves or who has won).
 * @param <M> The class that represents a move.
 */
public interface MoveGenerator<M> {
    /**
     * Play the given move and modify the state of the game.
     * @param move The move to play
     */
	void makeMove(M move);
	
    /**
     * Undo the last move and restore the state of the game.
     */
	void unmakeMove();
	
    /**
     * Lists every valid moves of the current player.
	 * <br>Some ai algorithm, like alpha-beta pruning can be greatly optimized when moves are sorted with the best moves (a priori) first.
     * @return a list of moves. Please note this list may not be empty in some draw situations (for example when a chess game ends because of insufficient material).
     */
	List<M> getMoves();
    
    /**
     * Gets the current game status.
     * @return a status
     */
	Status getStatus();
}