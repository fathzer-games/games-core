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
 * @param <M> Implementation of the Move interface to use
 */
public class Negamax<M> extends AbstractAI<M> implements MoveSorter<M> {
    private TranspositionTable<M> transpositionTable;
    
	public Negamax(ExecutionContext<M> exec) {
		super(exec);
	}
	
	@Override
    public SearchResult<M> getBestMoves(final int depth, int size, int accuracy) {
		SearchResult<M> result = super.getBestMoves(depth, size, accuracy);
		if ((getGamePosition() instanceof HashProvider) && transpositionTable!=null && !isInterrupted()) {
			// Store best move info in table
			final Evaluation<M> best = result.getList().get(0);
			transpositionTable.store(((HashProvider)getGamePosition()).getHashKey(), EntryType.EXACT, depth, best.getValue(), best.getContent(), p->true);
		}
		return result;
    }

	@Override
    public SearchResult<M> getBestMoves(List<M> moves, int depth, int size, int accuracy) {
		return getBestMoves(depth, moves, size, accuracy, (m,alpha)-> rootEvaluation(m,depth,alpha));
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
	
    protected int negamax(final int depth, int maxDepth, int alpha, int beta, final int who) {
		final GamePosition<M> position = getGamePosition();
    	if (depth == 0 || isInterrupted()) {
//System.out.println("Evaluation: "+context.evaluate()+" * "+who);
    		getStatistics().evaluationDone();
            return who * position.evaluate();
        }
    	final Status status = position.getStatus();
		if (Status.DRAW.equals(status)) {
			return 0;
		} else if (!Status.PLAYING.equals(status)){
			final int nbMoves = (maxDepth-depth+1)/2;
			// Player looses after nbMoves moves
			return -position.getWinScore(nbMoves);
		}
		
		final boolean keyProvider = (position instanceof HashProvider) && transpositionTable!=null;
		final long key;
		final AlphaBetaState<M> state;
		if (keyProvider) {
			key = ((HashProvider)position).getHashKey();
			TranspositionTableEntry<M> entry = position instanceof HashProvider ? transpositionTable.get(((HashProvider)position).getHashKey()) : null;
			state = transpositionTable.getPolicy().accept(entry, depth, alpha, beta);
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

    	final List<M> moves = sort(state==null?null:state.getBestMove(), position.getMoves());
//    	final List<M> moves = sort(position.getMoves());
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
        	transpositionTable.getPolicy().store(transpositionTable, key, state);
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
//System.out.println("Here we are with "+best);
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
}
