package com.fathzer.games.ai;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.util.ContextualizedExecutor;
import com.fathzer.games.util.Evaluation;

public abstract class AbstractMPAI<M> implements AI<M> {
	private final Supplier<MoveGenerator<M>> moveGeneratorBuilder;
	protected final ContextualizedExecutor<MoveGenerator<M>> exec;
	private final MoveGenerator<M> globalMoveGenerator;
	private boolean interrupted;
	
	protected AbstractMPAI(Supplier<MoveGenerator<M>> moveGeneratorBuilder, ContextualizedExecutor<MoveGenerator<M>> exec) {
		this.moveGeneratorBuilder = moveGeneratorBuilder;
		this.exec = exec;
		this.interrupted = false;
		this.globalMoveGenerator = moveGeneratorBuilder.get();
	}
	
    protected MoveGenerator<M> getMoveGenerator() {
		final MoveGenerator<M> result = exec.getContext();
		if (result==null) {
			return globalMoveGenerator;
		}
		return result;
	}
	
    /**
     * Evaluates the state of the game <strong>for the current player</strong> after a move.
     * <br>The greatest the value is, the better the position of the current player is.
     * <br>This value should always be less that the one returned by {@link #getWinScore(int)} 
     * @return The evaluation of the position for the current player
     * @see #getWinScore(int)
     */
	public abstract int evaluate();
	
	@Override
    public List<Evaluation<M>> getBestMoves(final int depth, int size, int accuracy) {
        final List<M> moves = getMoveGenerator().getMoves();
		return this.getBestMoves(depth, moves, size, accuracy);
    }

	protected List<Evaluation<M>> getBestMoves(final int depth, List<M> moves, int size, int accuracy, BiFunction<Iterator<M>,Integer, Integer> evaluator) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
        }
        final FixedNumberSearch<M> search = new FixedNumberSearch<>(size, accuracy);
		final List<Callable<Void>> tasks = moves.stream().map(m ->
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
//System.out.println(m+" computed on "+exec+" by thread "+Thread.currentThread());
	            	final int value = evaluator.apply(Collections.singleton(m).iterator(), search.getLow());
	            	search.add(m, value);
					return null;
				}
			}
		).collect(Collectors.toList());
		try {
			final List<Future<Void>> futures = exec.invokeAll(tasks, moveGeneratorBuilder);
			exec.checkExceptions(futures);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
        return search.getResult();
    }
	
    /** Gets the score obtained for a win after nbMoves moves.
     * <br>The default value is Short.MAX_VALUE - nbMoves*10
     * @param nbMoves The number of moves needed to win.
     * @return an int
     */
	public int getWinScore(int nbMoves) {
		return Short.MAX_VALUE-nbMoves*10;
	}
	
	protected void log(M move, int depth) {
//		for (int i = 0; i < depth; i++) {
//			System.out.print("  ");
//		}
//		System.out.println(move);
	}
	protected void log(int score, int depth) {
//		for (int i = 0; i < depth; i++) {
//			System.out.print("  ");
//		}
//		System.out.println(score);
	}
	
	public boolean isInterrupted() {
		return interrupted;
	}
	
	public void interrupt() {
		interrupted = true;
	}
}
