package com.fathzer.games.perft;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;

/** A <a href="https://www.chessprogramming.org/Perft">Perft</a> test with each divide computed by a separate thread.
 * @see PerfTBuilder
 */
class MultiThreadedPerfT<M> extends PerfT<M> {
	final ExecutorService exec;
	
	MultiThreadedPerfT(ExecutorService exec, MoveGenerator<M> board, int depth, boolean playLeaves, MoveConfidence moveType) {
		super(board, depth, playLeaves, moveType);
		this.exec = exec;
	}
	
	@Override
	void compute(List<M> moves) {
		final List<Future<Divide<M>>> results = moves.stream().map(m -> exec.submit(getDivideTask(board, m, depth))).toList();
		addDivides(results);
	}
	
	private Callable<Divide<M>> getDivideTask(MoveGenerator<M> board, M move, int depth) {
		return () -> getRootPerfT(board.fork(), move, depth - 1);
	}
	
	void addDivides(List<Future<Divide<M>>> results) {
		try {
			for (Future<Divide<M>> f : results) {
				final Divide<M> divide = f.get();
				if (divide!=null) {
					result.add(divide);
				}
			}
		} catch (InterruptedException e) {
			result.setInterrupted(true);
			interrupt();
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			throw new RuntimeException(e.getCause());
		}
	}
}