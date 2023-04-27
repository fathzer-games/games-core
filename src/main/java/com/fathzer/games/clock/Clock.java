package com.fathzer.games.clock;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.fathzer.games.Color;

/** A clock.
 * <br>This class is thread safe.
 */
public class Clock {
	private static final ThreadFactory FACTORY = r -> {
	    Thread t = Executors.defaultThreadFactory().newThread(r);
	    t.setDaemon(true);
	    return t;
    };
	protected static final ScheduledThreadPoolExecutor TIMER = new ScheduledThreadPoolExecutor(1, FACTORY);
	
	static {
		TIMER.setRemoveOnCancelPolicy(true);
	}
	
	private CountDown[] counters;
	private Color playing;
	private ScheduledFuture<?> flagFall;
	private Consumer<Clock> timeUp;
	
	public Clock(ClockSettings settings, Consumer<Clock> timeUp) {
		this(settings, settings, timeUp);
	}
	
	public Clock(ClockSettings whiteSettings, ClockSettings blackSettings, Consumer<Clock> timeUp) {
		this.counters = new CountDown[2];
		this.counters[Color.WHITE.ordinal()] = whiteSettings.buildClockState();
		this.counters[Color.BLACK.ordinal()] = blackSettings.buildClockState();
		this.playing = Color.BLACK;
		this.timeUp = timeUp;
	}
	
	public Clock withStartingColor(Color startPlayer) {
		this.playing = startPlayer;
		return this;
	}

	public synchronized boolean isPaused() {
		return playing==null || getPlayerState().isPaused();
	}
	
	public synchronized Color getPlaying() {
		return isPaused() ? null : this.playing;
	}
	
	protected void debug(String message) {
		// This method does nothing
		//TODO Remove when things are ok
		System.out.println(System.currentTimeMillis()+": "+message);
	}

	public synchronized boolean tap() {
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
		flagFall = TIMER.schedule(()-> {
			synchronized(this) {
				try {
					timeUp.accept(this);
				} finally {
					getPlayerState().pause();
					// Mark the game ended
					playing = null;
				}
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
	
	private CountDown getPlayerState() {
		return getPlayerState(playing);
	}

	private CountDown getPlayerState(Color color) {
		return this.counters[color.ordinal()];
	}
	
	public synchronized long getRemaining(Color color) {
		return getPlayerState(color).getRemainingTime();
	}
}
