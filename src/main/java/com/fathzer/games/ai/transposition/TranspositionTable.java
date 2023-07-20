package com.fathzer.games.ai.transposition;

/** A <a href="https://en.wikipedia.org/wiki/Transposition_table">transposition table</a>.
 * @param <E> The class that represents a Table entry
 */
public interface TranspositionTable<E extends TranspositionTableEntry> {
	/** Get a table entry.
	 * @param The key
	 * @return a table entry or null if this position should not be stored in transposition table.
	 * If the key does not exists yet, but should be stored later, it returns a entry with valid flag set to false.
	 * Typically, a fake table, that never saves anything will return null.
	 */
	E get(long key);
	
	/** Sets a key entry.
	 * <br>If a key already exists in the same table slot, it is replaced or not depending on the table implementation.
	 * @param entry An entry
	 */
	void update(E entry);
}
