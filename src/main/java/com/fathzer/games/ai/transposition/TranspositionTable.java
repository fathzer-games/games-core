package com.fathzer.games.ai.transposition;

/** A <a href="https://en.wikipedia.org/wiki/Transposition_table">transposition table</a>.
 */
public interface TranspositionTable<M> {
	/** Get a table entry.
	 * @param The key
	 * @return a table entry or null if this position should not be stored in transposition table.
	 * If the key does not exists yet, but should be stored later, it returns a entry with valid flag set to false.
	 * Typically, a fake table, that never saves anything will return null.
	 */
	TranspositionTableEntry<M> get(long key);
	
	/** Sets a key entry.
	 * <br>If a key already exists in the same table slot, it is replaced or not depending on the table implementation.
	 * @param key The entry's key
	 * @param type The entry's type
	 * @param depth The search depth at which the entry is stored
	 * @param value The entry's value
	 * @param move The entry's move 
	 */
	void store(long key, EntryType type, int depth, int value, M move);
	
	/** Tests if old entry should be kept when a new one is available.
	 * <br>This method can be used by {@link store} method to decide if entry should replace an existing one.
	 * @param old The entry previously in the table
	 * @param type The entry's type
	 * @param depth The search depth at which the entry is stored
	 * @param value The entry's value
	 * @param move The entry's move 
	 * @return false if the old entry should be discarded and replaced by the new one.
	 * <br>The default implementation returns true if depth of old entry is lower than depth of new one. 
	 */
	default boolean keep(TranspositionTableEntry<M> old, EntryType type, int depth, int value, M move) {
		return old.getDepth()<depth;
	}
	
	/** Called when position changes.
	 * <br>On this event, the table can clean itself, or increment a generation counter used in its own implementation of {@link #keep} method.
	 */
	void newPosition();
}
