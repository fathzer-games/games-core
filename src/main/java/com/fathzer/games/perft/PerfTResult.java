package com.fathzer.games.perft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import com.fathzer.games.MoveGenerator;

/** The result of a {@link PerfT} test.
 * @param <M> The type of move generated by the {@link MoveGenerator} used in the test.
 */
public class PerfTResult<M> {
	private final AtomicLong nbMovesMade;
	private final AtomicLong nbMovesFound;
	private final Collection<Divide<M>> divides;
	private boolean interrupted = false;
	
	/** Creates a new empty {@link PerfTResult} instance. */
	public PerfTResult() {
		nbMovesMade = new AtomicLong();
		nbMovesFound = new AtomicLong();
		divides = new ArrayList<>();
	}

	/** Gets the <a href="https://www.chessprogramming.org/Perft#Divide">divides</a> that have been computed.
     * @return A collection of {@link Divide} instances
     */
	public Collection<Divide<M>> getDivides() {
		return divides;
	}
	
	/** Adds a new {@link Divide} to the result.
	 * @param divide The divide to add.
	 */
	public void add(Divide<M> divide) {
		divides.add(divide);
	}
	
	/** Gets the number of leaves found in the test.
	 * <br>This is the sum of the {@link Divide#count} of each {@link Divide} and should be usually compared with the 
	 * expected number of moves returned by {@link PerfTTestData#getExpectedLeaveCount(int)}).
	 * @return The number of leaves found in the test.
	 */
	public long getNbLeaves() {
		return divides.stream().mapToLong(Divide::getNbLeaves).sum();
	}
	
	/**
	 * Gets the number of moves that have been made during the test.
	 * @return a long
	 */
	public long getNbMovesMade() {
		return nbMovesMade.get();
	}

	/** Declares a move made during the test.
	 */
	public void addMoveMade() {
		nbMovesMade.incrementAndGet();
	}

	/** Gets the number of moves that have been found during the test.
	 * @return a long
	 */
	public long getNbMovesFound() {
		return nbMovesFound.get();
	}
	
	/** Declares some moves found during the test.
	 * @param nb the number of moves found to add
	 */
	public void addMovesFound(int nb) {
		nbMovesFound.addAndGet(nb);
	}

	/** Checks whether this test has been interrupted.
	 * @return true if the test has been interrupted, false otherwise.
	 */
	public boolean isInterrupted() {
		return interrupted;
	}

	/** Sets this test interrupted flag.
	 * @param interrupted the new value for the interrupted flag
	 */
	public void setInterrupted(boolean interrupted) {
		this.interrupted = interrupted;
	}
}
