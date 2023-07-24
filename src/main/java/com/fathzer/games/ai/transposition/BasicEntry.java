package com.fathzer.games.ai.transposition;

public class BasicEntry<M> implements TranspositionTableEntry<M> {
	private boolean valid;
	private long key;
	private EntryType type;
	private int depth;
	private int score;
	private M move;
	
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

	public void setKey(long key) {
		this.key = key;
	}

	@Override
	public EntryType getEntryType() {
		return type;
	}

	public void setEntryType(EntryType type) {
		this.type = type;
	}

	@Override
	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	@Override
	public int getValue() {
		return score;
	}
	
	public void setValue(int score) {
		this.score = score;
	}

	@Override
	public M getMove() {
		return move;
	}
	
	public void setMove(M move) {
		this.move = move;
	}
}
