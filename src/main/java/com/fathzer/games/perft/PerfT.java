package com.fathzer.games.perft;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.util.ContextualizedExecutor;

public class PerfT<M> {
	private boolean playLeaves;
	private boolean interrupted;
	private ContextualizedExecutor<MoveGenerator<M>> exec;
	
	public PerfT(ContextualizedExecutor<MoveGenerator<M>> exec) {
		this.exec = exec;
		this.playLeaves = true;
	}
	
	public boolean isPlayLeaves() {
		return playLeaves;
	}

	/** Sets this PerfT to play the moves corresponding to tree leaves or not.
	 * <br>The default setting is to play the leave moves.
	 * @param playLeaves true to play the leave moves false to not play them.
	 * <br>Warning, if the tested move generator returns <a href="https://www.chessprogramming.org/Pseudo-Legal_Move">pseudo legal</a> moves, setting <i>playLeaves</i> to false leads to erroneous results.
	 * Indeed, every non legal leave move returned by the move generator will not be tested and will be counted as a legal move.
	 */
	public void setPlayLeaves(boolean playLeaves) {
		this.playLeaves = playLeaves;
	}
	
	public PerfTResult<M> divide(final int depth, Supplier<MoveGenerator<M>> generator) {
		if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
		}
		final List<M> moves = generator.get().getMoves(false);
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
			if (moveGenerator.makeMove(move, MoveConfidence.PSEUDO_LEGAL)) {
				result.addMoveMade();
				leaves = get(depth, result);
				moveGenerator.unmakeMove();
			} else {
				leaves = 0;
			}
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
		final List<M> moves = generator.getMoves(false);
		result.addMovesFound(moves.size());
		if (depth==1 && !playLeaves) {
			return moves.size();
		}
		long count = 0;
		for (M move : moves) {
            if (generator.makeMove(move, MoveConfidence.PSEUDO_LEGAL)) {
	            result.addMoveMade();
	            count += get(depth-1, result);
	            generator.unmakeMove();
            }
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