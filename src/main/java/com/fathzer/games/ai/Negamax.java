package com.fathzer.games.ai;

import java.util.Iterator;
import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;
import com.fathzer.games.ZobristProvider;
import com.fathzer.games.ai.transposition.EntryType;
import com.fathzer.games.ai.transposition.TranspositionTable;
import com.fathzer.games.ai.transposition.TranspositionTableEntry;
import com.fathzer.games.util.Evaluation;

/**
 * A Negamax with alpha beta pruning implementation.
 * @param <M> Implementation of the Move interface to use
 */
public abstract class Negamax<M> extends AbstractAI<M> {
    private final AlphaBetaState state = new AlphaBetaState();
	
	protected Negamax(MoveGenerator<M> moveGenerator) {
		super(moveGenerator);
	}

	@Override
    public List<Evaluation<M>> getBestMoves(final int depth, List<M> moves, int size, int accuracy) {
		return getBestMoves(depth, moves, size, accuracy, (c,alpha)-> get(c,depth,alpha));
    }

	private int get(Iterator<M> moves, final int depth, int alpha) {
    	if (alpha==Integer.MIN_VALUE) {
    		// WARNING: -Integer.MIN_VALUE is equals to ... Integer.MIN_VALUE
    		// So using it as alpha value makes negamax fail 
    		alpha += 1;
    	}
        M move = moves.next();
//System.out.println("Play move "+move+" at depth "+depth+" for "+1);
        getMoveGenerator().makeMove(move);
        final int score = -negamax(depth-1, depth, -Integer.MAX_VALUE, -alpha, -1);
        getMoveGenerator().unmakeMove();
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
//		System.out.println ("alpha cut on "+move+"at depth "+depth+" with score="+score+" (alpha is "+alpha+")");
	}
	
    protected int negamax(final int depth, int maxDepth, int alpha, int beta, final int who) {
    	if (depth == 0 || isInterrupted()) {
//System.out.println("Evaluation: "+context.evaluate()+" * "+who);
            return who * evaluate();
        }
		final MoveGenerator<M> mg = getMoveGenerator();
    	final Status status = mg.getStatus();
		if (Status.DRAW.equals(status)) {
			return 0;
		} else if (!Status.PLAYING.equals(status)){
			final int nbMoves = (maxDepth-depth+1)/2;
			// Player looses after nbMoves moves
			return -getWinScore(nbMoves);
		}
		
		final int alphaOrigin = alpha;
		final boolean keyProvider = mg instanceof ZobristProvider;
		final TranspositionTableEntry<M> entry;
		final long key;
		if (keyProvider) {
			key = ((ZobristProvider)mg).getZobristKey();
			synchronized(getTranspositionTable().getLock(key)) {
				entry = mg instanceof ZobristProvider ? getTranspositionTable().get(((ZobristProvider)mg).getZobristKey()) : null;
				// entry is null if transposition table is not supported
				if (entry!=null && entry.isValid()) {
					state.set(depth, alpha, beta);
					Integer toBeReturned = process(entry, state);
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


    	final List<M> moves = mg.getMoves();
    	//TODO Remove move ordering responsibility from move generator
        int value = Integer.MIN_VALUE;
        M bestMove = null;
        for (M move : moves) {
            mg.makeMove(move);
            final int score = -negamax(depth-1, maxDepth, -beta, -alpha, -who);
            mg.unmakeMove();
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
    protected Integer process(TranspositionTableEntry<M> entry, AlphaBetaState state) {
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
    protected abstract TranspositionTable<M> getTranspositionTable();
}
