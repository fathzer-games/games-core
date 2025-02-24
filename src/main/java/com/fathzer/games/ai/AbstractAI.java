package com.fathzer.games.ai;

import java.util.List;
import java.util.function.BiFunction;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.Status;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.util.exec.ExecutionContext;

public abstract class AbstractAI<M, B extends MoveGenerator<M>> implements AI<M> {
	private final ExecutionContext<SearchContext<M,B>> context;
	private boolean interrupted;
	
	protected AbstractAI(ExecutionContext<SearchContext<M,B>> context) {
		this.context = context;
		this.interrupted = false;
	}
	
	@Override
    public SearchStatistics getStatistics() {
		return context.getContext().getStatistics();
	}

	public SearchContext<M, B> getContext() {
		return context.getContext();
	}

	@Override
    public SearchResult<M> getBestMoves(SearchParameters params) {
		getContext().getStatistics().clear();
		List<M> moves = getContext().getGamePosition().getMoves();
		getStatistics().movesGenerated(moves.size());
		return this.getBestMoves(moves, params);
    }

	@Override
    public SearchResult<M> getBestMoves(List<M> moves, SearchParameters params) {
		return getBestMoves(moves, params, (m,lowestInterestingScore)->rootEvaluation(m,params.getDepth(),lowestInterestingScore));
    }
	
	protected Integer rootEvaluation(M move, final int depth, int lowestInterestingScore) {
    	if (lowestInterestingScore==Integer.MIN_VALUE) {
    		// WARNING: -Integer.MIN_VALUE is equals to ... Integer.MIN_VALUE
    		// So using it as alpha value makes negamax fail 
    		lowestInterestingScore += 1;
    	}
        if (context.getContext().makeMove(move, MoveConfidence.UNSAFE)) {
	        getStatistics().movePlayed();
	        final int score = getRootScore(depth, lowestInterestingScore);
	        context.getContext().unmakeMove();
	        return score;
        } else {
        	return null;
        }
	}
	
	protected abstract int getRootScore(final int depth, int lowestInterestingScore);

	protected SearchResult<M> getBestMoves(List<M> moves, SearchParameters params, BiFunction<M,Integer, Integer> rootEvaluator) {
        final SearchResult<M> search = new SearchResult<>(params.getSize(), params.getAccuracy());
		context.execute(moves.stream().map(m -> getRootEvaluationTask(rootEvaluator, search, m)).toList());
        return search;
    }

	private Runnable getRootEvaluationTask(BiFunction<M, Integer, Integer> rootEvaluator, final SearchResult<M> search, M m) {
		return () -> {
				final Integer score = rootEvaluator.apply(m, search.getLow());
				if (!isInterrupted() && score!=null) {
					// Do not return interrupted evaluations, they are false
            		search.add(m, getContext().getEvaluator().toEvaluation(score));
				}
		};
	}
	
	@Override
	public boolean isInterrupted() {
		return interrupted;
	}
	
	@Override
	public void interrupt() {
		interrupted = true;
	}
	
	protected int getScore(final Evaluator<M,B> evaluator, final Status status, final int depth, int maxDepth) {
		if (Status.DRAW==status) {
			return 0;
		} else {
			//FIXME Maybe there's some games where the player wins if it can't move...
			return -evaluator.getWinScore(maxDepth-depth);
		}
	}
}
