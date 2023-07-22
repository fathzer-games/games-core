package com.fathzer.games.ai.transposition;

/** A <a href="https://en.wikipedia.org/wiki/Transposition_table">transposition table</a>.
 */
public interface TranspositionTable {
	/** Get a table entry.
	 * @param The key
	 * @return a table entry or null if this position should not be stored in transposition table.
	 * If the key does not exists yet, but should be stored later, it returns a entry with valid flag set to false.
	 * Typically, a fake table, that never saves anything will return null.
	 */
	TranspositionTableEntry get(long key);
	
	/** Sets a key entry.
	 * <br>If a key already exists in the same table slot, it is replaced or not depending on the table implementation.
	 * @param entry An entry
	 */
	void store(TranspositionTableEntry entry);
	
	/** Tests if old entry should be kept when a new one is available.
	 * <br>This method can be used by {@link store} method to decide if entry should replace an existing one.
	 * @param old The entry previously in the table
	 * @param newOne The new entry.
	 * @return false if the old entry should be discarded and replaced by the new one.
	 * <br>The default implementation returns true if depth of old entry is lower than depth of new one. 
	 */
	default boolean keep(TranspositionTableEntry old, TranspositionTableEntry newOne) {
		return old.getDepth()<newOne.getDepth();
	}
	
	/** Called when position changes.
	 * <br>On this event, the table can clean itself, or increment a generation counter used in its own implementation of {@link #keep} method.
	 */
	void newPosition();
}
