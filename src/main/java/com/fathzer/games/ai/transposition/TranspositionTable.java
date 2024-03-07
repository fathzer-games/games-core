package com.fathzer.games.ai.transposition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.HashProvider;

/** A <a href="https://en.wikipedia.org/wiki/Transposition_table">transposition table</a>.
 * <br>Implementations of this interface should be thread safe.
 */
public interface TranspositionTable<M> {
	/** Get a table entry.
	 * @param key The key
	 * @return a table entry or null if this position should not be stored in transposition table.
	 * If the key does not exists yet, but should be stored later, it returns a entry with valid flag set to false.
	 * Typically, a fake table, that never saves anything will return null.
	 */
	TranspositionTableEntry<M> get(long key);
	
	/** Sets a key entry.
	 * <br>If a key already exists in the same table slot, it is replaced.
	 * @param key The entry's key
	 * @param type The entry's type
	 * @param depth The search depth at which the entry is stored
	 * @param value The entry's value
	 * @param move The entry's move
	 * @param validator a predicate that returns true if the previous entry which is passed to the predicate should be replaced
 	 * @return true if state is stored, false if it is ignored
	 */
	boolean store(long key, EntryType type, int depth, int value, M move, Predicate<TranspositionTableEntry<M>> validator);
	
	/** Called when position changes.
	 * <br>On this event, the table can clean itself, or in a future release increment a generation counter in Entry generation.
	 */
	void newPosition(); //TODO Change comment when generation will be used in TableEntry
	
	void newGame(); //TODO Should call transpositionTablePolicy
	
	/** Gets the transposition table's policy.
	 * <br>The policy decides what should be stored in the table and how to use it in the search algorithm.
	 * @return
	 */
	TranspositionTablePolicy<M> getPolicy();
	
	/** Sets the transposition table's policy.
	 * @param policy The policy decides what should be stored in the table and how to use it in the search algorithm.
	 */
	void setPolicy(TranspositionTablePolicy<M> policy);
	
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
			M move = entry!=null && entry.isValid() ? entry.getMove() : null;
			if (move!=null && board.makeMove(move, MoveConfidence.UNSAFE)) {
				arrayPV.add(move);
				entry = get(zp.getHashKey());
			} else {
				break;
			}
		}

		// Unmake the moves
		for (int i=0;i<arrayPV.size();i++) {
			board.unmakeMove();
		}
		return arrayPV;
	}
	
	/**
	 * Collects the principal variation starting from the position on the board after a move
	 * <br>Warning, this method should not be called during table modification.
	 * @param board The position to collect pv from.
	 * @param move The move to play (if move is not a valid move, result is not specified).
	 * <br>The move generator should implement the {@link HashProvider} interface.
	 * @param maxDepth How deep the pv goes (avoids situations where keys point to
	 *            each other infinitely)
	 * @return The moves
	 */
	default List<M> collectPV(MoveGenerator<M> board, M move, int maxDepth) {
		if (board.makeMove(move, MoveConfidence.UNSAFE)) {
			try {
				final List<M> result = collectPV(board, maxDepth-1);
				result.add(0, move);
				return result;
			} finally {
				board.unmakeMove();
			}
		} else {
			throw new IllegalArgumentException("Move is not legal");
		}
	}
	
	/** Gets the maximum entry count in the table.
	 * @return an integer
	 */
	int getSize();
	
	/** Gets the table table's memory size expressed in MBytes.
	 * @return an integer
	 */
	int getMemorySizeMB();

	/** Gets an iterator on all the table entry.
	 * <br>This method is optional, the default implementation throws an UnsupportedOperationException
	 * @return An iterator on all entries currently in the table.
	 * <b>Warning</b>Calling {@link #store(long, EntryType, int, int, Object, Predicate)} during
	 *  the iterator use may have unpredictable results. 
	 */
	default Iterator<TranspositionTableEntry<M>> getEntries() {
		throw new UnsupportedOperationException();
	}
}
