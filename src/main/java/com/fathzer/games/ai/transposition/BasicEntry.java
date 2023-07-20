package com.fathzer.games.ai.transposition;

public class BasicEntry implements TranspositionTableEntry {
	private boolean valid;
	private long key;
	private EntryType type;
	private int depth;
	private int score;
	
	public BasicEntry(boolean valid, long key) {
		super();
		this.valid = valid;
		this.key = key;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public long getKey() {
		return key;
	}

	@Override
	public EntryType getEntryType() {
		return type;
	}

	@Override
	public void setEntryType(EntryType type) {
		this.type = type;
	}

	@Override
	public int getDepth() {
		return depth;
	}

	@Override
	public void setDepth(int depth) {
		this.depth = depth;
	}

	@Override
	public int getScore() {
		return score;
	}

	@Override
	public void setScore(int score) {
		this.score = score;
	}

}
