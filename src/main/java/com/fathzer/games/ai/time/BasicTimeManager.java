package com.fathzer.games.ai.time;

import com.fathzer.games.clock.CountDownState;

/** A TimeManager that uses a {@link RemainingMoveCountPredictor} to estimate the number of moves to finish the game
 *  and distributes the available time equally between all future moves.
 * @param <B> The type of the data required by the predictor.
 */
public class BasicTimeManager<B> implements TimeManager<B> {
	private final RemainingMoveCountPredictor<B> oracle;
	
	/** Constructor
	 * @param oracle The oracle to use to predict the number of moves to go
	 */
	public BasicTimeManager(RemainingMoveCountPredictor<B> oracle) {
		this.oracle = oracle;
	}

	@Override
	public long getMaxTime(B data, CountDownState countDown) {
		int movesToGo = countDown.getMovesToGo();
		if (movesToGo==0) {
			// Evaluate number of remaining moves
			movesToGo = oracle.getRemainingHalfMoves(data);
			// Convert half moves count to moves count
			movesToGo = (movesToGo+1)/2;
		}
		long remainingMs = countDown.getRemainingMs() + countDown.getIncrementMs()*(movesToGo-1);
		return remainingMs/movesToGo;
	}

}
