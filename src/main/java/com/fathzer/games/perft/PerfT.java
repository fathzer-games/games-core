package com.fathzer.games.perft;

import static com.fathzer.games.MoveGenerator.MoveConfidence.*;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;

/** A <a href="https://www.chessprogramming.org/Perft">Perft</a> test.
 * @see PerfTBuilder
 */
public class PerfT<M> implements Callable<PerfTResult<M>> {
	private final boolean playLeaves;
	private final MoveConfidence moveType;
	final MoveGenerator<M> board;
	private final AtomicBoolean started = new AtomicBoolean();
	final int depth;
	private volatile boolean interrupted;
	
	PerfT(MoveGenerator<M> board, int depth, boolean playLeaves, MoveConfidence moveType) {
		if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
		}
		this.board = board;
		this.playLeaves = playLeaves;
		this.moveType = moveType;
		this.depth = depth;
	}
	
	/** {@inheritDoc}
	 * @throws IllegalStateException if this method has already been called
	 */
	@Override
	public PerfTResult<M> call() {
		if (!started.compareAndSet(false, true)) {
			throw new IllegalStateException("This PerfT has already been started");
		}
		return compute();
	}

	PerfTResult<M> compute() {
		final List<M> moves = getMoves(board);
		final PerfTResult<M> result = new PerfTResult<>();
		result.addMovesFound(moves.size());
		for (M move : moves) {
			result.add(getPrfT(move, depth - 1, result));
		}
		return result;
	}
	
	MoveGenerator<M> getBoard() {
		return board;
	}
	
	List<M> getMoves(MoveGenerator<M> moveGenerator) {
		return LEGAL==moveType ? moveGenerator.getLegalMoves() : moveGenerator.getMoves();
	}

	Divide<M> getPrfT(M move, int depth, PerfTResult<M> result) {
		final long leaves;
		if (depth==0 && !playLeaves) {
			leaves = 1;
		} else {
			final MoveGenerator<M> moveGenerator = getBoard();
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
		if (isInterrupted()) {
			result.setInterrupted(true);
			return 1;
		}
    	if (depth==0) {
    		return 1;
    	}
    	final MoveGenerator<M> generator = getBoard();
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
	
	/** Returns true if this PerfT has been interrupted.
	 */
	public boolean isInterrupted() {
		if (interrupted) {
            return true;
		}
		if (Thread.interrupted()) {
			Thread.currentThread().interrupt();
            interrupted = true;
		}
		return interrupted;
	}

	/** Interrupts this PerfT.
	 * <br>If the {@link PerfT#call()} method is currently running, it will quickly stopped and will returned a {@link PerfTResult#isInterrupted()} tagged as interrupted.
	 */
	public void interrupt() {
		interrupted = true;
	}
}