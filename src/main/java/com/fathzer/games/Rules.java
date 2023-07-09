package com.fathzer.games;

/** The game's rules.
 * //TODO Change comment
 * <br>Typically, it is able to create the representation of a new game (for example, a chessboard at the beginning of the game)
 * and compute the {@link GameState} of any valid representation of that game (for example, for any position of pieces on a chessboard).
 * <br>Here <i>valid</i> means, for instance for chess game, with kings on the chessBoard.  
 * @param <T> The class that represents the game. 
 */
public interface Rules<T> {
	/** Creates a new game.*/
	T newGame();
}
