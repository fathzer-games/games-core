package com.fathzer.games.perft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

public class PerfTResult<M> {
	private final AtomicLong nbMovesMade;
	private final AtomicLong nbMovesFound;
	private final Collection<Divide<M>> divides;
	private boolean interrupted = false;
	
	public PerfTResult() {
		nbMovesMade = new AtomicLong();
		nbMovesFound = new AtomicLong();
		divides = new ArrayList<>();
	}

	public PerfTResult(long nbMovesMade, long nbMovesFound, Collection<Divide<M>> divides, boolean interrupted) {
		this.nbMovesMade = new AtomicLong(nbMovesMade);
		this.nbMovesFound = new AtomicLong(nbMovesFound);
		this.divides = divides;
		this.interrupted = interrupted;
	}

	public Collection<Divide<M>> getDivides() {
		return divides;
	}
	
	public void add(Divide<M> divide) {
		divides.add(divide);
	}
	
	public long getNbLeaves() {
		return divides.stream().mapToLong(Divide::getCount).sum();
	}
	
	public long getNbMovesMade() {
		return nbMovesMade.get();
	}

	public void addMoveMade() {
		nbMovesMade.incrementAndGet();
	}

	public long getNbMovesFound() {
		return nbMovesFound.get();
	}
	
	public void addMovesFound(int nb) {
		nbMovesFound.addAndGet(nb);
	}

	public boolean isInterrupted() {
		return interrupted;
	}

	public void setInterrupted(boolean interrupted) {
		this.interrupted = interrupted;
	}
}
