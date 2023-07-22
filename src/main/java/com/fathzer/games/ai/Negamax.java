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
	
	protected void cut(M move, int alpha, int beta, int score, int depth) {
//		System.out.println ("alpha cut on "+move+"at depth "+depth+" with score="+score+" (alpha is "+alpha+")");
	}
	
    protected int negamax(final int depth, int maxDepth, int alpha, int beta, final int who) {
    	if (depth == 0 || isInterrupted()) {
//System.out.println("Evaluation: "+context.evaluate()+" * "+who);
            return who * evaluate();
        }
    	final Status status = getMoveGenerator().getStatus();
		if (Status.DRAW.equals(status)) {
			return 0;
		} else if (!Status.PLAYING.equals(status)){
			final int nbMoves = (maxDepth-depth+1)/2;
			// Player looses after nbMoves moves
			return -getWinScore(nbMoves);
		}
		
		final int alphaOrigin = alpha;
		final MoveGenerator<M> mg = getMoveGenerator();
		final TranspositionTableEntry entry = mg instanceof ZobristProvider ? getTranspositionTable().get(((ZobristProvider)mg).getZobristKey()) : null;
		// Let remember the key (entry may be changed by a side potential side effect of transposition table during subtree exploring).
		final long key = entry!=null ? entry.getKey() : 0;
		// entry is null if transposition table is not supported
		if (entry!=null && entry.isValid() && entry.getDepth()>=depth) {
			final EntryType entryType = entry.getEntryType();
			final int entryValue = entry.getValue();
			if (entryType==EntryType.EXACT) {
//System.out.println("Bingo");
				return entryValue;
//			} else if (entryType==EntryType.LOWER_BOUND) {
//				if (entryValue>alpha) {
//					alpha = entryValue;
//				}
//			} else if (entryType==EntryType.UPPER_BOUND && entryValue<beta) {
//				beta=entryValue;
			}
		}

		
    	final List<M> moves = getMoveGenerator().getMoves();
    	//TODO Remove move ordering responsability from move generator
        int value = Integer.MIN_VALUE;
        for (M move : moves) {
//System.out.println("Play move "+move+" at depth "+depth+" for "+1);
            getMoveGenerator().makeMove(move);
            final int score = -negamax(depth-1, maxDepth, -beta, -alpha, -who);
            getMoveGenerator().unmakeMove();
            if (score > value) {
                value = score;
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
        	// Warning, entry could have been modified by a tt side effect while exploring the subtree
			entry.setKey(key);
        	entry.setValue(value);
        	entry.setDepth(depth);
   		    if (value <= alphaOrigin) {
   		    	entry.setEntryType(EntryType.UPPER_BOUND);
   		    } else if (value >= beta) {
   		    	entry.setEntryType(EntryType.LOWER_BOUND);
   		    } else {
   		    	entry.setEntryType(EntryType.EXACT);
   		    }
        	// Update the transposition table
        	getTranspositionTable().store(entry);
        }
        return value;
    }
    
    protected abstract TranspositionTable getTranspositionTable();
}
