package com.fathzer.games.ai;

import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.exec.ExecutionContext;

/**
 * <a href="https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning">AlphaBeta</a> based implementation.
 *
 * @param <M> Implementation of the Move interface to use
 * @param <B> Implementation of the {@link MoveGenerator} interface to use
 * @deprecated For testing and documentation purpose only, the preferred way to implement IA is to use {@link Negamax}.
 */
@Deprecated
public class AlphaBeta<M,B extends MoveGenerator<M>> extends AbstractAI<M,B> {
	protected AlphaBeta(ExecutionContext<M,B> exec, Evaluator<B> evaluator) {
		super(exec, evaluator);
	}

    @Override
	protected int getRootScore(final int depth, int lowestInterestingScore) {
		return alphabeta(depth-1, depth, lowestInterestingScore, Integer.MAX_VALUE, -1);
	}	
	protected void alphaCut(M move, int alpha, int score, int depth) {
//		System.out.println ("alpha cut on "+move+"at depth "+depth+" with score="+score+" (alpha is "+alpha+")");
	}

	protected void betaCut(M move, int beta, int score, int depth) {
//		System.out.println ("beta cut on "+move+"at depth "+depth+" with score="+score+" (beta is "+beta+")");
	}
	
    private int alphabeta(final int depth, int maxDepth, int alpha, int beta, final int who) {
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
            bestScore = -Integer.MAX_VALUE;
            for (M move : moves) {
//                System.out.println("Play move "+move+" at depth "+depth+" for "+who);
                if (position.makeMove(move)) {
                	hasValidMoves = true;
	                getStatistics().movePlayed();
	                final int score = alphabeta(depth-1, maxDepth, alpha, beta, -who);
	                position.unmakeMove();
	                if (score > bestScore) {
	                    bestScore = score;
	                }
	                if (bestScore >= beta) {
	                	betaCut(move, beta, score, depth);
	                	break;
	                }
					if (alpha<bestScore) {
	//					System.out.println ("alpha changed on "+move+" from "+alpha+" to "+v);
						alpha = bestScore;
					}
                }
            }
        } else {
            bestScore = Integer.MAX_VALUE;
            for (M move : moves) {
//                System.out.println("Play move "+move+" at depth "+depth+" for "+who);
                if (position.makeMove(move)) {
                	hasValidMoves = true;
	                final int score = alphabeta(depth-1, maxDepth, alpha, beta, -who);
	                position.unmakeMove();
	                if (score < bestScore) {
	                    bestScore = score;
	                }
	                if (alpha >= bestScore) {
	                	alphaCut(move, alpha, score, depth);
	                	break;
	                }
					if (beta>bestScore) {
	//					System.out.println ("beta changed on "+move+" from "+beta+" to "+v);
						beta = bestScore;
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
