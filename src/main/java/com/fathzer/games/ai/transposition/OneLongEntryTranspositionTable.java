package com.fathzer.games.ai.transposition;

import java.util.Arrays;

/**
 * A transposition table that can associate a key to an entry represented by a long different from 0.
 * <br>non zero, because 0 is reserved to define unknown keys.
 */
public class OneLongEntryTranspositionTable implements TranspositionTable {
	private long[] table; // Used for transposition table
	private final int size; // The number of slots either table will have
	private static final int SLOTS = 2; // The number of long per record
	
	private static final OneLongEntry GET = new OneLongEntry();
	private static final OneLongEntry PREVIOUS = new OneLongEntry();
	
	/** Constructor.
	 * @param sizeInMB The table size in MB
	 */
	public OneLongEntryTranspositionTable(int sizeInMB) {
		this.size = (1024 * 1024 / 8 / SLOTS)*sizeInMB;
		table = new long[size * SLOTS];
	}
/*	
	public Object getLock() {
		//TODO Probably better not to synchronize all threads on the whole transposition table
		return this;
	}
*/
	
	/** Puts an entry in the table.
	 * <br>This method always replace the value already stored
	 * <br>WARNING: This method is not thread safe, it should be synchronized on the object returned by {@link #getLock()}
	 * @param key The table key
	 * @param value The value to associate with the key
	 */
	public void put(long key, long value) {
		final int index = getKeyIndex(key);
		table[index]=key;
		table[index+1]=value;
	}
	
	/** {@inheritDoc} 
	 * <br>WARNING: This method is not thread safe, it should be synchronized on the object returned by {@link #getLock()}
	 */
	@Override
	public TranspositionTableEntry get(long key) {
		final int index = getKeyIndex(key);
		return GET.set(key, table[index]==key ? table[index+1] : 0);
//		return table[index]==key ? new OneLongEntry(key, table[index+1]) : new OneLongEntry(key, 0);
	}

	private int getKeyIndex(long key) {
		return Math.abs((int) (key % size) * SLOTS);
	}
	
	/** {@inheritDoc} 
	 * <br>WARNING: This method is not thread safe, it should be synchronized on the object returned by {@link #getLock()}
	 */
	@Override
	public void store(TranspositionTableEntry entry) {
		final int index = getKeyIndex(entry.getKey());
		final long old = table[index+1];
		if (old!=0 && keep(PREVIOUS.set(table[index], old), entry)) {
//		if (old!=0 && keep(new OneLongEntry(table[index], old), entry)) {
			// There's already something in the table that should be kept
			return;
		}
		table[index] = entry.getKey();
		table[index+1] = ((OneLongEntry)entry).toLong();
	}
	
	@Override
	public void newPosition() {
		// Clears the table
		Arrays.fill(table, 0);
	}

	/**
	 * Collects the principal variation starting from the position on the board
	 * 
	 * @param board
	 *            The position to collect pv from
	 * @param current_depth
	 *            How deep the pv goes (avoids situations where keys point to
	 *            each other infinitely)
	 * @return collectString The moves in a string
	 */
/*	public int[] collectPV(Board board, int current_depth) {
		int[] arrayPV = new int[128];
		int move = getMove(board.zobristKey);

		int i = current_depth;
		int index = 0;
		while (i > 0) {
			if (move == 0 || !board.validateHashMove(move))
				break;
			arrayPV[index] = move;
			board.makeMove(move);
			move = getMove(board.zobristKey);
			i--;
			index++;
		}

		// Unmake the moves
		for (i = index - 1; i >= 0; i--) {
			board.unmakeMove(arrayPV[i]);
		}
		return arrayPV;
	}*/
}
