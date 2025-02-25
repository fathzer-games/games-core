package com.fathzer.games.perft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;

/** A <a href="https://www.chessprogramming.org/Perft">Perft</a> test based on the fork/join multi-threaded framework.
 * @see PerfTBuilder
 */
class ForkJoinPerfT<M> extends MultiThreadedPerfT<M> {
	@SuppressWarnings({"serial", "java:S1948"})
	private class DivideJob extends RecursiveTask<Divide<M>> {
		M move;
		CountTask task;

		DivideJob(M move, CountTask task) {
			this.move = move;
			this.task = task;
		}

		@Override
		public Divide<M> compute() {
			return new Divide<>(move, task.compute());
		}
	}

	@SuppressWarnings({"serial", "java:S1948"})
	private class CountTask extends RecursiveTask<Long> {
		private final MoveGenerator<M> board;
		private final int depth;
		private final PerfTResult<M> perftResult;

		CountTask(MoveGenerator<M> board, int depth, PerfTResult<M> result) {
			this.board = board;
			this.depth = depth;
			this.perftResult = result;
		}

		@Override
		protected Long compute() {
			if (isInterrupted() || depth==0) {
				return 1L;
			}
			final List<M> moves = getMoves(board);
			perftResult.addMovesFound(moves.size());
			if (depth==1 && !playLeaves) {
				return (long)moves.size();
			}
			return depth >= 4 ? forkedCount(moves) : count(moves);
		}

		protected long forkedCount(final List<M> moves) {
			final Collection<CountTask> tasks = new ArrayList<>(moves.size());
			for (M move : moves) {
				if (board.makeMove(move, moveType)) {
					perftResult.addMoveMade();
					tasks.add(new CountTask(board.fork(), depth-1, perftResult));
					board.unmakeMove();
				}
			}
			tasks.forEach(RecursiveTask::fork);
			return tasks.stream().mapToLong(ForkJoinTask::join).sum();
		}

		protected long count(final List<M> moves) {
			long count = 0;
			for (M move : moves) {
				if (board.makeMove(move, moveType)) {
					perftResult.addMoveMade();
					count += new CountTask(board, depth-1, perftResult).compute();
					board.unmakeMove();
				}
			}
			return count;
		}
	}

	ForkJoinPerfT(ForkJoinPool exec, MoveGenerator<M> board, int depth, boolean playLeaves, MoveConfidence moveType) {
		super(exec, board, depth, playLeaves, moveType);
	}
	
	@Override
	void compute(List<M> moves) {
		final List<DivideJob> jobs = moves.stream().map(move -> getDivideJob(move, result)).filter(Objects::nonNull).toList();
		jobs.stream().forEach(((ForkJoinPool)exec)::submit);
		addDivides(jobs.stream().map(job -> (Future<Divide<M>>) job).toList());
	}
	
	private DivideJob getDivideJob(M move, PerfTResult<M> result) {
		if (!board.makeMove(move, moveType)) {
			return null;
		}
		result.addMoveMade();
		final DivideJob job = new DivideJob(move, new CountTask(board.fork(), depth - 1, result));
		board.unmakeMove();
		return job;
	}
}