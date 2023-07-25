package com.fathzer.games.ai.transposition;

import java.util.Arrays;

/**
 * A transposition table that associates a key to an entry represented by a long.
 * <br>Here are its limitations:<ul>
 * <li>score should be a short (16 bits)</li>
 * <li>depth is limited to 127 (8 bits), of course, it should be &gt;= 0</li>
 * <li>move can be represented as a integer (32 bits)</li>
 */
public abstract class OneLongEntryTranspositionTable<M> implements TranspositionTable<M> {
	private long[] table; // Used for transposition table
	private final int size; // The number of slots either table will have
	private static final int SLOTS = 2; // The number of long per record
	
	private final OneLongEntry<M> entry = new OneLongEntry<>(this::toMove);
	
	/** Constructor.
	 * @param sizeInMB The table size in MB
	 */
	protected OneLongEntryTranspositionTable(int sizeInMB) {
		this.size = (1024 * 1024 / 8 / SLOTS)*sizeInMB;
		table = new long[size * SLOTS];
	}
	
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
	public TranspositionTableEntry<M> get(long key) {
		final int index = getKeyIndex(key);
		return entry.set(key, table[index]==key ? table[index+1] : 0);
	}

	private int getKeyIndex(long key) {
		return Math.abs((int) (key % size) * SLOTS);
	}
	
	/** {@inheritDoc} 
	 * <br>WARNING: This method is not thread safe, it should be synchronized on the object returned by {@link #getLock()}
	 */
	@Override
	public void store(long key, EntryType type, int depth, int value, M move) {
		final int index = getKeyIndex(key);
		final long old = table[index+1];
		if (old!=0 && keep(entry.set(table[index], old), type, depth, value, move)) {
			return;
		}
		table[index] = key;
		table[index+1] = entry.toLong(type, (byte)depth, (short) value, toInt(move));
	}
	
	protected abstract int toInt(M move);
	protected abstract M toMove(int value);
	
	@Override
	public void newPosition() {
		// Clears the table
		Arrays.fill(table, 0);
	}
}
