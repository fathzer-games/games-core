package com.fathzer.games.ai.experimental;

import java.util.List;

import com.fathzer.games.Status;
import com.fathzer.games.HashProvider;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.AlphaBetaState;
import com.fathzer.games.ai.Evaluator;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.ai.transposition.EntryType;
import com.fathzer.games.ai.transposition.TranspositionTableEntry;
import com.fathzer.games.util.Evaluation;

/**
 * A Negamax with alpha beta pruning implementation.
 * @param <M> Implementation of the Move interface to use
 */
public class Negamax3<M,B extends MoveGenerator<M>> extends Negamax<M,B> {
//TODO Remove?
	public enum Event {EVAL, MAT, DRAW, EXIT, TT}
    public interface Spy<M, B extends MoveGenerator<M>> {
    	default void enter(TreeSearchStateStack<M,B> state) {}
    	default void alphaBetaFromTT(TreeSearchStateStack<M,B> state, AlphaBetaState<M> abState) {}
    	default void storeTT(TreeSearchStateStack<M,B> state, AlphaBetaState<M> abState, boolean store) {}
    	default void cut(TreeSearchStateStack<M,B> state, M move, int score) {}
    	default void exit(TreeSearchStateStack<M,B> state, Event evt, int value) {}
    }
    public Spy<M,B> spy = new Spy<M,B>() {};
//TODO
    
	public Negamax3(ExecutionContext<M,B> exec, Evaluator<B> evaluator) {
		super(exec, evaluator);
	}
	
	@Override
    public SearchResult<M> getBestMoves(SearchParameters params) {
		SearchResult<M> result = super.getBestMoves(params);
		if ((getGamePosition() instanceof HashProvider) && getTranspositionTable()!=null && !isInterrupted()) {
			// Store best move info in table
			final Evaluation<M> best = result.getList().get(0);
			getTranspositionTable().store(((HashProvider)getGamePosition()).getHashKey(), EntryType.EXACT, params.getDepth(), best.getValue(), best.getContent(), p->true);
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
    	final B position = getGamePosition();
//System.out.println("Play move "+move+" at depth "+depth+" for "+1);
        position.makeMove(move);
        getStatistics().movePlayed();
        final TreeSearchStateStack<M,B> stack = new TreeSearchStateStack<>(position, depth);
        stack.init(stack.get(stack.getCurrentDepth()), -Integer.MAX_VALUE, -alpha);
    	final int score = -negamax(stack);
        position.unmakeMove();
        return score;
	}
	
	protected Integer getEndOfSearchScore (TreeSearchStateStack<M,B> stack) {
		final TreeSearchState<M> state = stack.get(stack.getCurrentDepth());
		if (state.depth == 0 || isInterrupted()) {
//System.out.println("Evaluation: "+context.evaluate()+" * "+who);
    		getStatistics().evaluationDone();
    		final int eval = state.who * getEvaluator().evaluate(stack.position);
   			spy.exit(stack, Event.EVAL, eval);
			return eval;
        }
    	final Status status = stack.position.getStatus();
		if (Status.DRAW.equals(status)) {
			spy.exit(stack, Event.DRAW, 0);
			return 0;
		} else if (!Status.PLAYING.equals(status)){
			// Player looses after nbMoves moves
            final int matScore = -getEvaluator().getWinScore(stack.maxDepth-state.depth);
           	spy.exit(stack, Event.MAT, matScore);
			return matScore;
		} else {
			return null;
		}
	}
	
	@Override
    protected int negamax(final int depth, int maxDepth, int alpha, int beta, final int who) {
		throw new IllegalStateException("Should not be called anymore");
	}
	
	protected int negamax(TreeSearchStateStack<M,B> searchStack) {
    	spy.enter(searchStack);
		Integer endOfSearchScore = getEndOfSearchScore(searchStack);
		if (endOfSearchScore!=null) {
			return endOfSearchScore;
		}
		final TreeSearchState<M> searchState = searchStack.get(searchStack.getCurrentDepth());
		
		final boolean keyProvider = (searchStack.position instanceof HashProvider) && getTranspositionTable()!=null;
		final long key;
		final AlphaBetaState<M> state;
		if (keyProvider) {
			key = ((HashProvider)searchStack.position).getHashKey();
			TranspositionTableEntry<M> entry = getTranspositionTable().get(key);
			state = getTranspositionTable().getPolicy().accept(entry, searchState.depth, searchState.alphaOrigin, searchState.betaOrigin);
			if (state.isValueSet()) {
				spy.exit(searchStack, Event.TT, state.getValue());
				return fromTTScore(state.getValue(), searchStack.maxDepth);
			} else if (state.isAlphaBetaUpdated()) {
				spy.alphaBetaFromTT(searchStack, state);
				searchState.alpha = state.getAlphaUpdated();
				searchState.beta = state.getBetaUpdated();
			}
		} else {
			key = 0;
			state = null;
		}

		final List<M> moves = sort(state==null?null:state.getBestMove(), searchStack.position.getMoves());
    	getStatistics().movesGenerated(moves.size());
        int value = -Integer.MAX_VALUE;
        M bestMove = null;
        for (M move : moves) {
        	searchStack.makeMove(move);
            getStatistics().movePlayed();
            final int score = -negamax(searchStack);
            searchStack.unmakeMove();
            if (score > value) {
                value = score;
                bestMove = move;
                if (value > searchState.alpha) {
                	searchState.alpha = value;
                    if (searchState.alpha >= searchState.beta) {
                   		spy.cut(searchStack, move, score);
                    	break;
                    }
                }
            }
        }
        
        if (keyProvider && !isInterrupted()) {
        	// If a transposition table is available
        	state.setValue(toTTScore(value,searchState.depth, searchStack.maxDepth));
        	state.updateAlphaBeta(searchState.alpha, searchState.beta);
        	state.setBestMove(bestMove);
        	boolean store = getTranspositionTable().getPolicy().store(getTranspositionTable(), key, state);
       		spy.storeTT(searchStack, state, store);
        }

       	spy.exit(searchStack, Event.EXIT, value);
        return value;
    }
}
