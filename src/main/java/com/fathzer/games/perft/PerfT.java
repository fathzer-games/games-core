package com.fathzer.games.perft;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.util.ContextualizedExecutor;

public class PerfT<M> {
	private boolean playLeaves;
	private boolean interrupted;
	private ContextualizedExecutor<MoveGenerator<M>> exec;
	
	public PerfT(ContextualizedExecutor<MoveGenerator<M>> exec) {
		this.exec = exec;
		this.playLeaves = false;
	}
	
	public boolean isPlayLeaves() {
		return playLeaves;
	}

	public void setPlayLeaves(boolean playLeaves) {
		this.playLeaves = playLeaves;
	}
	
	public PerfTResult<M> divide(final int depth, Supplier<MoveGenerator<M>> generator) {
		if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
		}
		final List<M> moves = generator.get().getMoves();
		final PerfTResult<M> result = new PerfTResult<>();
		result.addMovesFound(moves.size());
		final List<Callable<Divide<M>>> tasks = new ArrayList<>(moves.size());
		for (M move : moves) {
			tasks.add(new Callable<Divide<M>>() {
				@Override
				public Divide<M> call() throws Exception {
					return getPrfT(move, depth - 1, result);
				}
			});
		}
		try {
			final List<Future<Divide<M>>> results = exec.invokeAll(tasks, generator);
			for (Future<Divide<M>> f : results) {
				result.add(f.get());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			result.setInterrupted(true);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	private Divide<M> getPrfT(M move, int depth, PerfTResult<M> result) {
		final long leaves;
		if (depth==0 && !playLeaves) {
			leaves = 1;
		} else {
			final MoveGenerator<M> moveGenerator = exec.getContext();
			moveGenerator.makeMove(move);
			result.addMoveMade();
			leaves = get(depth, result);
			moveGenerator.unmakeMove();
		}
		return new Divide<>(move, leaves);
	}
	
    private long get (final int depth, PerfTResult<M> result) {
		if (interrupted) {
			result.setInterrupted(true);
			return 1;
		}
    	if (depth==0) {
    		return 1;
    	}
    	final MoveGenerator<M> generator = exec.getContext();
		final List<M> state = generator.getMoves();
		result.addMovesFound(state.size());
		if (depth==1 && !playLeaves) {
			return state.size();
		}
		long count = 0;
		for (M move : state) {
            generator.makeMove(move);
            result.addMoveMade();
            count += get(depth-1, result);
            generator.unmakeMove();
		}
        return count;
    }
	
	public boolean isInterrupted() {
		return interrupted;
	}

	public void interrupt() {
		interrupted = true;
	}
}