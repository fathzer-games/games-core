package com.fathzer.games.ai.transposition;

public enum SizeUnit {
	MB(1024*1024), KB(1024), B(1);

	private final int size;

	private SizeUnit(int size) {
		this.size = size;
	}

	/** Get the number of bytes of this size unit.
	 * @return an integer
	 */
	public int getSize() {
		return size;
	}
}