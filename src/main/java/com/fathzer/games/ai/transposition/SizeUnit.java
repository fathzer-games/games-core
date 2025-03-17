package com.fathzer.games.ai.transposition;

/** A unit of size for memory size. */
public enum SizeUnit {
	/** Megabytes (1024KB) */
	MB(1024*1024), 
	/** Kilobytes (1024 Bytes) */
	KB(1024), 
	/** Bytes (1B) */
	B(1);

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