package com.fathzer.games.ai;

import java.util.Collections;
import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;
import com.fathzer.games.ai.exec.ExecutionContext;

/**
 * <a href="https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning">AlphaBeta</a> based implementation.
 *
 * @param <M> Implementation of the Move interface to use
 * @param <B> Implementation of the {@link MoveGenerator} interface to use
 * @deprecated For testing and documentation purpose only, the preferred way to implement IA is to use {@link Negamax}.
 */
@Deprecated
public abstract class AlphaBeta<M,B extends MoveGenerator<M>> extends AbstractAI<M,B> implements MoveSorter<M> {
	//TODO Verify it's still working
	
	protected AlphaBeta(ExecutionContext<M,B> exec, Evaluator<B> evaluator) {
		super(exec, evaluator);
	}

	@Override
    public SearchResult<M> getBestMoves(List<M> moves, SearchParameters params) {
		return getBestMoves(moves, params, (m,l)->alphabeta(Collections.singletonList(m),params.getDepth(),1,params.getDepth(),l,Integer.MAX_VALUE));
    }
	
	protected void alphaCut(M move, int alpha, int score, int depth) {
//		System.out.println ("alpha cut on "+move+"at depth "+depth+" with score="+score+" (alpha is "+alpha+")");
	}

	protected void betaCut(M move, int beta, int score, int depth) {
//		System.out.println ("beta cut on "+move+"at depth "+depth+" with score="+score+" (beta is "+beta+")");
	}
	
    private int alphabeta(List<M> moves, final int depth, final int who, int maxDepth, int alpha, int beta) {
    	final B position = getGamePosition();
    	if (depth == 0 || isInterrupted()) {
    		getStatistics().evaluationDone();
            return who * getEvaluator().evaluate(position);
        } else if (moves==null) {
        	final Status status = position.getStatus();
			if (Status.DRAW.equals(status)) {
				return 0;
			} else if (!Status.PLAYING.equals(status)){
				final int nbMoves = (maxDepth-depth+1)/2;
				return -getEvaluator().getWinScore(nbMoves)*who;
			} else {
				moves = sort(position.getMoves());
				getStatistics().movesGenerated(moves.size());
			}
        }
        int bestScore;
        if (who > 0) {
            bestScore = Integer.MIN_VALUE;
            for (M move : moves) {
//                System.out.println("Play move "+move+" at depth "+depth+" for "+who);
                position.makeMove(move);
                getStatistics().movePlayed();
                final int score = alphabetaScore(depth, who, maxDepth, alpha, beta);
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
                alpha = Math.max(bestScore, alpha);
            }
        } else {
            bestScore = Integer.MAX_VALUE;
            for (M move : moves) {
//                System.out.println("Play move "+move+" at depth "+depth+" for "+who);
                position.makeMove(move);
                final int score = alphabetaScore(depth, who, maxDepth, alpha, beta);
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
        return bestScore;
    }

	protected int alphabetaScore(final int depth, final int who, int maxDepth, final int alpha, final int beta) {
		return alphabeta(null, depth - 1, -who, maxDepth, alpha, beta);
	}
}
