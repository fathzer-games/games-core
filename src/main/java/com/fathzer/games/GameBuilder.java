package com.fathzer.games;

/** A class that can create a new game.
 * <br>Typically, it is able to create the representation of a new game (for example, a chess board at the beginning of the game)
 * @param <T> The class that represents the game. 
 */
@FunctionalInterface
public interface GameBuilder<T> {
	/** Creates a new game.*/
	T newGame();
}
