package com.fathzer.games.ai;

import java.util.List;

/** A class that can sort move lists to have best move first.
 * <br>Some ai algorithm, like alpha-beta pruning can be greatly optimized when moves are sorted with the best moves (a priori) first.
 * @param <M> The move class
 * @see <a href="https://www.chessprogramming.org/Move_Ordering">Move Ordering on Chess Programming Wiki</a>
 */
public interface MoveSorter<M> {
	/** Sorts a move list best move first.
	 * <br>The default implementation returns the unsorted list.
	 * @param moves The moves
	 * @return The ordered moves list
	 */
	default List<M> sort(List<M> moves) {
		return moves;
	}
}
