package com.fathzer.games.ai;

import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.exec.ExecutionContext;

/**
 * <a href="https://en.wikipedia.org/wiki/Minimax">Minimax</a> based implementation.
 * @param <M> Implementation of the Move interface to use
 * @param <B> Implementation of the {@link MoveGenerator} interface to use
 * @deprecated For testing and documentation purpose only, the preferred way to implement IA is to use {@link Negamax}.
 */
@Deprecated
public class Minimax<M,B extends MoveGenerator<M>> extends AbstractAI<M,B> {

    protected Minimax(ExecutionContext<M,B> exec, Evaluator<B> evaluator) {
		super(exec, evaluator);
	}

    @Override
	protected int getRootScore(final int depth, int lowestInterestingScore) {
		return minimax(depth-1, depth, -1);
	}

    private int minimax(final int depth, int maxDepth, final int who) {
    	final B position = getGamePosition();
    	if (depth == 0 || isInterrupted()) {
    		getStatistics().evaluationDone();
            return who * getEvaluator().evaluate(position);
        } else {
        	final Status status = position.getStatus();
			if (Status.DRAW.equals(status)) {
				return 0;
			} else if (!Status.PLAYING.equals(status)){
				return -getEvaluator().getWinScore(maxDepth-depth)*who;
			}
        }
		List<M> moves = position.getMoves();
		getStatistics().movesGenerated(moves.size());
    	int bestScore;
    	boolean hasValidMoves = false;
        if (who > 0) {
            // max
            bestScore = -Integer.MAX_VALUE;
            for (M move : moves) {
                if (position.makeMove(move)) {
                	hasValidMoves = true;
	                getStatistics().movePlayed();
	                int score = minimax(depth-1, maxDepth, -who);
	                position.unmakeMove();
	                if (score > bestScore) {
	                    bestScore = score;
	                }
                }
            }
        } else {
            // min
            bestScore = Integer.MAX_VALUE;
            for (M move : moves) {
                if (position.makeMove(move)) {
                	hasValidMoves = true;
	                int score = minimax(depth-1, maxDepth, -who);
	                position.unmakeMove();
	                if (score < bestScore) {
	                    bestScore = score;
	                }
                }
            }
        }
        // Let's imagine check mate with no detection at the beginning of the search and all pseudo moves are impossible (because of check mate)
        // hasValidMoves should be useful there.
        if (!hasValidMoves) {
        	throw new IllegalStateException("This should not happen with early mate detection");
        }
        return bestScore;
    }
}
