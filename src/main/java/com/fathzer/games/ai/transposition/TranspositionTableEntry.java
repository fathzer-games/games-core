package com.fathzer.games.ai.transposition;

/** A <a href="https://en.wikipedia.org/wiki/Transposition_table">transposition table</a> entry.
 * <br><b>Warning:</b> All getters have non specified results when called on an invalid entry.
 * @param <M> The type of moves
 */
public interface TranspositionTableEntry<M> {
	/** Tests if the entry is valid.
	 * @return true if the entry is present in the transposition table, false if there no entry for the key in the table.
	 */
	boolean isValid();
	
	/** Gets the entry's key.
	 * @return a long
	 */
	long getKey();
	
	
	/** Get's the entry's type.
	 * @return the entry type
	 */
	EntryType getEntryType();
	
	
	/** Get's the entry's depth.
	 * @return a positive or null integer
	 */
	int getDepth();
	
	
	/** Get's the entry's value.
	 * @return an integer
	 */
	int getValue();
	
	/** Get's the entry's move.
	 * @return a move
	 */
	M getMove();
}
