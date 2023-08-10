package com.fathzer.games.ai;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.exec.ExecutionContext;

public abstract class AbstractAI<M,B extends MoveGenerator<M>> implements AI<M> {
	private final ExecutionContext<M,B> context;
	private final Evaluator<B> evaluator;
	private boolean interrupted;
	private SearchStatistics statistics;
	
	protected AbstractAI(ExecutionContext<M,B> context, Evaluator<B> evaluator) {
		this.context = context;
		this.evaluator = evaluator;
		this.interrupted = false;
		this.statistics = new SearchStatistics();
	}
	
	@Override
    public SearchStatistics getStatistics() {
		return statistics;
	}

	protected B getGamePosition() {
    	return context.getGamePosition();
	}
	
	protected Evaluator<B> getEvaluator() {
		return evaluator;
	}

	@Override
    public SearchResult<M> getBestMoves(SearchParameters params) {
		statistics.clear();
		List<M> moves = getGamePosition().getMoves();
		getStatistics().movesGenerated(moves.size());
		if (getClass().isAssignableFrom(MoveSorter.class)) {
			moves = ((MoveSorter<M>)this).sort(moves);
		}
		return this.getBestMoves(moves, params);
    }

	protected SearchResult<M> getBestMoves(List<M> moves, SearchParameters params, BiFunction<M,Integer, Integer> rootEvaluator) {
		statistics.clear();
        final SearchResult<M> search = new SearchResult<>(params.getSize(), params.getAccuracy());
		final List<Runnable> tasks = moves.stream().map(m -> new Runnable() {
			@Override
			public void run() {
            	final int low = search.getLow();
				final int score = rootEvaluator.apply(m, low);
				if (!isInterrupted()) {
					// Do not return interrupted evaluations, they are false
//if (depth==8) System.out.println(m+" by thread "+Thread.currentThread()+" at depth "+depth+" with low="+low+"=> value="+value+". Interrupted="+isInterrupted());
            		search.add(m, getEvaluator().toEvaluation(score, params.getDepth()));
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
