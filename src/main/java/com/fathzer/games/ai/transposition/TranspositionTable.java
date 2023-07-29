package com.fathzer.games.ai.transposition;

import java.util.ArrayList;
import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.HashProvider;

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
	
	/** Gets the lock to use in order to synchronize access to data attached to a key.
	 * @param The key
	 * @return an object to synchronize on. THe default implementation returns this.
	 */
	default Object getLock(long key) {
		return this;
	}
	
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
	 * <br>The default implementation returns true if depth of old entry is higher than depth of new one (remember depth of tree leaves is 0). 
	 */
	default boolean keep(TranspositionTableEntry<M> old, EntryType type, int depth, int value, M move) {
		return old.getDepth()>depth;
	}
	
	/** Called when position changes.
	 * <br>On this event, the table can clean itself, or increment a generation counter used in its own implementation of {@link #keep} method.
	 */
	void newPosition();
	
	/**
	 * Collects the principal variation starting from the position on the board
	 * <br>Warning, this method should not be called during table modification.
	 * @param board The position to collect pv from.
	 * <br>The move generator should implement the {@link HashProvider} interface.
	 * @param maxDepth How deep the pv goes (avoids situations where keys point to
	 *            each other infinitely)
	 * @return The moves
	 */
	default List<M> collectPV(MoveGenerator<M> board, int maxDepth) {
		final HashProvider zp = (HashProvider)board; 
		final List<M> arrayPV = new ArrayList<>(maxDepth);
		TranspositionTableEntry<M> entry = get(zp.getHashKey());

		for (int i=0;i<maxDepth;i++) {
			//FIXME Be aware of zobrist key collisions that should make move is not possible
			M move = entry!=null && entry.isValid() ? entry.getMove() : null;
			if (move==null /*|| !board.isValid(move)*/) {
				break;
			}
			arrayPV.add(move);
			board.makeMove(move);
			entry = get(zp.getHashKey());
		}

		// Unmake the moves
		for (int i=0;i<arrayPV.size();i++) {
			board.unmakeMove();
		}
		return arrayPV;
	}
}
