package com.fathzer.games.clock;

/** The settings for a clock.
 * <br>This class supports <a href="">the Fisher increment and Bronstein delay</a>.
 * <br>It also supports change of ClockSetting after an amount of plies (for instance 30 minutes with no increment for the first 20 plies, then 5 minutes with 5 second increment until the end of the game).
 */
public class ClockSettings {
	private int initialTime = 180;
	private int increment = 2;
	private int movesNumberBeforeIncrement = 1;
	private boolean canAccumulate = true;
	private int movesNumberBeforeNext = Integer.MAX_VALUE;
	private ClockSettings next = null;
	
	/** Constructor.
	 * <br>The clock is set to 3mn + 2s increment.
	 */
	public ClockSettings() {
		super();
	}

	/** Sets the initial time allowed to players.
	 * @param initialTime The initial time in seconds.
	 * @throws IllegalArgumentException if initialTime is &lt;= 0
	 */
	public void setInitialTime(int initialTime) {
		if (initialTime<=0) {
			throw new IllegalArgumentException("Initial time must be strictly positive");
		}
		this.initialTime = initialTime;
	}

	/** Set the increment given when a player makes a move (or a fixed number of moves).
	 * @param increment The increment of time in seconds
	 * @param movesNumberBeforeIncrement Number of moves required before increment is given
	 * @param canAccumulate true if remaining amount of time after applying increment can exceed the remaining amount after the previous increment (or the game start).
	 * <br>In other words, true means "Fisher increment" and false means "Bronstein delay".  
	 * @throws IllegalArgumentException if movesNumberBeforeIncrement is &lt;= 0
	 */
	public void setIncrement(int increment, int movesNumberBeforeIncrement, boolean canAccumulate) {
		if (movesNumberBeforeIncrement<=0) {
			throw new IllegalArgumentException("movesNumberBeforeIncrement must be strictly positive");
		}
		this.increment = increment;
		this.movesNumberBeforeIncrement = movesNumberBeforeIncrement;
		this.canAccumulate = canAccumulate;
	}

	/** Set next settings.
	 * <br>Settings can be chained. For instance a settings of 30 minutes with no increment for the first 20 plies, can be chained with an additional time of 5 minutes with 5 second increment until the end of the game.
	 * @param movesNumberBeforeNext The number of plies to play in this settings before switching to next setting
	 * @param next The additional setting
	 * @throws IllegalArgumentException if movesNumberBeforeNext is &lt;= 0 or next is null but movesNumberBeforeNext is not Integer.MAX_VALUE, or next!=null but movesNumberBeforeNext is Integer.MAX_VALUE.
	 */
	public void setNext(int movesNumberBeforeNext, ClockSettings next) {
		if (movesNumberBeforeNext<=0) {
			throw new IllegalArgumentException("Can't set a non strictly positive maximum number of moves in a settings");
		}
		if (next==null && movesNumberBeforeNext!=Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Can't set a maximum number of moves in this settings without a next clock setting");
		} else if (next != null && movesNumberBeforeNext==Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Can't set next settings with an infinite maximum number of moves in this settings");
		}
		this.movesNumberBeforeNext = movesNumberBeforeNext;
		this.next = next;
	}

	public int getInitialTime() {
		return initialTime;
	}

	public int getIncrement() {
		return increment;
	}

	public int getMovesNumberBeforeIncrement() {
		return movesNumberBeforeIncrement;
	}

	public boolean isCanAccumulate() {
		return canAccumulate;
	}

	public int getMovesNumberBeforeNext() {
		return movesNumberBeforeNext;
	}

	public ClockSettings getNext() {
		return next;
	}
}
