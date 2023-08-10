package com.fathzer.games.ai.experimental;

import java.util.List;

import static com.fathzer.games.ai.experimental.Spy.Event.*;

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
import com.fathzer.games.util.EvaluatedMove;

/**
 * A Negamax with alpha beta pruning implementation.
 * @param <M> Implementation of the Move interface to use
 */
public class Negamax3<M,B extends MoveGenerator<M>> extends Negamax<M,B> {
    private Spy<M,B> spy = new Spy<M,B>() {};
    
	public Negamax3(ExecutionContext<M,B> exec, Evaluator<B> evaluator) {
		super(exec, evaluator);
	}
	
	@Override
    public SearchResult<M> getBestMoves(SearchParameters params) {
		SearchResult<M> result = super.getBestMoves(params);
		if ((getGamePosition() instanceof HashProvider) && getTranspositionTable()!=null && !isInterrupted()) {
			// Store best move info in table
			final EvaluatedMove<M> best = result.getList().get(0);
			getTranspositionTable().store(((HashProvider)getGamePosition()).getHashKey(), EntryType.EXACT, params.getDepth(), best.getScore(), best.getContent(), p->true);
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
        final TreeSearchStateStack<M,B> stack = new TreeSearchStateStack<>(position, depth);
        stack.init(stack.getCurrent(), alpha, Integer.MAX_VALUE);
        stack.makeMove(move);
        getStatistics().movePlayed();
    	final int score = -negamax(stack);
        stack.unmakeMove();
        return score;
	}
	
	protected boolean getEndOfSearchScore (TreeSearchStateStack<M,B> stack) {
		final TreeSearchState<M> state = stack.getCurrent();
		if (state.depth == 0 || isInterrupted()) {
//System.out.println("Evaluation: "+context.evaluate()+" * "+who);
    		getStatistics().evaluationDone();
    		state.value = state.who * getEvaluator().evaluate(stack.position);
   			spy.exit(stack, EVAL);
			return true;
        }
    	final Status status = stack.position.getStatus();
		if (Status.DRAW.equals(status)) {
			state.value = 0;
			spy.exit(stack, DRAW);
			return true;
		} else if (!Status.PLAYING.equals(status)){
			// Player looses after nbMoves moves
            state.value = -getEvaluator().getWinScore(stack.maxDepth-state.depth);
           	spy.exit(stack, MAT);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
    protected int negamax(final int depth, int maxDepth, int alpha, int beta, final int who) {
		throw new IllegalStateException("Should not be called anymore");
	}
	
	protected int negamax(TreeSearchStateStack<M,B> searchStack) {
    	spy.enter(searchStack);
		final TreeSearchState<M> searchState = searchStack.getCurrent();
		if (getEndOfSearchScore(searchStack)) {
			return searchState.value;
		}
		
		final boolean keyProvider = (searchStack.position instanceof HashProvider) && getTranspositionTable()!=null;
		final long key;
		final AlphaBetaState<M> state;
		if (keyProvider) {
			key = ((HashProvider)searchStack.position).getHashKey();
			TranspositionTableEntry<M> entry = getTranspositionTable().get(key);
			state = getTranspositionTable().getPolicy().accept(entry, searchState.depth, searchState.alphaOrigin, searchState.betaOrigin, v -> fromTTScore(v, searchStack.maxDepth));
			if (state.isValueSet()) {
				searchState.value = state.getValue();
				spy.exit(searchStack, TT);
				return state.getValue();
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
        for (M move : moves) {
        	searchStack.makeMove(move);
            getStatistics().movePlayed();
            final int score = -negamax(searchStack);
            searchStack.unmakeMove();
            if (score > searchState.value) {
            	searchState.value = score;
                searchState.bestMove = move;
                if (score > searchState.alpha) {
                	searchState.alpha = score;
                    if (score >= searchState.beta) {
                   		spy.cut(searchStack, move);
                    	break;
                    }
                }
            }
        }
        
        if (keyProvider && !isInterrupted()) {
        	// If a transposition table is available
        	state.setValue(searchState.value);
        	state.updateAlphaBeta(searchState.alpha, searchState.beta);
        	state.setBestMove(searchState.bestMove);
        	boolean store = getTranspositionTable().getPolicy().store(getTranspositionTable(), key, state, v -> toTTScore(v,searchState.depth, searchStack.maxDepth));
       		spy.storeTT(searchStack, state, store);
        }

       	spy.exit(searchStack, EXIT);
        return searchState.value;
    }

	public void setSpy(Spy<M,B> spy) {
		this.spy = spy;
	}
}
