package com.fathzer.games.ai.transposition;

import static com.fathzer.games.ai.transposition.EntryType.*;

import java.util.function.IntUnaryOperator;

import com.fathzer.games.MoveGenerator;
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
public class BasicPolicy<M, B extends MoveGenerator<M>> implements TranspositionTablePolicy<M, B> {
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

	/** Processes an non exact transposition table entry.
	 * <br>This method is called by {@link #accept(TranspositionTableEntry, int, int, int, IntUnaryOperator)} when entry is not null and it is not invalid and its type is not EXACT.
	 * @param entry The entry
	 * @param alpha The current alpha value
	 * @param beta The current beta value
	 * @param value The value stored in the entry, converted by the <code>toTTScoreConverter</code> function passed to the {@link #accept(TranspositionTableEntry, int, int, int, IntUnaryOperator)} method.
	 * @param state The state to update (this state will be returned by @link #accept(TranspositionTableEntry, int, int, int, IntUnaryOperator)).
	 */
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
	public boolean store(TranspositionTable<M, B> table, long key, AlphaBetaState<M> state, IntUnaryOperator toTTScoreConverter) {
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

	/** Checks whether an entry should be replaced by new data.
	 * @param entry The entry that is currently in the transposition table
	 * @param newKey The new entry key
	 * @param newDepth The new entry depth
	 * @param newType The new entry type
	 * @return true if the entry should be replaced, false otherwise. The default implementation returns true if:<ul>
	 * <li>of course, the current entry is invalid</li>
	 * <li>the new entry is exact and the current entry is not exact</li>
	 * <li>the current entry and the new depth is &gt; the current depth</li>
	 * </ul>
	 */
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
