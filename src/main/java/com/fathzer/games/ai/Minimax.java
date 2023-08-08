package com.fathzer.games.ai;

import java.util.Collections;
import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;
import com.fathzer.games.ai.exec.ExecutionContext;

/**
 * <a href="https://en.wikipedia.org/wiki/Minimax">Minimax</a> based implementation.
 * @param <M> Implementation of the Move interface to use
 * @param <B> Implementation of the {@link MoveGenerator} interface to use
 * @deprecated For testing and documentation purpose only, the preferred way to implement IA is to use {@link Negamax}.
 */
@Deprecated
public abstract class Minimax<M,B extends MoveGenerator<M>> extends AbstractAI<M,B> {

    protected Minimax(ExecutionContext<M,B> exec, Evaluator<B> evaluator) {
		super(exec, evaluator);
	}

	@Override
    public SearchResult<M> getBestMoves(List<M> moves, SearchParameters params) {
		return getBestMoves(moves, params, (m,l)->minimax(Collections.singletonList(m),params.getDepth(),1,params.getDepth()));
    }

    private int minimax(List<M> moves, final int depth, final int who, int maxDepth) {
    	final B position = getGamePosition();
    	if (depth == 0 || isInterrupted()) {
    		getStatistics().evaluationDone();
            return who * getEvaluator().evaluate(position);
        } else if (moves==null) {
        	final Status status = position.getStatus();
			if (Status.DRAW.equals(status)) {
				return 0;
			} else if (!Status.PLAYING.equals(status)){
				int nbMoves = (maxDepth-depth+1)/2;
				return -getEvaluator().getWinScore(nbMoves);
			} else {
				moves = position.getMoves();
				getStatistics().movesGenerated(moves.size());
			}
        }
    	int bestScore;
        if (who > 0) {
            // max
            bestScore = Integer.MIN_VALUE;
            for (M move : moves) {
                position.makeMove(move);
                getStatistics().movePlayed();
                int score = minimax(null, depth-1, -who, maxDepth);
                position.unmakeMove();
                if (score > bestScore) {
                    bestScore = score;
                }
            }
        } else {
            // min
            bestScore = Integer.MAX_VALUE;
            for (M move : moves) {
                position.makeMove(move);
                int score = minimax(null, depth-1, -who, maxDepth);
                position.unmakeMove();
                if (score < bestScore) {
                    bestScore = score;
                }
            }
        }
        return bestScore;
    }
}
