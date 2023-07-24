package com.fathzer.games.ai.transposition;

/** A fake transposition table that never stores anything.
 */
public class FakeTranspositionTable<M> implements TranspositionTable<M> {

	@Override
	public TranspositionTableEntry<M> get(long key) {
		return null;
	}

	@Override
	public void store(long key, EntryType type, int depth, int value, M move) {
		// Nothing to do
	}

	@Override
	public void newPosition() {
		// Nothing to do
	}
}
