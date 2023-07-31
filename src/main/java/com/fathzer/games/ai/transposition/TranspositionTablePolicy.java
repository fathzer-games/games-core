package com.fathzer.games.ai.transposition;

import com.fathzer.games.ai.AlphaBetaState;

/** A class that decide if a transposition table entry should be replaced or not.
 */
public interface TranspositionTablePolicy<M> {
    /** Process a transposition table entry.
     * <br>This method is called before iterating on possible moves to use entry data in order to speed up the Negamax algorithm.
     * <br>One can override it to customize how transposition table entries are used.
     * <br>The default behaviour is to return the value of entries with {@link EntryType#EXACT} type and depth &gt;= the current negamax depth and ignore others.
     * <br>Please note that the call of this method is synchronized on the {@link TranspositionTable#getLock(long)} object.
     * @param entry The entry
     * @param depth The current depth
     * @param alpha The current alpha value
     * @param beta The current beta value
     * @return The state that should be applied. If a value is set, the search is stopped and the value is returned. If alpha or beta value are changed in returned instance, they are copied in calling search function.
     */
	AlphaBetaState accept(TranspositionTableEntry<M> entry, int depth, int alpha, int beta);
	
	
	/** Updates the transposition table, if required, after iterating on possible moves.
	 * <br>This method is responsible for deciding if something should be stored and what should be stored.
	 * <br>It typically uses {@link TranspositionTable#store(long, EntryType, int, int, Object, java.util.function.Predicate)} method to store/update the entry 
	 * @param table The transposition table
	 * @param key The key where to store data
	 * @param state The state returned by {@link #accept(TranspositionTableEntry, int, int, int)} updated with alpha, beta and value
	 * @param move The entry's move
	 * @see TranspositionTable#store(long, EntryType, int, int, Object, java.util.function.Predicate)
	 */
	void store(TranspositionTable<M> table, long key, AlphaBetaState state, M move);
}
