package com.fathzer.games.ai.transposition;

import com.fathzer.games.ai.AlphaBetaState;

public class BasicPolicy<M> implements TranspositionTablePolicy<M> {
	@Override
	public AlphaBetaState accept(TranspositionTableEntry<M> entry, int depth, int alpha, int beta) {
		final AlphaBetaState state = new AlphaBetaState(depth, alpha, beta);
    	if (entry!=null && entry.isValid() && entry.getDepth()>=depth && EntryType.EXACT==entry.getEntryType()) {
    		state.setValue(entry.getValue());
    	}
		return state;
	}
	
	@Override
	public void store(TranspositionTable<M> table, long key, AlphaBetaState state, M move) {
    	// Update the transposition table
    	if (state.getValue()>state.getAlpha() && state.getValue()<state.getBetaUpdated()) {
   			table.store(key, EntryType.EXACT, state.getDepth(), state.getValue(), move, p -> !p.isValid() || state.getDepth()>=p.getDepth());
    	}
	}
}
