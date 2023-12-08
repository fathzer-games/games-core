package com.fathzer.games.ai;

import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.util.exec.ExecutionContext;
import com.fathzer.games.Status;

/**
 * <a href="https://en.wikipedia.org/wiki/Minimax">Minimax</a> based implementation.
 * @param <M> Implementation of the Move interface to use
 * @param <B> Implementation of the {@link MoveGenerator} interface to use
 * @deprecated For testing and documentation purpose only, the preferred way to implement IA is to use {@link Negamax}.
 */
@Deprecated
public class Minimax<M,B extends MoveGenerator<M>> extends AbstractAI<M,B> {

    protected Minimax(ExecutionContext<SearchContext<M,B>> exec) {
		super(exec);
	}

    @Override
	protected int getRootScore(final int depth, int lowestInterestingScore) {
		return minimax(depth-1, depth, -1);
	}

    private int minimax(final int depth, int maxDepth, final int who) {
    	final B position = getGamePosition();
    	if (depth == 0 || isInterrupted()) {
    		getStatistics().evaluationDone();
            return who * getEvaluator().evaluate();
        }
    	final Status fastAnalysisStatus = position.getContextualStatus();
		if (fastAnalysisStatus==Status.DRAW) {
        	return getScore(position.getEndGameStatus(), depth, maxDepth)*who;
    	}
		
		List<M> moves = position.getMoves(false);
		getStatistics().movesGenerated(moves.size());
    	int bestScore;
    	boolean hasValidMoves = false;
        if (who > 0) {
            // max
            bestScore = -Integer.MAX_VALUE;
            for (M move : moves) {
                if (getContext().makeMove(move, MoveConfidence.PSEUDO_LEGAL)) {
                	hasValidMoves = true;
	                getStatistics().movePlayed();
	                int score = minimax(depth-1, maxDepth, -who);
	                getContext().unmakeMove();
	                if (score > bestScore) {
	                    bestScore = score;
	                }
                }
            }
        } else {
            // min
            bestScore = Integer.MAX_VALUE;
            for (M move : moves) {
                if (getContext().makeMove(move, MoveConfidence.PSEUDO_LEGAL)) {
                	hasValidMoves = true;
	                int score = minimax(depth-1, maxDepth, -who);
	                getContext().unmakeMove();
	                if (score < bestScore) {
	                    bestScore = score;
	                }
                }
            }
        }
        if (!hasValidMoves) {
        	return position.getEndGameStatus()==Status.DRAW ? 0 : -getEvaluator().getWinScore(maxDepth-depth)*who;
        }
        return bestScore;
    }
}
