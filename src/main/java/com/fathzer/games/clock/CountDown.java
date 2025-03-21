package com.fathzer.games.clock;

/** The clock count down for a player.
 * <br>In a classical game clock (for example a chess clock), each player have its own count down that stops when he makes a move
 * and restarts when the other player makes a move.
 * <br>The count down should always be created paused.
 */
public interface CountDown {
	/** Starts this count down.
	 * <br>If the count down was not pause, this method does nothing.
	 * @return the time remaining.
	 * @throws IllegalStateException if this is not paused
	 */
	long start();
	
	/** Pauses this count down.
	 * <br>If clock is paused, this method does nothing
	 * @return the time remaining.
	 */
	long pause();

	/** Pauses this count down and returns an updated the count down.
	 * @return the updated count down.
	 * <br><b>Warning</b>: it could be another instance of count down, especially when a move changes the games phase (switches to another clock settings).
	 * @throws IllegalStateException if this is paused
	 */
	CountDown moveDone();
	
	/** Gets the time remaining in this count down. 
	 * @return the time remaining.
	 */
	long getRemainingTime();
	
	/** Gets the number of moves remaining before next phase (before clockSettings change).
	 * @return 0 if there's no more phase, a positive number of moves if there is one.
	 */
	public int getRemainingMovesBeforeNext();
	
	/** Tests whether this count down is paused.
	 * @return true if this count down is paused
	 */
	boolean isPaused();
	
	/** Gets the current settings of this countDown.
	 * <br>Please note the returned settings may change during the game. If the original clock settings had a {@link ClockSettings#getNext()} settings,
	 * this 'next' settings will be returned as soon as it will be activated.
	 * @return the current settings
	 */
	ClockSettings getCurrentSettings();
}
