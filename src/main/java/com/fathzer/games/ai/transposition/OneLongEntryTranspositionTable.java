package com.fathzer.games.ai.transposition;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import com.fathzer.games.MoveGenerator;

/**
 * A transposition table that associates a key to an entry represented by a long.
 * <br>Here are its limitations:<ul>
 * <li>score should be a short (16 bits)</li>
 * <li>depth is limited to 127 (8 bits), of course, it should be &gt;= 0</li>
 * <li>move can be represented as a integer (32 bits)</li>
 * </ul>
 * @param <M> The type of moves
 * @param <B> The type of move generator
 */
public abstract class OneLongEntryTranspositionTable<M, B extends MoveGenerator<M>> implements TranspositionTable<M, B> {
	private static final int SLOTS = 2; // The number of long per record
	private final AtomicLongArray table; // Used for transposition table
	private final ReadWriteLock lock;
	private final int size; // The number of slots the table will have
	private int entryCount; // The number of currently occupied slots.
	private TranspositionTablePolicy<M, B> policy;

	/** Constructor.
	 * @param size The table size
	 * @param unit The unit used to pass the size
	 */
	protected OneLongEntryTranspositionTable(int size, SizeUnit unit) {
		this.size = (int) ((long)size*unit.getSize()) / 8 / SLOTS;
		table = new AtomicLongArray(this.size * SLOTS);
		this.lock = new ReentrantReadWriteLock();
		policy = new BasicPolicy<>();
	}
	
	@Override
	public TranspositionTableEntry<M> get(long key) {
		final int index = getKeyIndex(key);
		final OneLongEntry<M> entry = new OneLongEntry<>(this::toMove);
		lock.readLock().lock();
		try {
			return entry.set(key, table.get(index)==key ? table.get(index+1) : 0);
		} finally {
			lock.readLock().unlock();
		}
	}

	private int getKeyIndex(long key) {
		return Math.abs((int) (key % size) * SLOTS);
	}
	
	@Override
	public boolean store(long key, EntryType type, int depth, int value, M move, Predicate<TranspositionTableEntry<M>> validator) {
		final int index = getKeyIndex(key);
		final OneLongEntry<M> entry = new OneLongEntry<>(this::toMove);
		lock.writeLock().lock();
		try {
			entry.set(table.get(index), table.get(index+1));
			final boolean written = validator.test(entry);
			if (written) {
				if (!entry.isValid()) {
					entryCount++;
				}
				table.set(index, key);
				table.set(index+1, OneLongEntry.toLong(type, (byte)depth, (short) value, toInt(move)));
			}
			return written;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	public TranspositionTablePolicy<M, B> getPolicy() {
		return policy;
	}
	
	@Override
	public void setPolicy(TranspositionTablePolicy<M, B> policy) {
		this.policy = policy;
	}

	/** Converts a move to an int.
	 * @param move The move to convert
	 * @return The converted move
	 */
	protected abstract int toInt(M move);
	
	/** Converts an int to a move.
	 * @param value The value to convert
	 * @return The converted move
	 */
	protected abstract M toMove(int value);
	
	/**
	 * {@inheritDoc}
	 * In this implementation, the whole table is cleared.
	 */
	@Override
	public void newGame() {
		// Clears the table
		for (int i=0; i<table.length();i++) {
			table.set(i, 0);
		}
		this.entryCount = 0;
	}

	/**
	 * {@inheritDoc}
	 * In this implementation, the whole table is cleared.
	 */
	@Override
	public void newPosition(B board) {
		newGame();
	}
	
	@Override
	public int getSize() {
		return size;
	}
	
	@Override
	public int getEntryCount() {
		return entryCount;
	}

	@Override
	public final int getMemorySizeMB() {
		return (int)(((long)this.size * 8 * SLOTS) / SizeUnit.MB.getSize());
	}

	@Override
	public Iterator<TranspositionTableEntry<M>> getEntries() {
		return new TTIterator();
	}

	private class TTIterator implements Iterator<TranspositionTableEntry<M>> {
		private final int max;
		private int index = 0;
		private OneLongEntry<M> entry = new OneLongEntry<>(OneLongEntryTranspositionTable.this::toMove);
		
		private TTIterator() {
			index = 0;
			max = size*SLOTS;
			entry = new OneLongEntry<>(OneLongEntryTranspositionTable.this::toMove);
			prepareNext();
		}
		
		private void prepareNext() {
			while (index<max) {
				final long value = table.get(index+1);
				if (value!=0) {
					entry.set(table.get(index), value);
					break;
				}
				index += SLOTS;
			}
			if (index>=max) {
				entry=null;
			}
		}
		
		@Override
		public boolean hasNext() {
			return entry!=null;
		}

		@Override
		public TranspositionTableEntry<M> next() {
			if (entry==null) {
				throw new NoSuchElementException();
			}
			final TranspositionTableEntry<M> result = entry;
			index += SLOTS;
			prepareNext();
			return result;
		}
	}
}
