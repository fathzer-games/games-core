package com.fathzer.games.perft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

public class PerfTResult<M> {
	final AtomicLong nbMovesMade;
	final AtomicLong nbMovesFound;
	final Collection<Divide<M>> divides;
	boolean interrupted = false;
	
	PerfTResult() {
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
	
	public long getNbLeaves() {
		return divides.stream().mapToLong(Divide::getCount).sum();
	}
	
	public long getNbMovesMade() {
		return nbMovesMade.get();
	}

	public long getNbMovesFound() {
		return nbMovesFound.get();
	}


	public boolean isInterrupted() {
		return interrupted;
	}
}
