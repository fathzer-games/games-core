package com.fathzer.games.ai;

import java.util.List;
import java.util.function.BiFunction;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.Status;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.util.exec.ExecutionContext;
import com.fathzer.games.util.exec.Interruptible;

/** An abstract {@link DepthFirstAI} implementation.
 * @param <M> Implementation of the Move interface to use
 * @param <B> Implementation of the MoveGenerator interface to use
 */
public abstract class AbstractAI<M, B extends MoveGenerator<M>> implements DepthFirstAI<M, DepthFirstSearchParameters>, Interruptible {
	private final ExecutionContext<SearchContext<M,B>> context;
	private boolean interrupted;
	
	/** Constructor
	 * @param context The context to use for the search
	 */
	protected AbstractAI(ExecutionContext<SearchContext<M,B>> context) {
		this.context = context;
		this.interrupted = false;
	}
	
	@Override
    public SearchStatistics getStatistics() {
		return context.getContext().getStatistics();
	}

	/**
	 * Gets the context used for the search.
	 * @return the context
	 */
	public SearchContext<M, B> getContext() {
		return context.getContext();
	}

	@Override
    public SearchResult<M> getBestMoves(DepthFirstSearchParameters params) {
		getContext().getStatistics().clear();
		List<M> moves = getContext().getGamePosition().getMoves();
		getStatistics().movesGenerated(moves.size());
		return this.getBestMoves(moves, params);
    }

	@Override
    public SearchResult<M> getBestMoves(List<M> moves, DepthFirstSearchParameters params) {
		return getBestMoves(moves, params, (m,lowestInterestingScore)->rootEvaluation(m,params.getDepth(), lowestInterestingScore));
    }
	
	/**
	 * Evaluates a root move of the search tree.
	 * @param move The move to evaluate
	 * @param depth The depth of the search
	 * @param lowestInterestingScore The lowest interesting score under which the evaluation is not interesting (typically this can be used to cut the tree when this evaluation can't be reached)
	 * @return The score of the move (the score is computed by the {@link #getRootScore(int, int)} method), or null if the move is not valid
	 */
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
	
	/** 
	 * Gets the score of a root move.
	 * @param depth The depth of the search
	 * @param lowestInterestingScore The lowest interesting score under which the evaluation is not interesting (typically this can be used to cut the tree when this evaluation can't be reached)
	 * @return The score of the move
	 */
	protected abstract int getRootScore(final int depth, int lowestInterestingScore);

	/**
	 * Performs a search on a list of moves.
	 * <br>It is called by the {@link #getBestMoves(List, DepthFirstSearchParameters)} method and uses the execution context to process the moves (see {@link ExecutionContext#execute(Collection)}).
	 * @param moves The moves to evaluate
	 * @param params The parameters of the search
	 * @param rootEvaluator A function that evaluates the root moves
	 * @return The search result
	 */
	protected SearchResult<M> getBestMoves(List<M> moves, DepthFirstSearchParameters params, BiFunction<M,Integer, Integer> rootEvaluator) {
        final SearchResult<M> search = new SearchResult<>(params);
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
	
	/**
	 * Gets the score when the game ended during the search (for instance, for chess, when the last move played during the search is a mate).
	 * @param evaluator The evaluator used (returned by {@link SearchContext#getEvaluator()})
	 * @param status The status of the game
	 * @param depth The current search depth
	 * @param maxDepth The maximum depth of the search
	 * @return The score. The default implementation returns 0 for a draw, and calls {@link Evaluator#getWinScore(int)} for a loss.
	 */
	protected int getScore(final Evaluator<M,B> evaluator, final Status status, final int depth, int maxDepth) {
		if (Status.DRAW==status) {
			return 0;
		} else {
			//FIXME Maybe there's some games where the player wins if it can't move...
			return -evaluator.getWinScore(maxDepth-depth);
		}
	}
}
