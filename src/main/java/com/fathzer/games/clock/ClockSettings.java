package com.fathzer.games.clock;

/** The settings for a clock.
 * <br>This class supports <a href="https://en.wikipedia.org/wiki/Chess_clock">the Fisher increment and Bronstein delay</a>.
 * <br>It also supports change of ClockSetting after an amount of plies (for instance 30 minutes with no increment for the first 20 plies, then 5 minutes with 5 second increment until the end of the game).
 */
public class ClockSettings {
	private int initialTime;
	private int increment = 0;
	private int movesNumberBeforeIncrement = 0;
	private boolean canAccumulate = false;
	private int movesNumberBeforeNext = Integer.MAX_VALUE;
	private int maxRemainingKept = 0;
	private ClockSettings next = null;
	
	/** Constructor.
	 * @param initialTime The initial time in seconds.
	 * <br>No increment is defined nor next settings.
	 */
	public ClockSettings(int initialTime) {
		super();
		withInitialTime(initialTime);
	}

	/** Sets the initial time allowed to players.
	 * @param initialTime The initial time in seconds.
	 * @return this
	 * @throws IllegalArgumentException if initialTime is &lt; 0
	 */
	public ClockSettings withInitialTime(int initialTime) {
		if (initialTime<0) {
			throw new IllegalArgumentException("Initial time must be >= 0");
		}
		this.initialTime = initialTime;
		return this;
	}

	/** Set the increment given when a player makes a move (or a fixed number of moves).
	 * @param increment The increment of time in seconds
	 * @param movesNumberBeforeIncrement Number of moves required before increment is given
	 * @param canAccumulate true if remaining amount of time after applying increment can exceed the remaining amount after the previous increment (or the game start).
	 * <br>In other words, true means "Fisher increment" and false means "Bronstein delay".
	 * @return this
	 * @throws IllegalArgumentException if movesNumberBeforeIncrement is &lt;= 0 and increment != 0
	 */
	public ClockSettings withIncrement(int increment, int movesNumberBeforeIncrement, boolean canAccumulate) {
		if (movesNumberBeforeIncrement<=0 && increment!=0) {
			throw new IllegalArgumentException("movesNumberBeforeIncrement must be strictly positive");
		}
		this.increment = increment;
		this.movesNumberBeforeIncrement = movesNumberBeforeIncrement;
		this.canAccumulate = canAccumulate;
		return this;
	}

	/** Set next settings.
	 * <br>Settings can be chained. For instance a settings of 30 minutes with no increment for the first 20 plies, can be chained with an additional time of 5 minutes with 5 second increment until the end of the game.
	 * @param movesNumberBeforeNext The number of plies to play in this settings before switching to next settings
	 * @param maxRemainingKept The maximum of remaining time (in seconds) that should be added to next settings initial time
	 * @param next The additional settings
	 * @return this
	 * @throws IllegalArgumentException if movesNumberBeforeNext is &lt;= 0 or next is null but movesNumberBeforeNext is not Integer.MAX_VALUE, or next!=null but movesNumberBeforeNext is Integer.MAX_VALUE.
	 */
	public ClockSettings withNext(int movesNumberBeforeNext, int maxRemainingKept, ClockSettings next) {
		if (movesNumberBeforeNext<=0) {
			throw new IllegalArgumentException("Can't set a non strictly positive maximum number of moves in a settings");
		}
		if (next==null && movesNumberBeforeNext!=Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Can't set a maximum number of moves in this settings without a next clock setting");
		} else if (next != null && movesNumberBeforeNext==Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Can't set next settings with an infinite maximum number of moves in this settings");
		} else if (next != null && maxRemainingKept<0) {
			throw new IllegalArgumentException("Max remaing time kept should not be negative");
		}
		this.movesNumberBeforeNext = movesNumberBeforeNext;
		this.maxRemainingKept = maxRemainingKept;
		this.next = next;
		return this;
	}

	/** Gets the initial in seconds.
	 * @return a positive or null int
	 */
	public int getInitialTime() {
		return initialTime;
	}

	/** Gets the increment in seconds.
	 * @return an int
	 */
	public int getIncrement() {
		return increment;
	}

	/** Gets the number of moves to play before a time increment is granted.
	 * @return a strictly positive int
	 */
	public int getMovesNumberBeforeIncrement() {
		return movesNumberBeforeIncrement;
	}

	/** Tests whether the total amount of time after adding the increment can exceed the amount remaining when last increment was applied.
	 * @return a boolean (true for Fisher increment, false for Bronstein delay).
	 */
	public boolean isCanAccumulate() {
		return canAccumulate;
	}

	/** Gets the number of moves for which this settings applies before using next one.
	 * @return a strictly positive int
	 */
	public int getMovesNumberBeforeNext() {
		return movesNumberBeforeNext;
	}
	
	/** Gets the maximum of remaining time (in seconds) that be added to next setting's initial time when it become active.
	 * @return a positive or null int
	 */
	public int getMaxRemainingKept() {
		return maxRemainingKept;
	}

	/** Gets next settings to apply when this will expire.
	 * @return a ClockSetting or null if this settings never expires
	 */
	public ClockSettings getNext() {
		return next;
	}
	
	/** Builds a countdown that supports this setting.
	 * @return a new ClockState
	 */
	protected CountDown buildCountDown() {
		return new DefaultCountDown(this);
	}
}
