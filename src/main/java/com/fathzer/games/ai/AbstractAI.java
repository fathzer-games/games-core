package com.fathzer.games.ai;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.fathzer.games.ai.exec.ExecutionContext;

public abstract class AbstractAI<M> implements AI<M> {
	private final ExecutionContext<M> context;
	private boolean interrupted;
	private SearchStatistics statistics;
	
	protected AbstractAI(ExecutionContext<M> context) {
		this.interrupted = false;
		this.context = context;
		this.statistics = new SearchStatistics();
	}
	
    public SearchStatistics getStatistics() {
		return statistics;
	}

	protected GamePosition<M> getGamePosition() {
    	return context.getGamePosition();
	}

	@Override
    public SearchResult<M> getBestMoves(final int depth, int size, int accuracy) {
		List<M> moves = getGamePosition().getMoves();
		getStatistics().movesGenerated(moves.size());
		if (getClass().isAssignableFrom(MoveSorter.class)) {
			moves = ((MoveSorter<M>)this).sort(moves);
		}
		return this.getBestMoves(moves, depth, size, accuracy);
    }

	protected SearchResult<M> getBestMoves(final int depth, List<M> moves, int size, int accuracy, BiFunction<M,Integer, Integer> rootEvaluator) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
        }
        final SearchResult<M> search = new SearchResult<>(size, accuracy);
		final List<Runnable> tasks = moves.stream().map(m -> new Runnable() {
			@Override
			public void run() {
            	final int low = search.getLow();
				final int value = rootEvaluator.apply(m, low);
				if (!isInterrupted()) {
					// Do not return interrupted evaluations, they are false
//if (depth==8) System.out.println(m+" by thread "+Thread.currentThread()+" at depth "+depth+" with low="+low+"=> value="+value+". Interrupted="+isInterrupted());
            		search.add(m, value);
				}
			}
		}).collect(Collectors.toList());
		context.execute(tasks);
        return search;
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
