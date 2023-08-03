package com.fathzer.games.ai.transposition;

import com.fathzer.games.ai.AlphaBetaState;

public class BasicPolicy<M> implements TranspositionTablePolicy<M> {
	@Override
	public AlphaBetaState<M> accept(TranspositionTableEntry<M> entry, int depth, int alpha, int beta) {
		final AlphaBetaState<M> state = new AlphaBetaState<>(depth, alpha, beta);
    	if (entry!=null && entry.isValid() && EntryType.EXACT==entry.getEntryType()) {
    		if (entry.getDepth()>=depth) {
    			state.setValue(entry.getValue());
    		}
    		state.setBestMove(entry.getMove());
    	}
		return state;
	}
	
	@Override
	public void store(TranspositionTable<M> table, long key, AlphaBetaState<M> state) {
    	// Update the transposition table
    	if (state.getValue()>state.getAlpha() && state.getValue()<state.getBetaUpdated()) {
   			table.store(key, EntryType.EXACT, state.getDepth(), state.getValue(), state.getBestMove(), p -> !p.isValid() || state.getDepth()>=p.getDepth());
    	}
	}
}
