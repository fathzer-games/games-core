package com.fathzer.games.perft;

import static com.fathzer.games.MoveGenerator.MoveConfidence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.util.exec.ContextualizedExecutor;

public class PerfT<M> {
	private boolean playLeaves;
	private boolean interrupted;
	private ContextualizedExecutor<MoveGenerator<M>> exec;
	private MoveConfidence moveType = PSEUDO_LEGAL;
	
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
	 * <br>If <i>playLeaves</i> is false, the tested move generator is requested for legal moves to prevent erroneous results.
	 * Indeed, every non legal leave move returned by the pseudo-legal move generator would not be tested and would be counted as a legal move.
	 */
	public void setPlayLeaves(boolean playLeaves) {
		this.playLeaves = playLeaves;
		if (!playLeaves) {
			moveType = LEGAL;
		}
	}
	
	/** Sets this PerfT to get legal or <a href="https://www.chessprogramming.org/Pseudo-Legal_Move">pseudo legal</a> moves from the move generator.
	 * <br>By default PerfT plays pseudo legal moves.
	 * @param legal true to use legal moves, false to use pseudo-legal moves.
	 * <br>When <i>legal</i> is false, <i>playLeaves</i> is automatically set to true (because not doing this would result in wrong leaves count)
	 */
	public void setLegalMoves(boolean legal) {
		moveType = legal ? LEGAL : PSEUDO_LEGAL;
		if (!legal) {
			playLeaves = true;
		}
	}
	
	public PerfTResult<M> divide(final int depth, Supplier<MoveGenerator<M>> generator) {
		if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
		}
		final List<M> moves = getMoves(generator.get());
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
	
	private List<M> getMoves(MoveGenerator<M> moveGenerator) {
		return LEGAL==moveType ? moveGenerator.getLegalMoves() : moveGenerator.getMoves(false);
	}

	private Divide<M> getPrfT(M move, int depth, PerfTResult<M> result) {
		final long leaves;
		if (depth==0 && !playLeaves) {
			leaves = 1;
		} else {
			final MoveGenerator<M> moveGenerator = exec.getContext();
			if (moveGenerator.makeMove(move, moveType)) {
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
		final List<M> moves = getMoves(generator);
		result.addMovesFound(moves.size());
		if (depth==1 && !playLeaves) {
			return moves.size();
		}
		long count = 0;
		for (M move : moves) {
            if (generator.makeMove(move, moveType)) {
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