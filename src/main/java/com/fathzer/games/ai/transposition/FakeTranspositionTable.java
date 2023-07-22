package com.fathzer.games.ai.transposition;

/** A fake transposition table that never stores anything.
 */
public class FakeTranspositionTable implements TranspositionTable {

	@Override
	public TranspositionTableEntry get(long key) {
		return null;
	}

	@Override
	public void store(TranspositionTableEntry entry) {
		// Nothing to do
	}

	@Override
	public void newPosition() {
		// Nothing to do
	}
}
