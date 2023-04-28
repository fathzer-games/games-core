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
	private Color playing;
	private ScheduledFuture<?> flagFall;
	private ConcurrentLinkedQueue<Consumer<Status>> listeners;
	private boolean started;
	
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
		this.playing = Color.BLACK;
		this.listeners = new ConcurrentLinkedQueue<>();
	}
	
	/** Adds a listener.
	 * <br>Please note the listener can be called by another thread. 
	 * @param listener a listener that will be informed is a player runs out of time.
	 */
	public void addListener(Consumer<Status> listener) {
		listeners.add(listener);
	}
	
	/** Changes the player that should play when clock starts.
	 * <br>By default, the clock starts with the black player because its the way it works on some chess sites (<a href="https://chess.com">chess.com</a> for example):
	 * The clock starts when white player makes its first move. 
	 * @param startPlayer The color of the player whose countdown will start when the first tap will occur.
	 * @return this clock
	 * @throws IllegalStateException if clock is already started.
	 */
	public Clock withStartingColor(Color startPlayer) {
		if (started) {
			throw new IllegalStateException("Can't change the starting player after clock is started");
		}
		this.playing = startPlayer;
		return this;
	}

	/** Starts the countDown if it was paused or change the player whose countdown.
	 * @return true if the countdown is started. False if it can't be started because a player already ran out of time.
	 */
	public synchronized boolean tap() {
		started = true;
		if (playing==null) {
			return false;
		}
		if (!isPaused()) {
			// Stop the player's count down
			final CountDown playerState = getPlayerState();
			if (playerState.getRemainingTime() <= 0) {
				// Too late, time is up
				debug("Tap is ignored because it was received after clock fires timeup event");
				return false;
			}
			// The time was running, cancel the flagFallTask
			flagFall.cancel(false);
			this.counters[playing.ordinal()] = playerState.moveDone();
			
			// Set the flag for other player
			playing = playing.opposite(); 
		}
		final long remaining = getPlayerState().start();
		flagFall = getScheduler().schedule(()-> {
			synchronized(this) {
				getPlayerState().pause();
				final Status status = Color.WHITE.equals(playing) ? Status.BLACK_WON : Status.WHITE_WON;
				// Mark the game ended
				playing = null;
				listeners.iterator().forEachRemaining(l -> l.accept(status));
			}
		}, remaining, TimeUnit.MILLISECONDS);
		debug("Clock is now counting time for "+ playing);
		return true;
	}
	
	public synchronized boolean pause() {
		if (!isPaused()) {
			if (getPlayerState().pause()<=0) {
				// Too late, time is up
				return false;
			}
			flagFall.cancel(false);
			debug("Clock paused");
		}
		return playing!=null;
	}
	
	public synchronized boolean isPaused() {
		return playing==null || getPlayerState().isPaused();
	}
	
	public synchronized Color getPlaying() {
		return isPaused() ? null : this.playing;
	}
	
	public synchronized long getRemaining(Color color) {
		return getPlayerState(color).getRemainingTime();
	}
	
	protected void debug(String message) {
		// This method does nothing
	}

	public ScheduledExecutorService getScheduler() {
		return TIMER;
	}
	
	private CountDown getPlayerState() {
		return getPlayerState(playing);
	}

	private CountDown getPlayerState(Color color) {
		return this.counters[color.ordinal()];
	}
}
