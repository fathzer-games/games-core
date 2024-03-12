package com.fathzer.games.ai.transposition;

import static com.fathzer.games.ai.transposition.EntryType.*;

import java.util.function.IntUnaryOperator;

import com.fathzer.games.ai.AlphaBetaState;

/** A basic transposition table policy that records exact, lower or upper score and best/cut.
 * <br>It restores all best moves, exact, lower and upper values records recorded at a higher depth (closer to the root of evaluation)
 * (see <a href="https://en.wikipedia.org/wiki/Negamax#Negamax_with_alpha_beta_pruning">here</a>).
 * <br>Here are the overwrite rules:<ul>
 * <li>Never replace exact entries by inexact ones</li>
 * <li>Always replace inexact entries by exact ones</li>
 * <li>In other cases, replace entries if new one has higher depth</li>
 * </ul>
 * @param <M> The type of moves
 */
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

	protected void acceptNonExactRecord(TranspositionTableEntry<M> entry, int alpha, int beta, final int value, final AlphaBetaState<M> state) {
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
		return table.store(key, type, state.getDepth(), toTTScoreConverter.applyAsInt(state.getValue()), state.getBestMove(), p-> shouldReplace(p, key, state.getDepth(), type));
	}

	protected boolean shouldReplace(TranspositionTableEntry<M> entry, long newKey, int newDepth, EntryType newType) {
		if (!entry.isValid()) {
			// Always write if no entry is in the table
			return true;
		}
		if (entry.getEntryType()==EXACT && newType!=EXACT) {
			// Never replace exact by non exact
			return false;
		}
		if (newType==EXACT && entry.getEntryType()!=EXACT) {
			// Always replace non exact by exact
			return true;
		}
		// replace lower depth entries by higher depth
		return newDepth>entry.getDepth();
	}
}
