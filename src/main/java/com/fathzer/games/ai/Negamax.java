package com.fathzer.games.ai;

import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;
import com.fathzer.games.HashProvider;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.ai.transposition.EntryType;
import com.fathzer.games.ai.transposition.TranspositionTable;
import com.fathzer.games.ai.transposition.TranspositionTableEntry;
import com.fathzer.games.util.Evaluation;

/**
 * A Negamax with alpha beta pruning implementation.
 * @param <M> The type of the moves
 * @param <B> The type of the {@link MoveGenerator} to use
 */
public class Negamax<M,B extends MoveGenerator<M>> extends AbstractAI<M,B> implements MoveSorter<M> {
    private TranspositionTable<M> transpositionTable;
    
	public Negamax(ExecutionContext<M,B> exec, Evaluator<B> evaluator) {
		super(exec, evaluator);
	}
	
	@Override
    public SearchResult<M> getBestMoves(SearchParameters params) {
		SearchResult<M> result = super.getBestMoves(params);
		if ((getGamePosition() instanceof HashProvider) && transpositionTable!=null && !isInterrupted()) {
			// Store best move info in table
			final Evaluation<M> best = result.getList().get(0);
			transpositionTable.store(((HashProvider)getGamePosition()).getHashKey(), EntryType.EXACT, params.getDepth(), best.getValue(), best.getContent(), p->true);
		}
		return result;
    }

	@Override
    public SearchResult<M> getBestMoves(List<M> moves, SearchParameters params) {
		return getBestMoves(moves, params, (m,alpha)-> rootEvaluation(m,params.getDepth(),alpha));
    }

	private int rootEvaluation(M move, final int depth, int alpha) {
    	if (alpha==Integer.MIN_VALUE) {
    		// WARNING: -Integer.MIN_VALUE is equals to ... Integer.MIN_VALUE
    		// So using it as alpha value makes negamax fail 
    		alpha += 1;
    	}
    	final MoveGenerator<M> moveGenerator = getGamePosition();
//System.out.println("Play move "+move+" at depth "+depth+" for "+1);
        moveGenerator.makeMove(move);
        getStatistics().movePlayed();
        final int score = -negamax(depth-1, depth, -Integer.MAX_VALUE, -alpha, -1);
        moveGenerator.unmakeMove();
        return score;
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
	
	protected Integer getEndOfSearchScore (B position, final int depth, int maxDepth, final int who) {
    	if (depth == 0 || isInterrupted()) {
//System.out.println("Evaluation: "+context.evaluate()+" * "+who);
    		getStatistics().evaluationDone();
    		return who * getEvaluator().evaluate(position);
        }
    	final Status status = position.getStatus();
		if (Status.DRAW.equals(status)) {
			return 0;
		} else if (!Status.PLAYING.equals(status)){
			// Player looses after nbMoves half moves
            return -getEvaluator().getWinScore(maxDepth-depth);
		} else {
			return null;
		}
	}
	
    protected int negamax(final int depth, int maxDepth, int alpha, int beta, final int who) {
		final B position = getGamePosition();
		Integer endOfSearchScore = getEndOfSearchScore(position, depth, maxDepth, who);
		if (endOfSearchScore!=null) {
			return endOfSearchScore;
		}
		
		final boolean keyProvider = (position instanceof HashProvider) && transpositionTable!=null;
		final long key;
		final AlphaBetaState<M> state;
		if (keyProvider) {
			key = ((HashProvider)position).getHashKey();
			TranspositionTableEntry<M> entry = transpositionTable.get(key);
			state = transpositionTable.getPolicy().accept(entry, depth, alpha, beta);
			if (state.isValueSet()) {
				return fromTTScore(state.getValue(), maxDepth);
			} else if (state.isAlphaBetaUpdated()) {
				alpha = state.getAlphaUpdated();
				beta = state.getBetaUpdated();
			}
		} else {
			key = 0;
			state = null;
		}

		final List<M> moves = sort(state==null?null:state.getBestMove(), position.getMoves());
    	getStatistics().movesGenerated(moves.size());
        int value = Integer.MIN_VALUE;
        M bestMove = null;
        for (M move : moves) {
            position.makeMove(move);
            getStatistics().movePlayed();
            final int score = -negamax(depth-1, maxDepth, -beta, -alpha, -who);
            position.unmakeMove();
            if (score > value) {
                value = score;
                bestMove = move;
            }
            if (value > alpha) {
            	alpha = value;
            }
            if (alpha >= beta) {
            	cut(move, alpha, beta, score, depth);
            	break;
            }
        }
        
        if (keyProvider && !isInterrupted()) {
        	// If a transposition table is available
        	state.setValue(value);
        	state.updateAlphaBeta(alpha, beta);
        	state.setBestMove(bestMove);
        	transpositionTable.getPolicy().store(transpositionTable, key, state, v -> toTTScore(v, depth, maxDepth));
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

	protected List<M> sort(M best, List<M> moves) {
		final List<M> result = sort(moves);
		if (best!=null) {
			int index = moves.indexOf(best);
			if (index<0) {
				throw new IllegalArgumentException("Strange, best move "+best+" does not exists");
			} else if (index!=0) {
				// Put best move in first place
				result.add(0, result.remove(index));
			}
		}
		return result;
	}
	
	protected int toTTScore(int value, int depth, int maxDepth) {
		final Evaluator<B> ev = getEvaluator();
		if (ev.isWinLooseScore(value, maxDepth)) {
			return ev.getWinScore(depth);
		} else {
			return value;
		}
	}
	
	protected int fromTTScore(int value, int maxDepth) {
		final Evaluator<B> ev = getEvaluator();
		if (ev.isWinLooseScore(value, maxDepth)) {
			final int matDepth = ev.getNbHalfMovesToWin(value);
			return ev.getWinScore(maxDepth-matDepth);
		} else {
			return value;
		}
	}
}
