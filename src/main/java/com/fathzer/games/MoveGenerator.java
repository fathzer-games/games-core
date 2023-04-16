package com.fathzer.games;

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
     * List the current game state (including every valid moves for the current player).
     * <br><br>Improvement of the alpha beta pruning can be achieved without 
     * sacrificing accuracy, by using ordering heuristics to search parts 
     * of the tree that are likely to force alpha-beta cutoffs early.
     * <br><a href="http://en.wikipedia.org/wiki/Alpha-beta_pruning#Heuristic_improvements">Alpha-beta pruning on Wikipedia</a>
     * @return The list of the current player possible moves
     */
	GameState<M> getState();
}