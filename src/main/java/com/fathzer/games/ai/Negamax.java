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
    private final AlphaBetaState state = new AlphaBetaState();
    private TranspositionTable<M> transpositionTable;
    
	public Negamax(ExecutionContext<M> exec) {
		super(exec);
	}
	
	@Override
    public List<Evaluation<M>> getBestMoves(final int depth, int size, int accuracy) {
		List<Evaluation<M>> result = super.getBestMoves(depth, size, accuracy);
		if ((getGamePosition() instanceof HashProvider) && getTranspositionTable()!=null) {
			// Store best move info in table
			final Evaluation<M> best = result.get(0);
			getTranspositionTable().store(((HashProvider)getGamePosition()).getHashKey(), EntryType.EXACT, depth, best.getValue(), best.getContent());
		}
		return result;
    }

	@Override
    public List<Evaluation<M>> getBestMoves(final int depth, List<M> moves, int size, int accuracy) {
		return getBestMoves(depth, sort(moves), size, accuracy, (m,alpha)-> rootEvaluation(m,depth,alpha));
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
		
		final int alphaOrigin = alpha;
		final boolean keyProvider = (position instanceof HashProvider) && getTranspositionTable()!=null;
		final TranspositionTableEntry<M> entry;
		final long key;
		if (keyProvider) {
			key = ((HashProvider)position).getHashKey();
			synchronized(getTranspositionTable().getLock(key)) {
				entry = position instanceof HashProvider ? getTranspositionTable().get(((HashProvider)position).getHashKey()) : null;
				// entry is null if transposition table is not supported
				if (entry!=null && entry.isValid()) {
					state.set(depth, alpha, beta);
					Integer toBeReturned = processTranspositionTableEntry(entry, state);
					if (toBeReturned!=null) {
						return toBeReturned;
					} else {
						alpha = state.getAlpha();
						beta = state.getBeta();
					}
				}
			}
		} else {
			key = 0;
			entry = null;
		}


    	final List<M> moves = sort(position.getMoves());
        int value = Integer.MIN_VALUE;
        M bestMove = null;
        for (M move : moves) {
            position.makeMove(move);
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

        if (entry!=null) {
        	// If a transposition table is available
    		synchronized(getTranspositionTable().getLock(key)) {
    			toTranspositionTable(key, value, alphaOrigin, alpha, beta, depth, bestMove);
    		}
        }
        return value;
    }
    
    /** Process a transposition table entry.
     * <br>This method is called before iterating on possible moves to use entry data in order to speed up the Negamax algorithm.
     * <br>One can override it to customize how transposition table entries are used.
     * <br>The default behaviour is to return the value of entries with {@link EntryType#EXACT} type and depth &gt;= the current negamax depth and ignore others.
     * <br>Please note that the call of this method is synchronized on the {@link TranspositionTable#getLock(long)} object.
     * @param entry The entry
     * @param state The current negamax state
     * @return The value that should be returned without exploring the moves or null if moves exploration is required.
     * In such a case, you can change the alpha and beta values of the state passed as an argument.
     */
    protected Integer processTranspositionTableEntry(TranspositionTableEntry<M> entry, AlphaBetaState state) {
    	if (entry.getDepth()>=state.getDepth() && EntryType.EXACT==entry.getEntryType()) {
    		return entry.getValue();
    	} else {
    		return null;
    	}
    }
    
    /** Stores the results of possible moves exploration .
     * <br>This method is called after iterating on possible moves to store useful data in transposition table in order to speed up the Negamax algorithm.
     * <br>One can override it to customize how transposition table entries are used.
     * <br>The default behaviour is to store only {@link EntryType#EXACT} values.
     * <br>Please note that the call of this method is synchronized on the {@link TranspositionTable#getLock(long)} object.
     * @param key The key
     * @param value The returned value
     * @param move The move that corresponds to the value
     * @param alphaOrigin The alpha value passed to the {@link #negamax(int, int, int, int, int)} method.
     * @param alpha The alpha value.
     * @param beta The beta value
     * @param depth The current depth
     */
    protected void toTranspositionTable(long key, int value, int alphaOrigin, int alpha, int beta, int depth, M move) {
    	// Update the transposition table
    	if (value>alphaOrigin && value<beta) {
   			getTranspositionTable().store(key, EntryType.EXACT, depth, value, move);
    	}
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
}
