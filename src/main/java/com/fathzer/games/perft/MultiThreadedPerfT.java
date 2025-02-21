package com.fathzer.games.perft;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.util.exec.ContextualizedExecutor;

/** A <a href="https://www.chessprogramming.org/Perft">Perft</a> test.
 * @see PerfTBuilder
 */
class MultiThreadedPerfT<M> extends PerfT<M> {
	private final ContextualizedExecutor<MoveGenerator<M>> exec;
	
	MultiThreadedPerfT(ContextualizedExecutor<MoveGenerator<M>> exec, MoveGenerator<M> board, int depth, boolean playLeaves, MoveConfidence moveType) {
		super(board, depth, playLeaves, moveType);
		this.exec = exec;
	}
	
	@Override
	PerfTResult<M> compute() {
		final List<M> moves = getMoves(board);
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
			final List<Future<Divide<M>>> results = exec.invokeAll(tasks, board);
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

	@Override
	MoveGenerator<M> getBoard() {
		return exec.getContext();
	}
}