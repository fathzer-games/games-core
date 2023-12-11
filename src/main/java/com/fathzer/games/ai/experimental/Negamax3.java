package com.fathzer.games.ai.experimental;

import java.util.List;

import static com.fathzer.games.ai.experimental.Spy.Event.*;

import com.fathzer.games.Status;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.HashProvider;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.AlphaBetaState;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchContext;
import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.transposition.EntryType;
import com.fathzer.games.ai.transposition.TranspositionTableEntry;
import com.fathzer.games.util.exec.ExecutionContext;

/**
 * An experimental of a Negamax with alpha beta pruning implementation and transposition table.
 * <br>It has the same functionalities than {@link Negamax} except it allows easier search debugging at the price of a (little) performance loss.
 * @param <M> Implementation of the Move interface to use
 */
public class Negamax3<M,B extends MoveGenerator<M>> extends Negamax<M,B> {
    private Spy<M,B> spy = new Spy<M,B>() {};
    
	public Negamax3(ExecutionContext<SearchContext<M,B>> exec) {
		super(exec);
	}
	
	@Override
    public SearchResult<M> getBestMoves(SearchParameters params) {
		SearchResult<M> result = super.getBestMoves(params);
		final B gamePosition = getContext().getGamePosition();
		if ((gamePosition instanceof HashProvider) && getTranspositionTable()!=null && !isInterrupted()) {
			// Store best move info in table
			final EvaluatedMove<M> best = result.getList().get(0);
			getTranspositionTable().store(((HashProvider)gamePosition).getHashKey(), EntryType.EXACT, params.getDepth(), best.getScore(), best.getContent(), p->true);
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
//System.out.println("Play move "+move+" at depth "+depth+" for "+1);
        final TreeSearchStateStack<M,B> stack = new TreeSearchStateStack<>(getContext(), depth);
        stack.init(stack.getCurrent(), lowestInterestingScore, Integer.MAX_VALUE);
        if (stack.makeMove(move, MoveConfidence.UNSAFE)) {
	        getStatistics().movePlayed();
	    	final int score = -negamax(stack);
	        stack.unmakeMove();
	        spy.moveUnmade(stack, null, move);
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
		try {
			spy.enter(searchStack);
			final TreeSearchState<M> searchState = searchStack.getCurrent();
			final Evaluator<M, B> evaluator = searchStack.context.getEvaluator();
			if (searchState.depth == 0 || isInterrupted()) {
				getStatistics().evaluationDone();
				searchState.value = searchState.who * evaluator.evaluate(searchStack.context.getGamePosition());
				spy.exit(searchStack, EVAL);
				return searchState.value;
			}
	    	final Status fastAnalysisStatus = searchStack.context.getGamePosition().getContextualStatus();
	    	if (fastAnalysisStatus!=Status.PLAYING) {
				spy.exit(searchStack, END_GAME);
	    		return getScore(evaluator, fastAnalysisStatus, searchState.depth, searchStack.maxDepth);
	    	}
			
			final boolean keyProvider = (searchStack.context instanceof HashProvider) && getTranspositionTable()!=null;
			final long key;
			final AlphaBetaState<M> state;
			if (keyProvider) {
				key = ((HashProvider)searchStack.context).getHashKey();
				TranspositionTableEntry<M> entry = getTranspositionTable().get(key);
				state = getTranspositionTable().getPolicy().accept(entry, searchState.depth, searchState.alphaOrigin, searchState.betaOrigin, v -> ttToScore(v, searchState.depth, searchStack.maxDepth, evaluator));
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
	
	        boolean noValidMove = true;
	    	final M moveFromTT = state!=null ? state.getBestMove() : null;
	
			// Try move from TT
	    	boolean moveFromTTBreaks = false;
	    	if (moveFromTT!=null && searchStack.makeMove(moveFromTT, MoveConfidence.UNSAFE)) {
	        	noValidMove = false;
	    		getStatistics().moveFromTTPlayed();
				if (onValidMove(searchStack, null, moveFromTT)) {
					moveFromTTBreaks = true;
				}
	    	}
	    	if (!moveFromTTBreaks) {
	    		// Try other moves
				final List<M> moves = searchStack.context.getGamePosition().getMoves(false);
	        	spy.movesComputed(searchStack, moves);
				getStatistics().movesGenerated(moves.size());
				for (M move : moves) {
					if (searchStack.makeMove(move, MoveConfidence.PSEUDO_LEGAL)) {
						noValidMove = false;
						getStatistics().movePlayed();
						if (onValidMove(searchStack, moves, move)) {
							break;
						}
					}
				}
		
				if (noValidMove) {
					// Player can't move it's a draw or a loose
					//TODO Maybe there's some games where the player wins if it can't move...
					searchState.value = getScore(evaluator, searchStack.context.getGamePosition().getEndGameStatus(), searchState.depth, searchStack.maxDepth);
					if (searchState.value > searchState.alpha) {
						searchState.alpha = searchState.value;
					}
				}
	    	}
	
			if (keyProvider && !isInterrupted()) {
				// If a transposition table is available
				state.setValue(searchState.value);
				state.updateAlphaBeta(searchState.alpha, searchState.beta);
				state.setBestMove(searchState.bestMove);
				boolean store = getTranspositionTable().getPolicy().store(getTranspositionTable(), key, state, v -> scoreToTT(v, searchState.depth, searchStack.maxDepth, evaluator));
				spy.storeTT(searchStack, state, store);
			}
	
			if (noValidMove) {
				spy.exit(searchStack, END_GAME);
			} else {
				spy.exit(searchStack, EXIT);
			}
			return searchState.value;
		} catch (RuntimeException e) {
			spy.exception(searchStack, e);
			throw e;
		}
    }
	
	private boolean onValidMove(TreeSearchStateStack<M,B> searchStack, List<M> moves, M move) {
		final int score = -negamax(searchStack);
		searchStack.unmakeMove();
		spy.moveUnmade(searchStack, moves, move);

		final TreeSearchState<M> searchState = searchStack.getCurrent();
		if (score > searchState.value) {
			searchState.value = score;
			searchState.bestMove = move;
			if (score > searchState.alpha) {
				searchState.alpha = score;
				if (score >= searchState.beta) {
					spy.cut(searchStack, move);
					return true;
				}
			}
		}
		return false;
	}

	public void setSpy(Spy<M,B> spy) {
		this.spy = spy;
	}
}
