package com.fathzer.games;

import java.util.Iterator;
import java.util.function.IntUnaryOperator;

/** This interface represents the current state of a game.
 * <br>This is basically the list of possible moves and the status of the game.
 * @param <M> The class of the moves
 */
public interface GameState<M> extends Iterable<M> {
	/** Gets the game status, if it's still running, if some player won or if it's a draw.
	 * <br>If the game is running, the number of possible moves can never be 0.
	 * If a player has to pass its turn, he should play a virtual move that does nothing. 
	 * @return The game status
	 */
	Status getStatus();
	
	/** Gets the number of possible moves for next player.
	 * @return a positive or null int. 
	 */
	int size();
	
	/** Gets a possible move by its index. 
	 * @param index The move index
	 * @return a move instance
	 */
	M get(int index);
	
	/** Sorts the move according an evaluation function.
	 * <br>Some ai algorithm, like alpha-beta pruning can be greatly optimized when moves are sorted
	 * using an ordering heuristics to search parts of the tree that are likely to force alpha-beta cutoffs early.
	 * See <a href="http://en.wikipedia.org/wiki/Alpha-beta_pruning#Heuristic_improvements">Alpha-beta pruning on Wikipedia</a>
	 * <br>This method allows to implement a custom sorting algorithm. The default implementation does nothing.
	 * @param evaluator A function that takes the move index as argument and returns the move value (greater is better).
	 */
	default void sort(IntUnaryOperator evaluator) {
		//FIXME, This method signature implies the move representation is an integer!!!
		// Default implementation does nothing
	}
	
	@Override
	default Iterator<M> iterator() {
		return new MoveIterator<>(this);
	}
}
