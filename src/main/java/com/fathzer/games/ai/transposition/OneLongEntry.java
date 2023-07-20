package com.fathzer.games.ai.transposition;

/** A transposition table entry that can be converted to/from a long.
 * <br>Here are its limitations:<ul>
 * <li>score should be a short (16 bits)</li>
 * <li>depth is limited to 127 (8 bits), of course, it should be &gt;= 0</li>
 * <li>move can be represented as a int (32 bits)</li>
 * </ul> 
 */
public class OneLongEntry implements TranspositionTableEntry {
	//	private static final long moveMask = 0xffffffffL; // 32 bits
	private static final int SCORE_SHIFT = 32;
	private static final long SCORE_MASK =  0xffff00000000L; // 16 bits
	private static final int DEPTH_SHIFT = 48;
	private static final long DEPTH_MASK = 0xff000000000000L; // 8 bits
	private static final int TYPE_SHIFT = 56;
	private static final long TYPE_MASK = 0x300000000000000L; // 2 bits
	private static final long VALID_MASK = 0x400000000000000L; // 1 bit
	// It remain 3 bits not used
	
	private long key;
	private long value;

	/** Constructor of valid entry.
	 * @param key The entry key
	 * @param value The value that represents the entry (0 for invalid entry)
	 */
	public OneLongEntry(long key, long value) {
		this.key = key;
		this.value = value;
	}

	/** Converts this entry to a non zero long.
	 * @return a long different from 0
	 */
	public long toLong() {
		return value | VALID_MASK;
	}
	
	@Override
	public long getKey() {
		return key;
	}

	@Override
	public boolean isValid() {
		return value!=0;
	}

	@Override
	public EntryType getEntryType() {
		return EntryType.values()[(int) ((value & TYPE_MASK)>>TYPE_SHIFT)];
	}

	@Override
	public void setEntryType(EntryType type) {
		value = (value & ~TYPE_MASK) | (((long)type.ordinal()) << TYPE_SHIFT);
	}

	@Override
	public int getDepth() {
		return (byte) ((value & DEPTH_MASK) >> DEPTH_SHIFT);
	}

	@Override
	public void setDepth(int depth) {
		value = (value & ~DEPTH_MASK) | (((long)depth) << DEPTH_SHIFT);
	}

	@Override
	public int getScore() {
		return (short) ((value & SCORE_MASK) >> SCORE_SHIFT);
	}

	@Override
	public void setScore(int score) {
		value = (value & ~SCORE_MASK) | (((long)score) << SCORE_SHIFT);
	}
}
