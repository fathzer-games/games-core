package com.fathzer.games.ai.transposition;

import java.util.function.IntUnaryOperator;

import com.fathzer.games.ai.AlphaBetaState;

public class BasicPolicy<M> implements TranspositionTablePolicy<M> {
	@Override
	public AlphaBetaState<M> accept(TranspositionTableEntry<M> entry, int depth, int alpha, int beta, IntUnaryOperator fromTTScoreConverter) {
		final AlphaBetaState<M> state = new AlphaBetaState<>(depth, alpha, beta);
    	if (entry!=null && entry.isValid() && EntryType.EXACT==entry.getEntryType()) {
    		if (entry.getDepth()>=depth) {
    			state.setValue(fromTTScoreConverter.applyAsInt(entry.getValue()));
    		}
    		state.setBestMove(entry.getMove());
    	}
		return state;
	}
	
	@Override
	public boolean store(TranspositionTable<M> table, long key, AlphaBetaState<M> state, IntUnaryOperator toTTScoreConverter) {
    	// Update the transposition table
    	if (state.getValue()>state.getAlpha() && state.getValue()<state.getBetaUpdated()) {
   			return table.store(key, EntryType.EXACT, state.getDepth(), toTTScoreConverter.applyAsInt(state.getValue()), state.getBestMove(), p -> !p.isValid() || state.getDepth()>=p.getDepth());
    	}
    	return false;
	}
}
