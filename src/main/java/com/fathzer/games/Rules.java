package com.fathzer.games;

/** The game's rules.
 * <br>Typically, it is able to create the representation of a new game (for example, a chessboard at the beginning of the game)
 * and compute the {@link GameState} of any valid representation of that game (for example, for any position of pieces on a chessboard).
 * <br>Here <i>valid</i> means, for instance for chess game, with kings on the chessBoard.  
 * @param <T> The class that represents the game. 
 * @param <M> The class that represents a move.
 */
public interface Rules<T, M> {
	/** Creates a new game.*/
	T newGame();
	/** Gets the state of a game.
	 * @param board The representation of the game.
	 * @return the game's state.
	 */
	GameState<M> getState(T board);
}
