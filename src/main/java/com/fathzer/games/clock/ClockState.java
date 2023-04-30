package com.fathzer.games.clock;

/** The state of a clock.
 */
public enum ClockState {
	/** The clock has not been started yet.*/
	CREATED,
	/** The clock was started and one player countdown is running.*/
	COUNTING,
	/** The clock was started, but is currently paused. No countdown is running.*/
	PAUSED,
	/** One of the player countdown has ran out of time. An ended clock can't be restarted.*/
	ENDED;
}
