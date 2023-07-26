package com.fathzer.games.ai;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.util.Evaluation;

public abstract class AbstractAI<M> implements AI<M> {
	private final ExecutionContext<M> context;
	private boolean interrupted;
	
	protected AbstractAI(ExecutionContext<M> context) {
		this.interrupted = false;
		this.context = context;
	}
	
    protected MoveGenerator<M> getMoveGenerator() {
    	return context.getMoveGenerator();
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
		final List<Runnable> tasks = moves.stream().map(m -> new Runnable() {
			@Override
			public void run() {
//System.out.println(m+" computed on "+exec+" by thread "+Thread.currentThread());
	            	final int value = evaluator.apply(Collections.singleton(m).iterator(), search.getLow());
	            	search.add(m, value);
			}
		}).collect(Collectors.toList());
		context.execute(tasks);
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
	
	@Override
	public boolean isInterrupted() {
		return interrupted;
	}
	
	@Override
	public void interrupt() {
		interrupted = true;
	}
}
