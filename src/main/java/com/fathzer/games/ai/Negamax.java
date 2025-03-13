package com.fathzer.games.ai;

import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.Status;
import com.fathzer.games.HashProvider;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.evaluation.QuiesceEvaluator;
import com.fathzer.games.ai.transposition.AlphaBetaState;
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
public class Negamax<M,B extends MoveGenerator<M>> extends AbstractAI<M,B> implements TTAi<M, B> {
    private TranspositionTable<M, B> transpositionTable;
    private QuiesceEvaluator<M,B> quiesceEvaluator;
    
	/** Constructor
	 * @param exec The execution context
	 */
	public Negamax(ExecutionContext<SearchContext<M,B>> exec) {
		super(exec);
		quiesceEvaluator = (ctx, depth, alpha, beta) -> {
			final SearchContext<M, B> context = getContext();
			context.getStatistics().evaluationDone();
			return context.getEvaluator().evaluate(context.getGamePosition());
		};
	}

	@Override
    public SearchResult<M> getBestMoves(DepthFirstSearchParameters params) {
		final SearchResult<M> result = super.getBestMoves(params);
		// Warning, result can be empty if searching position with no possible moves
		if ((getContext().getGamePosition() instanceof HashProvider hp) && transpositionTable!=null && !isInterrupted() && !result.getList().isEmpty()) {
			// Store best move info in table
			final EvaluatedMove<M> best = result.getList().get(0);
			transpositionTable.store(hp.getHashKey(), EntryType.EXACT, params.getDepth(), best.getScore(), best.getMove(), p->true);
		}
		return result;
    }

	@Override
	protected int getRootScore(final int depth, int lowestInterestingScore) {
		return -negamax(depth-1, depth, -Integer.MAX_VALUE, -lowestInterestingScore);
	}
	
	/** Gets the evaluation of the position after <a href="https://en.wikipedia.org/wiki/Quiescence_search">quiescence search</a>.
	 * <br>The default implementation returns the quiesce policy result.
	 * @param depth The depth (number of half moves) at which the method is called (it is useful to return correct mate scores)
	 * @param alpha Alpha value after <i>normal</i> search performed by {@link #negamax(int, int, int, int)} method.
	 * @param beta Beta value after <i>normal</i> search performed by {@link #negamax(int, int, int, int)} method.
	 * @return the node evaluation
	 * @see #setQuiesceEvaluator(QuiesceEvaluator)
	 */
	protected int quiesce(int depth, int alpha, int beta) {
		return quiesceEvaluator.evaluate(getContext(), depth, alpha, beta);
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
			return quiesce(maxDepth, alpha, beta);
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
    public final TranspositionTable<M, B> getTranspositionTable() {
    	return transpositionTable;
    }
    
    @Override
    public void setTranspositonTable(TranspositionTable<M, B> table) {
    	this.transpositionTable = table;
    }

	public QuiesceEvaluator<M,B> getQuiesceEvaluator() {
		return quiesceEvaluator;
	}

	/** Sets the quiesce evaluator used to evaluate positions (see <a href="https://en.wikipedia.org/wiki/Quiescence_search">quiescence search</a>).
	 * <br>The default implementation simply returns the current position evaluation without performing any quiescence search.
	 * @param quiesceEvaluator The new quiesce evaluator.
	*/
	public void setQuiesceEvaluator(QuiesceEvaluator<M,B> quiesceEvaluator) {
		this.quiesceEvaluator = quiesceEvaluator;
	}
}
