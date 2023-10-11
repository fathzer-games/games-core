package com.fathzer.games.ai;

import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;
import com.fathzer.games.HashProvider;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.ai.transposition.EntryType;
import com.fathzer.games.ai.transposition.TTAi;
import com.fathzer.games.ai.transposition.TranspositionTable;
import com.fathzer.games.ai.transposition.TranspositionTableEntry;

/**
 * A Negamax with alpha beta pruning implementation.
 * @param <M> The type of the moves
 * @param <B> The type of the {@link MoveGenerator} to use
 */
public class Negamax<M,B extends MoveGenerator<M>> extends AbstractAI<M,B> implements TTAi<M> {
    private TranspositionTable<M> transpositionTable;
    
	public Negamax(ExecutionContext<M,B> exec, Evaluator<B> evaluator) {
		super(exec, evaluator);
	}
	
	@Override
    public SearchResult<M> getBestMoves(SearchParameters params) {
		SearchResult<M> result = super.getBestMoves(params);
		if ((getGamePosition() instanceof HashProvider) && transpositionTable!=null && !isInterrupted()) {
			// Store best move info in table
			final EvaluatedMove<M> best = result.getList().get(0);
			transpositionTable.store(((HashProvider)getGamePosition()).getHashKey(), EntryType.EXACT, params.getDepth(), best.getScore(), best.getContent(), p->true);
		}
		return result;
    }

	protected int getRootScore(final int depth, int lowestInterestingScore) {
		return -negamax(depth-1, depth, -Integer.MAX_VALUE, -lowestInterestingScore, -1);
	}
	
	/** Called when a cut occurs.
	 * @param move The move that triggers the cut
	 * @param alpha The alpha value when cut occurred
	 * @param beta The beta value when cut occurred
	 * @param value The value when cut occurred
	 * @param depth The depth at which the cut occurred
	 */
	protected void cut(M move, int alpha, int beta, int value, int depth) {
//		System.out.println ("alpha cut on "+move+"at depth "+depth+" with score="+value+" (alpha is "+alpha+")");
	}
	
    protected int negamax(final int depth, int maxDepth, int alpha, int beta, final int who) {
		final B position = getGamePosition();
    	if (depth == 0 || isInterrupted()) {
    		getStatistics().evaluationDone();
    		return who * getEvaluator().evaluate(position);
        }
    	if (position.isRepetition()==Status.DRAW) {
    		return 0;
    	}

		final boolean keyProvider = (position instanceof HashProvider) && transpositionTable!=null;
		final long key;
		final AlphaBetaState<M> state;
		if (keyProvider) {
			key = ((HashProvider)position).getHashKey();
			TranspositionTableEntry<M> entry = transpositionTable.get(key);
			state = transpositionTable.getPolicy().accept(entry, depth, alpha, beta, v -> scoreToTT(v, depth, maxDepth, getEvaluator()));
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

		final List<M> moves = position.getMoves();
    	getStatistics().movesGenerated(moves.size());
    	if (state!=null) {
    		insert(state.getBestMove(), moves);
    	}
        int value = Integer.MIN_VALUE;
        M bestMove = null;
        boolean noValidMove = true; 
        for (M move : moves) {
            if (position.makeMove(move)) {
            	noValidMove = false;
	            getStatistics().movePlayed();
	            final int score = -negamax(depth-1, maxDepth, -beta, -alpha, -who);
	            position.unmakeMove();
	            if (score > value) {
	                value = score;
	                bestMove = move;
	                if (score > alpha) {
	                	alpha = score;
	                    if (score >= beta) {
	                    	cut(move, alpha, beta, score, depth);
	                    	break;
	                    }
	                }
	            }
            }
        }
        
        if (noValidMove) {
			// Player can't move it's a draw or a loose
			//TODO Maybe there's some games where the player wins if it can't move...
        	value = position.onNoValidMove()==Status.DRAW ? 0 : -getEvaluator().getWinScore(maxDepth-depth);
        	if (value>alpha) {
        		alpha = value;
        	}
         }
        
        if (keyProvider && !isInterrupted()) {
        	// If a transposition table is available
        	state.setValue(value);
        	state.updateAlphaBeta(alpha, beta);
        	state.setBestMove(bestMove);
        	transpositionTable.getPolicy().store(transpositionTable, key, state, v -> ttToScore(v, depth, maxDepth, getEvaluator()));
        }
        return value;
    }

    
    /** Gets the transposition table used by this instance.
     * @return a transposition table.
     */
    public final TranspositionTable<M> getTranspositionTable() {
    	return transpositionTable;
    }
    
    public void setTranspositonTable(TranspositionTable<M> table) {
    	this.transpositionTable = table;
    }

	protected void insert(M best, List<M> moves) {
		if (best!=null) {
			if (moves.remove(best)) {
				moves.add(0, best);
			} else {
				// Remember, move can not be possible (if it come from a different game position with same hash key
				// We can simply ignore the best move if it is not possible
				if (Boolean.getBoolean("HashCollisionChase")) {
					//TODO Let it there for a while, it already help to find a bug in hash key building...
					throw new IllegalArgumentException("Strange, best move "+best+" does not exists (hash="+((HashProvider)getGamePosition()).getHashKey()+")");
				}
			}
		}
	}
}
