package com.fathzer.games.ai;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;
import com.fathzer.games.util.ContextualizedExecutor;
import com.fathzer.games.util.Evaluation;

/**
 * AlphaBeta based implementation.
 *
 * @param <M> Implementation of the Move interface to use
 */
public abstract class AlphaBeta<M> extends AbstractAI<M> {
	
	protected AlphaBeta(Supplier<MoveGenerator<M>> moveGeneratorBuilder, ContextualizedExecutor<MoveGenerator<M>> exec) {
		super(moveGeneratorBuilder, exec);
	}

	@Override
    public List<Evaluation<M>> getBestMoves(final int depth, List<M> moves, int size, int accuracy) {
		return getBestMoves(depth, moves, size, accuracy, (c,l)->alphabeta(c,depth,1,depth,l,Integer.MAX_VALUE));
    }
	
	protected void alphaCut(M move, int alpha, int score, int depth) {
//		System.out.println ("alpha cut on "+move+"at depth "+depth+" with score="+score+" (alpha is "+alpha+")");
	}

	protected void betaCut(M move, int beta, int score, int depth) {
//		System.out.println ("beta cut on "+move+"at depth "+depth+" with score="+score+" (beta is "+beta+")");
	}
	
    private int alphabeta(Iterator<M> moves, final int depth, final int who, int maxDepth, int alpha, int beta) {
    	final MoveGenerator<M> moveGenerator = getMoveGenerator();
    	if (depth == 0 || isInterrupted()) {
            return who * evaluate();
        } else if (moves==null) {
        	final Status status = moveGenerator.getStatus();
			if (Status.DRAW.equals(status)) {
				return 0;
			} else if (!Status.PLAYING.equals(status)){
				final int nbMoves = (maxDepth-depth+1)/2;
				return -getWinScore(nbMoves)*who;
			} else {
				moves = moveGenerator.getMoves().iterator();
			}
        }
        int bestScore;
        if (who > 0) {
            bestScore = Integer.MIN_VALUE;
            while (moves.hasNext()) {
                M move = moves.next();
//                System.out.println("Play move "+move+" at depth "+depth+" for "+who);
                moveGenerator.makeMove(move);
                final int score = alphabetaScore(depth, who, maxDepth, alpha, beta);
                moveGenerator.unmakeMove();
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
            while (moves.hasNext()) {
                M move = moves.next();
//                System.out.println("Play move "+move+" at depth "+depth+" for "+who);
                moveGenerator.makeMove(move);
                final int score = alphabetaScore(depth, who, maxDepth, alpha, beta);
                moveGenerator.unmakeMove();
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
