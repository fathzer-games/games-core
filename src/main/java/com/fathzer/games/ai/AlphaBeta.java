package com.fathzer.games.ai;

import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.util.exec.ExecutionContext;
import com.fathzer.games.Status;
import com.fathzer.games.ai.evaluation.Evaluator;

/**
 * <a href="https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning">AlphaBeta</a> based implementation.
 *
 * @param <M> Implementation of the Move interface to use
 * @param <B> Implementation of the {@link MoveGenerator} interface to use
 * @deprecated For testing and documentation purpose only, the preferred way to implement AI is to use {@link Negamax}.
 */
@Deprecated(since="always", forRemoval=false)
@SuppressWarnings("java:S1133")
public class AlphaBeta<M,B extends MoveGenerator<M>> extends AbstractAI<M, B> {
	/** Constructor.
	 * @param exec The execution context
	 */
	protected AlphaBeta(ExecutionContext<SearchContext<M,B>> exec) {
		super(exec);
	}

    @Override
	protected int getRootScore(final int depth, int lowestInterestingScore) {
		return alphabeta(depth-1, depth, lowestInterestingScore, Integer.MAX_VALUE, -1);
	}

	/** This method is called when an alpha cut is detected.
	 * @param move The move that caused the alpha cut
	 * @param alpha The alpha value at the time of the cut
	 * @param score The score of the move that caused the cut
	 * @param depth The depth of the search at the time of the cut
	 * <br>The default implementation does nothing.
	 */
	protected void alphaCut(M move, int alpha, int score, int depth) {
		// Does nothing by default
	}

	/** This method is called when a beta cut is detected.
	 * @param move The move that caused the beta cut
	 * @param beta The beta value at the time of the cut
	 * @param score The score of the move that caused the cut
	 * @param depth The depth of the search at the time of the cut
	 * <br>The default implementation does nothing.
	 */
	protected void betaCut(M move, int beta, int score, int depth) {
		// Does nothing by default
	}
	
    private int alphabeta(final int depth, int maxDepth, int alpha, int beta, final int who) {
    	final SearchContext<M, B> context = getContext();
    	final B position = context.getGamePosition();
    	final Evaluator<M, B> evaluator = context.getEvaluator();
    	if (depth == 0 || isInterrupted()) {
    		getStatistics().evaluationDone();
            return who * evaluator.evaluate(position);
        }
    	final Status fastAnalysisStatus = position.getContextualStatus();
		if (fastAnalysisStatus==Status.DRAW) {
        	return getScore(evaluator, position.getEndGameStatus(), depth, maxDepth)*who;
    	}

		List<M> moves = position.getMoves();
		getStatistics().movesGenerated(moves.size());
        int bestScore;
    	boolean hasValidMoves = false;
        if (who > 0) {
            bestScore = -Integer.MAX_VALUE;
            for (M move : moves) {
                if (getContext().makeMove(move, MoveConfidence.PSEUDO_LEGAL)) {
                	hasValidMoves = true;
	                getStatistics().movePlayed();
	                final int score = alphabeta(depth-1, maxDepth, alpha, beta, -who);
	                getContext().unmakeMove();
	                if (score > bestScore) {
	                    bestScore = score;
	                }
	                if (bestScore >= beta) {
	                	betaCut(move, beta, score, depth);
	                	break;
	                }
					if (alpha<bestScore) {
						alpha = bestScore;
					}
                }
            }
        } else {
            bestScore = Integer.MAX_VALUE;
            for (M move : moves) {
                if (getContext().makeMove(move, MoveConfidence.PSEUDO_LEGAL)) {
                	hasValidMoves = true;
	                final int score = alphabeta(depth-1, maxDepth, alpha, beta, -who);
	                getContext().unmakeMove();
	                if (score < bestScore) {
	                    bestScore = score;
	                }
	                if (alpha >= bestScore) {
	                	alphaCut(move, alpha, score, depth);
	                	break;
	                }
					if (beta>bestScore) {
						beta = bestScore;
					}
                }
            }
        }
        if (!hasValidMoves) {
        	return position.getEndGameStatus()==Status.DRAW ? 0 : -evaluator.getWinScore(maxDepth-depth)*who;
        }
        return bestScore;
    }
}
