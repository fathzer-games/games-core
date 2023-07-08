package com.fathzer.games.ai;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;
import com.fathzer.games.util.ContextualizedExecutor;
import com.fathzer.games.util.Evaluation;

/**
 * Minimax based implementation.
 * @param <M> Implementation of the Move interface to use
 * @deprecated For testing and documentation purpose only, the preferred way to implement IA is to use {@link Negamax}.
 */
@Deprecated
public abstract class Minimax<M> extends AbstractAI<M> {

    protected Minimax(Supplier<MoveGenerator<M>> moveGeneratorBuilder, ContextualizedExecutor<MoveGenerator<M>> exec) {
		super(moveGeneratorBuilder, exec);
	}

	@Override
    public List<Evaluation<M>> getBestMoves(final int depth, Iterator<M> moves, int size, int accuracy) {
		return getBestMoves(depth, moves, size, accuracy, (c,l)->minimax(c,depth,1,depth));
    }

    private int minimax(Iterator<M> moves, final int depth, final int who, int maxDepth) {
    	final MoveGenerator<M> moveGenerator = getMoveGenerator();
    	if (depth == 0 || isInterrupted()) {
            return who * evaluate();
        } else if (moves==null) {
        	final Status status = moveGenerator.getStatus();
			if (Status.DRAW.equals(status)) {
				return 0;
			} else if (!Status.PLAYING.equals(status)){
				int nbMoves = (maxDepth-depth+1)/2;
				return -getWinScore(nbMoves)*who;
			} else {
				moves = moveGenerator.getMoves().iterator();
			}
        }
    	int bestScore;
        if (who > 0) {
            // max
            bestScore = Integer.MIN_VALUE;
            while (moves.hasNext()) {
                M move = moves.next();
                moveGenerator.makeMove(move);
                int score = minimax(null, depth-1, -who, maxDepth);
                moveGenerator.unmakeMove();
                if (score > bestScore) {
                    bestScore = score;
                }
            }
        } else {
            // min
            bestScore = Integer.MAX_VALUE;
            while (moves.hasNext()) {
                M move = moves.next();
                moveGenerator.makeMove(move);
                int score = minimax(null, depth-1, -who, maxDepth);
                moveGenerator.unmakeMove();
                if (score < bestScore) {
                    bestScore = score;
                }
            }
        }
        return bestScore;
    }
}
