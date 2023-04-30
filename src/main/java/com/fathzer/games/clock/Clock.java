package com.fathzer.games.clock;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.fathzer.games.Color;
import com.fathzer.games.Status;
import static com.fathzer.games.clock.ClockState.*;

/** A clock.
 * <br>A clock is basically two countdowns, one for each player. When a player has to play, his time decreases.
 * When the player makes his move, he taps on the clock. This action stops his countdown and starts his opponent's.
 * <br>Additionally, the game can be paused then both countdowns are stopped.
 * <br>One can register with the clock to be informed when time is up for a player.
 * <br>This class is thread safe.
 */
public class Clock {
	private static final ThreadFactory FACTORY = r -> {
	    Thread t = Executors.defaultThreadFactory().newThread(r);
	    t.setDaemon(true);
	    return t;
    };
	private static final ScheduledExecutorService TIMER = new ScheduledThreadPoolExecutor(1, FACTORY);
	
	static {
		((ScheduledThreadPoolExecutor)TIMER).setRemoveOnCancelPolicy(true);
	}
	
	private CountDown[] counters;
	private ScheduledFuture<?> flagFall;
	private volatile Color playing;
	private ConcurrentLinkedQueue<Consumer<Status>> statusListeners;
	private ConcurrentLinkedQueue<Consumer<ClockEvent>> clockListeners;
	private volatile ClockState state;
	
	/** Constructor.
	 * @param settings The settings to apply to both players
	 */
	public Clock(ClockSettings settings) {
		this(settings, settings);
	}
	
	/** Constructor.
	 * @param whiteSettings The settings for white player
	 * @param blackSettings The settings for black player
	 */
	public Clock(ClockSettings whiteSettings, ClockSettings blackSettings) {
		this.counters = new CountDown[2];
		this.counters[Color.WHITE.ordinal()] = whiteSettings.buildCountDown();
		this.counters[Color.BLACK.ordinal()] = blackSettings.buildCountDown();
		this.playing = Color.WHITE;
		this.statusListeners = new ConcurrentLinkedQueue<>();
		this.clockListeners = new ConcurrentLinkedQueue<>();
		this.state = CREATED;
	}
	
	/** Adds a game status change listener.
	 * <br>Please note the listener can be called by another thread. 
	 * @param listener a listener that will be informed when a player runs out of time.
	 */
	public void addStatusListener(Consumer<Status> listener) {
		statusListeners.add(listener);
	}
	
	/** Adds a clock state change listener.
	 * <br>Please note the listener can be called by another thread. 
	 * @param listener a listener that will be informed when clock state changes.
	 * @see ClockEvent
	 */
	public void addClockListener(Consumer<ClockEvent> listener) {
		clockListeners.add(listener);
	}
	
	/** Changes the player that should play when clock starts.
	 * <br>By default, the clock starts with the white player because its the way it usually works. But you could want white first move to starts the black count down. 
	 * @param startPlayer The color of the player whose countdown will start when the first tap will occur.
	 * @return this clock
	 * @throws IllegalStateException if clock is already started.
	 */
	public Clock withStartingColor(Color startPlayer) {
		if (state!=CREATED) {
			throw new IllegalStateException("Can't change the starting player after clock is started");
		}
		this.playing = startPlayer;
		return this;
	}
	
	/** Starts the countDown if it was paused or change the player whose countdown.
	 * @return true if the countdown is started. False if it can't be started because a player already ran out of time.
	 */
	public synchronized boolean tap() {
		if (ENDED==state) {
			return false;
		}
		if (COUNTING==state) {
			// Stop the player's count down
			final CountDown playerState = getPlayerCountdown();
			if (playerState.getRemainingTime() <= 0) {
				// Too late, time is up
				return false;
			}
			// The time was running, cancel the flagFallTask
			flagFall.cancel(false);
			this.counters[playing.ordinal()] = playerState.moveDone();
			
			// Set the flag for other player
			playing = playing.opposite(); 
		}
		final long remaining = getPlayerCountdown().start();
		flagFall = getScheduler().schedule(()-> {
			synchronized(this) {
				getPlayerCountdown().pause();
				final Status status = Color.WHITE.equals(playing) ? Status.BLACK_WON : Status.WHITE_WON;
				setState(ENDED);
				statusListeners.forEach(l -> l.accept(status));
			}
		}, remaining, TimeUnit.MILLISECONDS);
		setState(COUNTING);
		return true;
	}
	
	/** Pauses the countdown.
	 * @return false if time was already up or true if the method succeeded or clock was not counting.
	 */
	public synchronized boolean pause() {
		if (state==COUNTING) {
			if (getPlayerCountdown().pause()<=0) {
				// Too late, time is up
				return false;
			}
			flagFall.cancel(false);
			setState(PAUSED);
		}
		return ENDED!=state;
	}
	
	/** Gets the state of this clock (paused, counting, ...).
	 * @return a ClockState. 
	 */
	public synchronized ClockState getState() {
		return state;
	}
	
	/** Gets the currently playing color.
	 * <br>If the clock is not started, paused or ended, the player that would play is the clock restarts is returned.
	 * @return a Color.
	 */
	public synchronized Color getPlaying() {
		return this.playing;
	}
	
	/** Gets the remaining time for a player in ms.
	 * @param color The player's color
	 * @return a long
	 */
	public synchronized long getRemaining(Color color) {
		return getPlayerCountdown(color).getRemainingTime();
	}
	
	/** Gets this clock scheduler.
	 * <br>It could be used to schedule some clock related tasks (for instance, to update clock GUI in GUI).
	 * Please note that tasks scheduled must be short to keep clock accuracy safe.
	 * <br>You can also override this method to implement your own scheduler (for instance for testing).
	 * @return The clock's scheduler.
	 */
	public ScheduledExecutorService getScheduler() {
		return TIMER;
	}
	
	private CountDown getPlayerCountdown() {
		return getPlayerCountdown(playing);
	}

	private CountDown getPlayerCountdown(Color color) {
		return this.counters[color.ordinal()];
	}
	
	private synchronized void setState(ClockState next) {
		final ClockState old = state;
		state = next;
		if (old!=next || old==COUNTING) {
			this.clockListeners.forEach(l -> l.accept(new ClockEvent(this, old, next)));
		}
	}
}
