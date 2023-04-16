package com.fathzer.games.perft;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fathzer.games.GameState;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.util.GameContextExecutor;

/**
 * A <a href="https://www.chessprogramming.org/Perft">perfT</a> implementation.
 * @param <M> Implementation of the Move interface to use
 */
public class PerfT<M> extends GameContextExecutor<M> {
	private Supplier<MoveGenerator<M>> supplier;
	private boolean playLeaves;
	
	public PerfT(Supplier<MoveGenerator<M>> supplier) {
		this.supplier = supplier;
		this.playLeaves = false;
	}
	
	public boolean isPlayLeaves() {
		return playLeaves;
	}

	public void setPlayLeaves(boolean playLeaves) {
		this.playLeaves = playLeaves;
	}

	public PerfTResult<M> divide(final int depth) {
		if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
		}
		final PerfTResult<M> result = new PerfTResult<>();
		final GameState<M> moves = supplier.get().getState();
		result.nbMovesFound.addAndGet(moves.size());
        final IntStream stream = IntStream.range(0, moves.size());
		List<Callable<Divide<M>>> tasks = stream.mapToObj(m -> 
			new Callable<Divide<M>>() {
				@Override
				public Divide<M> call() throws Exception {
					return getPrfT(moves.get(m), depth - 1, result);
				}
			
		}).collect(Collectors.toList());
		try {
			final List<Future<Divide<M>>> results = exec(tasks);
			for (Future<Divide<M>> f : results) {
				result.divides.add(f.get());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			result.interrupted = true;
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
			getMoveGenerator().makeMove(move);
			result.nbMovesMade.incrementAndGet();
			leaves = get(depth, result);
			getMoveGenerator().unmakeMove();
		}
		return new Divide<>(move, leaves);
	}
	
    private long get (final int depth, PerfTResult<M> result) {
		if (isInterrupted()) {
			result.interrupted = true;
			return 1;
		}
    	if (depth==0) {
    		return 1;
    	}
    	final MoveGenerator<M> generator = getMoveGenerator();
		final GameState<M> state = generator.getState();
		result.nbMovesFound.addAndGet(state.size());
		if (depth==1 && !playLeaves) {
			return state.size();
		}
		long count = 0;
		for (int i = 0; i < state.size(); i++) {
            generator.makeMove(state.get(i));
            result.nbMovesMade.incrementAndGet();
            count += get(depth-1, result);
            generator.unmakeMove();
		}
        return count;
    }

	@Override
	public MoveGenerator<M> buildMoveGenerator() {
		return supplier.get();
	}
}
