package com.fathzer.games.clock;

/** The event fired when clock's state changes. 
 */
public class ClockEvent {
	private final Clock clock;
	private final ClockState previousState;
	private final ClockState newState;
	
	ClockEvent(Clock clock, ClockState previousState, ClockState newState) {
		super();
		this.clock = clock;
		this.previousState = previousState;
		this.newState = newState;
	}

	/** The clock that fired the event.
	 * @return a Clock
	 */
	public Clock getClock() {
		return clock;
	}

	/** The previous state of the clock.
	 * @return A clock state.
	 */
	public ClockState getPreviousState() {
		return previousState;
	}

	/** The current state of the clock.
	 * <br>Please note that having previous and current state equals to {@link ClockState#COUNTING} is not a bug.
	 * This particular event is fired every time a player makes a move: The state is changed from counting one player's time to counting other player's time.
	 * @return A clock state.
	 */
	public ClockState getNewState() {
		return newState;
	}
}
