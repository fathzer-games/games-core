package com.fathzer.games.movelibrary;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.fathzer.games.ai.evaluation.EvaluatedMove;

/** A class that can choose a move from a database, without any tree search.
 * <br>The typical usage is for <a href="https://en.wikipedia.org/wiki/Chess_opening_book_(computers)">Opening books</a> or <a href="https://en.wikipedia.org/wiki/Endgame_tablebase">End game tablebases</a>
 * @param <M> The type of moves
 * @param <B> The type of the keys that allow to retrieve the moves from the library (typically, the game board)
 */
public interface MoveLibrary<M, B> extends Function<B, Optional<EvaluatedMove<M>>> {
	/** Gets the moves for a position.
	 * @param board a position
	 * @return The moves available for the position in the library and their evaluations, or an empty list if it can't find any move.
	 */
	List<EvaluatedMove<M>> getMoves(B board);
	
	/**
	 * Gets the move to play for a position.
	 * @param board a position
	 * @return an empty Optional if it can't find a move for the position, or the proposed move and its evaluation if one was found.
	 */
	@Override
	Optional<EvaluatedMove<M>> apply(B board);

	/**
	 * This method is called when a new game is started.
	 * <br>This allows to reset the library state. Typically, a library may stop searching after a number of moves are played.
	 * Calling this method should restart search.
	 */
	default void newGame() {
		// Does nothing by default
	}
}
