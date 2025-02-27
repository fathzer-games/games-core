package com.fathzer.games.util.exec;

/** A task that can be interrupted
 */
public interface Interruptible {
    
    /** Interrupts the current search */
	void interrupt();

    /** Tests if the search was interrupted.
     * @return <code>true</code> if the search was interrupted, <code>false</code> otherwise
     */
	boolean isInterrupted();
}
