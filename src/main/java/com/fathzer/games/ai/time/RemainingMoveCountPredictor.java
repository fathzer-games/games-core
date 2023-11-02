package com.fathzer.games.ai.time;

/** An oracle that predicts how long a game will be.
 * @param <B> The data requested by the oracle. 
 */
public interface RemainingMoveCountPredictor<B> {
	/** Gets an evaluation of remaining half moves.
	 * @param data The data the oracle needs (typically, current game's position).
	 * @return an prediction of the number of half moves to reach end of game
	 */
	int getRemainingHalfMoves(B data);
}
