package com.fathzer.games.ai;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import com.fathzer.games.GameState;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;
import com.fathzer.games.util.ContextualizedExecutor;
import com.fathzer.games.util.Evaluation;

/**
 * A Negamax with alpha beta pruning implementation.
 * @param <M> Implementation of the Move interface to use
 */
public abstract class Negamax<M> extends AbstractAI<M> {
	protected Negamax(Supplier<MoveGenerator<M>> moveGeneratorBuilder, ContextualizedExecutor<MoveGenerator<M>> exec) {
		super(moveGeneratorBuilder, exec);
	}

	@Override
    public List<Evaluation<M>> getBestMoves(final int depth, Iterator<M> moves, int size, int accuracy) {
		return getBestMoves(depth, moves, size, accuracy, (c,alpha)-> get(c,depth,alpha));
    }

	private int get(Iterator<M> moves, final int depth, int alpha) {
    	if (alpha==Integer.MIN_VALUE) {
    		// WARNING: -Integer.MIN_VALUE is equals to ... Integer.MIN_VALUE
    		// So using it as alpha value makes negamax fail 
    		alpha += 1;
    	}
    	final MoveGenerator<M> moveGenerator = getMoveGenerator();
        M move = moves.next();
//System.out.println("Play move "+move+" at depth "+depth+" for "+1);
        moveGenerator.makeMove(move);
        final int score = -negamax(depth-1, depth, -Integer.MAX_VALUE, -alpha, -1);
        moveGenerator.unmakeMove();
        return score;
	}
	
	protected void cut(M move, int alpha, int beta, int score, int depth) {
//		System.out.println ("alpha cut on "+move+"at depth "+depth+" with score="+score+" (alpha is "+alpha+")");
	}
	
    private int negamax(final int depth, int maxDepth, int alpha, int beta, final int who) {
    	final MoveGenerator<M> context = getMoveGenerator();
    	if (depth == 0 || isInterrupted()) {
//System.out.println("Evaluation: "+context.evaluate()+" * "+who);
            return who * evaluate();
        }
    	final GameState<M> state = context.getState();
		if (Status.DRAW.equals(state.getStatus())) {
			return 0;
		} else if (!Status.PLAYING.equals(state.getStatus())){
			final int nbMoves = (maxDepth-depth+1)/2;
			return -getWinScore(nbMoves);
		}
    	final Iterator<M> moves = state.iterator();
        int value = Integer.MIN_VALUE;
        while (moves.hasNext()) {
            M move = moves.next();
//System.out.println("Play move "+move+" at depth "+depth+" for "+1);
            context.makeMove(move);
            final int score = -negamax(depth-1, maxDepth, -beta, -alpha, -who);
            context.unmakeMove();
            if (score > value) {
                value = score;
            }
            if (value > alpha) {
            	alpha = value;
            }
            if (alpha >= beta) {
            	cut(move, alpha, beta, score, depth);
            	break;
            }
        }
        return value;
    }
}
