package com.fathzer.games.ai.experimental;

import java.util.List;

import static com.fathzer.games.ai.experimental.Spy.Event.*;

import com.fathzer.games.Status;
import com.fathzer.games.HashProvider;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.AlphaBetaState;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.ai.transposition.EntryType;
import com.fathzer.games.ai.transposition.TranspositionTableEntry;

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
	protected Integer rootEvaluation(M move, final int depth, int lowestInterestingScore) {
    	if (lowestInterestingScore==Integer.MIN_VALUE) {
    		// WARNING: -Integer.MIN_VALUE is equals to ... Integer.MIN_VALUE
    		// So using it as alpha value makes negamax fail 
    		lowestInterestingScore += 1;
    	}
    	final B moveGenerator = getGamePosition();
//System.out.println("Play move "+move+" at depth "+depth+" for "+1);
        final TreeSearchStateStack<M,B> stack = new TreeSearchStateStack<>(moveGenerator, depth);
        stack.init(stack.getCurrent(), lowestInterestingScore, Integer.MAX_VALUE);
        if (stack.makeMove(move)) {
	        getStatistics().movePlayed();
	    	final int score = -negamax(stack);
	        stack.unmakeMove();
	        return score;
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
		final TreeSearchState<M> searchState = searchStack.getCurrent();
		if (searchState.depth == 0 || isInterrupted()) {
			getStatistics().evaluationDone();
			searchState.value = searchState.who * getEvaluator().evaluate(searchStack.position);
			spy.exit(searchStack, EVAL);
			return searchState.value;
		}
		
		final boolean keyProvider = (searchStack.position instanceof HashProvider) && getTranspositionTable()!=null;
		final long key;
		final AlphaBetaState<M> state;
		if (keyProvider) {
			key = ((HashProvider)searchStack.position).getHashKey();
			TranspositionTableEntry<M> entry = getTranspositionTable().get(key);
			state = getTranspositionTable().getPolicy().accept(entry, searchState.depth, searchState.alphaOrigin, searchState.betaOrigin, v -> scoreToTT(v, searchState.depth, searchStack.maxDepth, getEvaluator()));
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

		final List<M> moves = searchStack.position.getMoves();
		getStatistics().movesGenerated(moves.size());
		if (state != null) {
			insert(state.getBestMove(), moves);
		}
		boolean noValidMove = true;
		for (M move : moves) {
			if (searchStack.makeMove(move)) {
				noValidMove = false;
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
		}

		if (noValidMove) {
			// Player can't move it's a draw or a loose
			//TODO Maybe there's some games where the player wins if it can't move...
			searchState.value = searchStack.position.onNoValidMove() == Status.DRAW ? 0
					: -getEvaluator().getWinScore(searchStack.maxDepth - searchState.depth);
			if (searchState.value > searchState.alpha) {
				searchState.alpha = searchState.value;
			}
		}

		if (keyProvider && !isInterrupted()) {
			// If a transposition table is available
			state.setValue(searchState.value);
			state.updateAlphaBeta(searchState.alpha, searchState.beta);
			state.setBestMove(searchState.bestMove);
			boolean store = getTranspositionTable().getPolicy().store(getTranspositionTable(), key, state, v -> ttToScore(v, searchState.depth, searchStack.maxDepth, getEvaluator()));
			spy.storeTT(searchStack, state, store);
		}

		if (noValidMove) {
			spy.exit(searchStack, searchState.value == 0 ? DRAW : MAT);
		} else {
			spy.exit(searchStack, EXIT);
		}
		return searchState.value;
    }

	public void setSpy(Spy<M,B> spy) {
		this.spy = spy;
	}
}
