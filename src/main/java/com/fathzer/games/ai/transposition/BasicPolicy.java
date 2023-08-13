package com.fathzer.games.ai.transposition;

import static com.fathzer.games.ai.transposition.EntryType.*;

import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

import com.fathzer.games.ai.AlphaBetaState;

public class BasicPolicy<M> implements TranspositionTablePolicy<M> {
	@Override
	public AlphaBetaState<M> accept(TranspositionTableEntry<M> entry, int depth, int alpha, int beta, IntUnaryOperator fromTTScoreConverter) {
		final AlphaBetaState<M> state = new AlphaBetaState<>(depth, alpha, beta);
    	if (entry!=null && entry.isValid()) {
    		if (entry.getDepth()>=depth) {
				final int value = fromTTScoreConverter.applyAsInt(entry.getValue());
    			if (EXACT==entry.getEntryType()) {
					state.setValue(value);
    			} else {
    				acceptNonExactRecord(entry, alpha, beta, value, state);
    			}
    		}
    		state.setBestMove(entry.getMove());
    	}
		return state;
	}

	protected void acceptNonExactRecord(TranspositionTableEntry<M> entry, int alpha, int beta, final int value,
			final AlphaBetaState<M> state) {
		boolean updated = false;
		if (LOWER_BOUND==entry.getEntryType()) {
			updated = value>alpha;
			if (updated) {
				alpha = value;
			}
		} else if (UPPER_BOUND==entry.getEntryType()) {
			updated = value<beta;
			if (updated) {
				beta = value;
			}
		}
		if (updated) {
		    if (alpha >= beta) {
		        state.setValue(value);
		    } else {
		    	state.updateAlphaBeta(alpha, beta);
		    }
		}
	}
	
	@Override
	public boolean store(TranspositionTable<M> table, long key, AlphaBetaState<M> state, IntUnaryOperator toTTScoreConverter) {
    	final EntryType type;
    	if (state.getValue() <= state.getAlpha()) {
    		type = UPPER_BOUND;
    	} else if (state.getValue() >= state.getBetaUpdated()) {
    		type = LOWER_BOUND;
    	} else {
    		type = EXACT;
    	}
    	// Update the transposition table
		return table.store(key, type, state.getDepth(), toTTScoreConverter.applyAsInt(state.getValue()), state.getBestMove(), replacePredicate(state.getDepth()));
	}

	protected Predicate<TranspositionTableEntry<M>> replacePredicate(int depth) {
		return p -> !p.isValid() || depth>=p.getDepth();
	}
}
