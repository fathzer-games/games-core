package com.fathzer.games.ai.transposition;

/** A fake transposition table that never stores anything.
 */
public class FakeTranspositionTable implements TranspositionTable<BasicEntry> {

	@Override
	public BasicEntry get(long key) {
		return null;
	}

	@Override
	public void update(BasicEntry entry) {
		// Nothing to do
	}
	
}
