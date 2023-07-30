package com.fathzer.games.ai.transposition;

import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A transposition table that associates a key to an entry represented by a long.
 * <br>Here are its limitations:<ul>
 * <li>score should be a short (16 bits)</li>
 * <li>depth is limited to 127 (8 bits), of course, it should be &gt;= 0</li>
 * <li>move can be represented as a integer (32 bits)</li>
 */
public abstract class OneLongEntryTranspositionTable<M> implements TranspositionTable<M> {
	private final AtomicLongArray table; // Used for transposition table
	private final Lock readLock;
	private final Lock writeLock;
	private final int size; // The number of slots either table will have
	private static final int SLOTS = 2; // The number of long per record
		
	/** Constructor.
	 * @param sizeInMB The table size in MB
	 */
	protected OneLongEntryTranspositionTable(int sizeInMB) {
		this.size = (1024 * 1024 / 8 / SLOTS)*sizeInMB;
		table = new AtomicLongArray(size * SLOTS);
		final ReadWriteLock lock = new ReentrantReadWriteLock();
		this.readLock = lock.readLock();
		this.writeLock = lock.writeLock();
	}
	
	/** {@inheritDoc} 
	 * <br>WARNING: This method is not thread safe, it should be synchronized on the object returned by {@link #getLock()}
	 */
	@Override
	public TranspositionTableEntry<M> get(long key) {
		final int index = getKeyIndex(key);
		final OneLongEntry<M> entry = new OneLongEntry<>(this::toMove);
		readLock.lock();
		try {
			return entry.set(key, table.get(index)==key ? table.get(index+1) : 0);
		} finally {
			readLock.unlock();
		}
	}

	private int getKeyIndex(long key) {
		return Math.abs((int) (key % size) * SLOTS);
	}
	
	@Override
	public void store(long key, EntryType type, int depth, int value, M move) {
		final int index = getKeyIndex(key);
		final OneLongEntry<M> entry = new OneLongEntry<>(this::toMove);
		writeLock.lock();
		try {
			final long old = table.get(index+1);
			if (old!=0 && keep(entry.set(table.get(index), old), type, depth, value, move)) {
				return;
			}
			table.set(index, key);
			table.set(index+1, entry.toLong(type, (byte)depth, (short) value, toInt(move)));
		} finally {
			writeLock.unlock();
		}
	}
	
	protected abstract int toInt(M move);
	protected abstract M toMove(int value);
	
	@Override
	public void newPosition() {
		// Clears the table
		for (int i=0; i<table.length();i++) {
			table.set(i, 0);
		}
	}
}
