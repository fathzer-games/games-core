package com.fathzer.games.ai;

import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.Status;
import com.fathzer.games.HashProvider;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.transposition.EntryType;
import com.fathzer.games.ai.transposition.TTAi;
import com.fathzer.games.ai.transposition.TranspositionTable;
import com.fathzer.games.ai.transposition.TranspositionTableEntry;
import com.fathzer.games.util.exec.ExecutionContext;

/**
 * A Negamax with alpha beta pruning implementation and transposition table usage.
 * @param <M> The type of the moves
 * @param <B> The type of the {@link MoveGenerator} to use
 */
public class Negamax<M,B extends MoveGenerator<M>> extends AbstractAI<M,B> implements TTAi<M> {
    private TranspositionTable<M> transpositionTable;
    private QuiescePolicy<M,B> quiescePolicy;
    
	public Negamax(ExecutionContext<SearchContext<M,B>> exec) {
		super(exec);
		quiescePolicy = (ctx, alpha, beta) -> getContext().getEvaluator().evaluate(getContext().getGamePosition());
	}

	@Override
    public SearchResult<M> getBestMoves(SearchParameters params) {
		SearchResult<M> result = super.getBestMoves(params);
		final B gamePosition = getContext().getGamePosition();
		if ((gamePosition instanceof HashProvider) && transpositionTable!=null && !isInterrupted()) {
			// Store best move info in table
			final EvaluatedMove<M> best = result.getList().get(0);
			transpositionTable.store(((HashProvider)gamePosition).getHashKey(), EntryType.EXACT, params.getDepth(), best.getScore(), best.getContent(), p->true);
		}
		return result;
    }

	@Override
	protected int getRootScore(final int depth, int lowestInterestingScore) {
		return -negamax(depth-1, depth, -Integer.MAX_VALUE, -lowestInterestingScore);
	}
	
	/** Gets the evaluation of the position after <a href="https://en.wikipedia.org/wiki/Quiescence_search">quiescence search</a>.
	 * <br>The default implementation returns the quiesce policy result.
	 * @param alpha Alpha value after <i>normal</i> search performed by {@link #negamax(int, int, int, int)} method.
	 * @param beta Beta value after <i>normal</i> search performed by {@link #negamax(int, int, int, int)} method.
	 * @return the node evaluation
	 * @see #setQuiescePolicy(QuiescePolicy)
	 */
	protected int quiesce(int alpha, int beta) {
		return quiescePolicy.quiesce(getContext(), alpha, beta);
	}
	
    protected int negamax(final int depth, int maxDepth, int alpha, int beta) {
    	final SearchContext<M, B> context = getContext();
		final B position = context.getGamePosition();
     	final Evaluator<M, B> evaluator = context.getEvaluator();
    	final Status fastAnalysisStatus = position.getContextualStatus();
    	if (fastAnalysisStatus!=Status.PLAYING) {
    		return getScore(evaluator, fastAnalysisStatus, depth, maxDepth);
    	}

		final boolean keyProvider = (position instanceof HashProvider) && transpositionTable!=null;
		final long key;
		final AlphaBetaState<M> state;
		if (keyProvider) {
			key = ((HashProvider)position).getHashKey();
			TranspositionTableEntry<M> entry = transpositionTable.get(key);
			state = transpositionTable.getPolicy().accept(entry, depth, alpha, beta, v -> ttToScore(v, depth, maxDepth, evaluator));
			if (state.isValueSet()) {
				return state.getValue();
			} else if (state.isAlphaBetaUpdated()) {
				alpha = state.getAlphaUpdated();
				beta = state.getBetaUpdated();
			}
		} else {
			key = 0;
			state = null;
		}
     	if (depth == 0 || isInterrupted()) {
			return quiesce(alpha, beta);
        }

        int value = Integer.MIN_VALUE;
        M bestMove = null;
        boolean noValidMove = true;
    	final M moveFromTT = state!=null ? state.getBestMove() : null;
    	boolean moveFromTTBreaks = false;
    	if (moveFromTT!=null && getContext().makeMove(moveFromTT, MoveConfidence.UNSAFE)) {
    		// Try move from TT
        	noValidMove = false;
            getStatistics().moveFromTTPlayed();
            final int score = -negamax(depth-1, maxDepth, -beta, -alpha);
            getContext().unmakeMove();
            if (score > value) {
                value = score;
                bestMove = moveFromTT;
                if (score > alpha) {
                	alpha = score;
                    if (score >= beta) {
                    	moveFromTTBreaks = true;
                    }
                }
            }   		
    	}
    	if (!moveFromTTBreaks) {
    		final List<M> moves = position.getMoves();
        	getStatistics().movesGenerated(moves.size());
	        for (M move : moves) {
	            if (!move.equals(moveFromTT) && getContext().makeMove(move, MoveConfidence.PSEUDO_LEGAL)) {
	            	noValidMove = false;
		            getStatistics().movePlayed();
		            final int score = -negamax(depth-1, maxDepth, -beta, -alpha);
		            getContext().unmakeMove();
		            if (score > value) {
		                value = score;
		                bestMove = move;
		                if (score > alpha) {
		                	alpha = score;
		                    if (score >= beta) {
		                    	break;
		                    }
		                }
		            }
	            }
	        }
	        
	        if (noValidMove) {
				// Player can't move it's a draw or a loose
	        	value = getScore(evaluator, position.getEndGameStatus(), depth, maxDepth);
	        	if (value>alpha) {
	        		alpha = value;
	        	}
	         }
    	}
        
        if (keyProvider && !isInterrupted()) {
        	// If a transposition table is available
        	state.setValue(value);
        	state.updateAlphaBeta(alpha, beta);
        	state.setBestMove(bestMove);
        	transpositionTable.getPolicy().store(transpositionTable, key, state, v -> scoreToTT(v, depth, maxDepth, evaluator));
        }
        return value;
    }
    
    @Override
    public final TranspositionTable<M> getTranspositionTable() {
    	return transpositionTable;
    }
    
    @Override
    public void setTranspositonTable(TranspositionTable<M> table) {
    	this.transpositionTable = table;
    }

	public QuiescePolicy<M,B> getQuiescePolicy() {
		return quiescePolicy;
	}

	/** Sets the quiesce policy used to evaluate positions (see <a href="https://en.wikipedia.org/wiki/Quiescence_search">quiescence search</a>).
	 * <br>The default implementation simply returns the current position evaluation without performing any quiescence search.
	 * @param alpha Alpha value after <i>normal</i> search performed by {@link #negamax(int, int, int, int)} method.
	 * @param beta Beta value after <i>normal</i> search performed by {@link #negamax(int, int, int, int)} method.
	 * @return the position's evaluation
	*/
	public void setQuiescePolicy(QuiescePolicy<M,B> quiescePolicy) {
		this.quiescePolicy = quiescePolicy;
	}
}
