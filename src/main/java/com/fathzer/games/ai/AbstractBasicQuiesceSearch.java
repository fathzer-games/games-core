package com.fathzer.games.ai;

import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;

/** A basic quiesce search.
 */
public abstract class AbstractBasicQuiesceSearch<M, B extends MoveGenerator<M>> implements QuiescePolicy {
	private final SearchContext<M, B> context;

	protected AbstractBasicQuiesceSearch(SearchContext<M, B> context) {
		this.context = context;
	}

	public int quiesce(int alpha, int beta) {
		return quiesce(alpha, beta, 0);
	}

	protected int quiesce(int alpha, int beta, int quiesceDepth) {
		final SearchStatistics statistics = context.getStatistics();
		final int standPat = context.getEvaluator().evaluate(context.getGamePosition());
		statistics.evaluationDone();
		if (standPat>=beta) {
			return beta;
		}
		if (alpha < standPat) {
			alpha = standPat;
		}
		final List<M> moves = getMoves(quiesceDepth);
    	statistics.movesGenerated(moves.size());
        for (M move : moves) {
            if (makeMove(move)) {
                statistics.movePlayed();
	            final int score = -quiesce(-beta, -alpha, quiesceDepth+1);
	            context.unmakeMove();
	            if (score >= beta) {
	                return beta;
	            }
	            if (score > alpha) {
	            	alpha = score;
	            }
            }
        }
		return alpha;
	}
	
	protected abstract List<M> getMoves(int quiesceDepth);
	
	protected boolean makeMove(M move) {
		return context.makeMove(move, MoveConfidence.PSEUDO_LEGAL);
	}
	
	protected SearchContext<M,B> getContext() {
		return context;
	}
}
