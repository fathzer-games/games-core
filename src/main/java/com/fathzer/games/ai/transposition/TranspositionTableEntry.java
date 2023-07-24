package com.fathzer.games.ai.transposition;

/** A <a href="https://en.wikipedia.org/wiki/Transposition_table">transposition table</a> entry.
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
	EntryType getEntryType();
	int getDepth();
	int getValue();
	M getMove();
}
