package com.fathzer.games.perft;

import static com.fathzer.games.MoveGenerator.MoveConfidence.*;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.util.UncheckedException;

/** A <a href="https://www.chessprogramming.org/Perft">Perft</a> test.
 * @param <M> The type of moves
 * @see PerfTBuilder
 */
public class PerfT<M> {
	final boolean playLeaves;
	final MoveConfidence moveType;
	final MoveGenerator<M> board;
	private final AtomicBoolean started = new AtomicBoolean();
	final int depth;
	final PerfTResult<M> result;
	
	PerfT(MoveGenerator<M> board, int depth, boolean playLeaves, MoveConfidence moveType) {
		if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
		}
		if (board==null) {
			throw new IllegalArgumentException("Board cannot be null");
		}
		this.board = board;
		this.playLeaves = playLeaves;
		this.moveType = moveType;
		this.depth = depth;
		this.result = new PerfTResult<>();
	}
	
	/** Gets the result of the PerfT.
	 * @return The result of the PerfT
	 * @throws IllegalStateException if this method has already been called
	 * @throws UncheckedException if an execution error occurs
	 */
	public PerfTResult<M> get() {
		if (!started.compareAndSet(false, true)) {
			throw new IllegalStateException("This PerfT has already been started");
		}
		try {
			final List<M> moves = getMoves(board);
			result.addMovesFound(moves.size());
			compute(moves);
			return this.result;
		} catch (UncheckedException e) {
			throw e;
		} catch (Exception e) {
			throw new UncheckedException(e);
		}
	}

	void compute(List<M> moves) {
		for (M move : moves) {
			final Divide<M> divide = getRootPerfT(board, move, depth - 1);
			if (divide!=null) {
				result.add(divide);
			}
		}
	}
	
	List<M> getMoves(MoveGenerator<M> moveGenerator) {
		return LEGAL==moveType ? moveGenerator.getLegalMoves() : moveGenerator.getMoves();
	}

	Divide<M> getRootPerfT(MoveGenerator<M> moveGenerator, M move, int depth) {
		final long leaves;
		if (depth==0 && !playLeaves) {
			leaves = 1;
		} else {
			if (moveGenerator.makeMove(move, moveType)) {
				result.addMoveMade();
				leaves = new PerfTTask(moveGenerator, depth).call();
				moveGenerator.unmakeMove();
			} else {
				return null;
			}
		}
		return new Divide<>(move, leaves);
	}
	
	class PerfTTask implements Callable<Long> {
		private final MoveGenerator<M> generator;
		private int depth;

		public PerfTTask(MoveGenerator<M> generator, int depth) {
			this.generator = generator;
			this.depth = depth;
		}

		@Override
		public Long call() {
			if (isInterrupted()) {
				result.setInterrupted(true);
				return 1L;
			}
	    	if (depth==0) {
	    		return 1L;
	    	}
			final List<M> moves = getMoves(generator);
			result.addMovesFound(moves.size());
			if (depth==1 && !playLeaves) {
				return (long)moves.size();
			}
	        return process(moves);
		}
		
		protected Long process(List<M> moves) {
			long count = 0;
			for (M move : moves) {
	            if (generator.makeMove(move, moveType)) {
		            result.addMoveMade();
		            count += goDeeper();
		            generator.unmakeMove();
	            }
			}
            return count;
         }
		
		protected Long goDeeper() {
			depth--;
			long nbLeaves = call();
			depth++;
			return nbLeaves;
		}
	}

	/** Checks if this PerfT has been interrupted.
	 * @return true if this PerfT has been interrupted
	 */
	public boolean isInterrupted() {
		if (result.isInterrupted()) {
            return true;
		}
		if (Thread.interrupted()) {
			Thread.currentThread().interrupt();
            result.setInterrupted(true);
		}
		return result.isInterrupted();
	}

	/** Interrupts this PerfT.
	 * <br>If the {@link PerfT#get()} method is currently running, it will quickly stopped and will returned a {@link PerfTResult#isInterrupted()} tagged as interrupted.
	 */
	public void interrupt() {
		result.setInterrupted(true);
	}
}