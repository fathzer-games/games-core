package com.fathzer.games.ai.transposition;

import java.util.function.IntUnaryOperator;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.AlphaBetaState;

/** A class that decide if a transposition table entry should be replaced or not.
 */
public interface TranspositionTablePolicy<M, B extends MoveGenerator<M>> {
    /** Processes a transposition table entry.
     * <br>This method is called before iterating on possible moves to use entry data in order to speed up the Negamax algorithm.
     * <br>One can override it to customize how transposition table entries are used.
     * <br>The default behaviour is to return the value of entries with {@link EntryType#EXACT} type and depth &gt;= the current negamax depth and ignore others.
     * @param entry The entry
     * @param depth The current depth
     * @param alpha The current alpha value
     * @param beta The current beta value
	 * @param fromTTScoreConverter A function that will convert the value stored in the table to the value effectively returned in this method's result.
	 * <br>This could seems strange because there's a lot of examples on the Internet that returns directly the stored value.
	 * But, unfortunately, this strategy does not work with win/loose score and recursive deepening. The <a href="https://github.com/maksimKorzh/chess_programming/blob/master/src/bbc/tt_search_mating_scores/TT_mate_scoring.txt">following text</a> explains the problem.
     * @return The state that should be applied. If a value is set, the search is stopped and the value is returned. If alpha or beta value are changed in returned instance, they are copied in calling search function.
     */
	AlphaBetaState<M> accept(TranspositionTableEntry<M> entry, int depth, int alpha, int beta, IntUnaryOperator fromTTScoreConverter);
	
	/** Updates the transposition table, if required, after iterating on possible moves.
	 * <br>This method is responsible for deciding if something should be stored and what should be stored.
	 * <br>It typically uses {@link TranspositionTable#store(long, EntryType, int, int, Object, java.util.function.Predicate)} method to store/update the entry 
	 * @param table The transposition table
	 * @param key The key where to store data
	 * @param state The state returned by {@link #accept(TranspositionTableEntry, int, int, int, IntUnaryOperator)} updated with alpha, beta and value
	 * @param toTTScoreConverter A function that will convert the state value to the value effectively stored in the table.
	 * <br>This could seems strange because there's a lot of examples on the Internet that stores directly the state value.
	 * But, unfortunately, this strategy does not work with win/loose score and recursive deepening. The <a href="https://github.com/maksimKorzh/chess_programming/blob/master/src/bbc/tt_search_mating_scores/TT_mate_scoring.txt">following text</a> explains the problem.
	 * @return true if state is stored, false if it is ignored
	 * @see TranspositionTable#store(long, EntryType, int, int, Object, java.util.function.Predicate)
	 */
	boolean store(TranspositionTable<M, B> table, long key, AlphaBetaState<M> state, IntUnaryOperator toTTScoreConverter);
}
