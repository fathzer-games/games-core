package com.fathzer.games.ai.time;

import com.fathzer.games.clock.CountDownState;

/** The interface of a class able to compute how much time to allocate to next move search.
 * @param <B> The type of the data used by the time manager.
 */
public interface TimeManager<B> {
	/** Gets the time the allocate to next move search.
	 * @param data The data requested to compute the result (usually the game's current state)
	 * @param countDown The of state of the player's count down. 
	 * @return A positive long
	 */
	long getMaxTime(B data, CountDownState countDown);
}
