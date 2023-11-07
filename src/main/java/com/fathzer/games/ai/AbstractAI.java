package com.fathzer.games.ai;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.Status;
import com.fathzer.games.ai.evaluation.Evaluator;
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
		List<M> moves = getGamePosition().getMoves(false);
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
    	final B moveGenerator = getGamePosition();
//System.out.println("Play move "+move+" at depth "+depth+" for "+1);
        if (moveGenerator.makeMove(move, MoveConfidence.UNSAFE)) {
	        getStatistics().movePlayed();
	        final int score = getRootScore(depth, lowestInterestingScore);
	        moveGenerator.unmakeMove();
	        return score;
        } else {
        	return null;
        }
	}
	
	protected abstract int getRootScore(final int depth, int lowestInterestingScore);

	protected SearchResult<M> getBestMoves(List<M> moves, SearchParameters params, BiFunction<M,Integer, Integer> rootEvaluator) {
		statistics.clear();
        final SearchResult<M> search = new SearchResult<>(params.getSize(), params.getAccuracy());
		final List<Runnable> tasks = moves.stream().map(m -> new Runnable() {
			@Override
			public void run() {
            	final int low = search.getLow();
				final Integer score = rootEvaluator.apply(m, low);
				if (!isInterrupted() && score!=null) {
					// Do not return interrupted evaluations, they are false
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
	
	protected int getScore(final Status status, final int depth, int maxDepth) {
		if (Status.DRAW==status) {
			return 0;
		} else {
			//FIXME Maybe there's some games where the player wins if it can't move...
			return -getEvaluator().getWinScore(maxDepth-depth);
		}
	}
}
