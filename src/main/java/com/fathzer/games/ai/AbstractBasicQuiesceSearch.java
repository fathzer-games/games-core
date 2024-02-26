package com.fathzer.games.ai;

import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;

/** A basic quiesce search.
 */
public abstract class AbstractBasicQuiesceSearch<M, B extends MoveGenerator<M>> implements QuiescePolicy<M,B> {
	protected AbstractBasicQuiesceSearch() {
		super();
	}

	@Override
	public int quiesce(SearchContext<M, B> context, int alpha, int beta) {
		return quiesce(context, alpha, beta, 0);
	}

	protected int quiesce(SearchContext<M, B> context, int alpha, int beta, int quiesceDepth) {
		final SearchStatistics statistics = context.getStatistics();
		final int standPat = context.getEvaluator().evaluate(context.getGamePosition());
		statistics.evaluationDone();
		if (standPat>=beta) {
			return beta;
		}
		if (alpha < standPat) {
			alpha = standPat;
		}
		final List<M> moves = getMoves(context, quiesceDepth);
    	statistics.movesGenerated(moves.size());
        for (M move : moves) {
            if (makeMove(context, move)) {
                statistics.movePlayed();
	            final int score = -quiesce(context, -beta, -alpha, quiesceDepth+1);
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
	
	protected abstract List<M> getMoves(SearchContext<M, B> context, int quiesceDepth);
	
	protected boolean makeMove(SearchContext<M, B> context, M move) {
		return context.makeMove(move, MoveConfidence.PSEUDO_LEGAL);
	}
}
