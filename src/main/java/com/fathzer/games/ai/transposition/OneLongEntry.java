package com.fathzer.games.ai.transposition;

import java.util.function.IntFunction;

/** A transposition table entry that can be converted to/from a long.
 * <br>Here are its limitations:<ul>
 * <li>score should be a short (16 bits)</li>
 * <li>depth is limited to 127 (8 bits), of course, it should be &gt;= 0</li>
 * <li>move can be represented as a int (32 bits)</li>
 * </ul> 
 */
class OneLongEntry<M> implements TranspositionTableEntry<M> {
	private static final long MOVE_MASK = 0xffffffffL; // 32 bits
	private static final int SCORE_SHIFT = 32;
	private static final long SCORE_MASK =  0xffff00000000L; // 16 bits
	private static final int DEPTH_SHIFT = 48;
	private static final long DEPTH_MASK = 0xff000000000000L; // 8 bits
	private static final int TYPE_SHIFT = 56;
	private static final long TYPE_MASK = 0x300000000000000L; // 2 bits
	// It remain 4 bits not used
	
	private final IntFunction<M> toMove;
	private long key;
	private long value;
	
	/** Constructor.
	 * @param toMove A function able to convert an integer to a move
	 */
	OneLongEntry(IntFunction<M> toMove) {
		this.toMove = toMove;
	}
	OneLongEntry<M> set(long key, long value) {
		this.key = key;
		this.value = value;
		return this;
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
		return EntryType.ALL.get((int) ((value & TYPE_MASK)>>TYPE_SHIFT));
	}
	
	static long toLong(EntryType type, byte depth, short value, int move) {
		return type==EntryType.INVALID ? 0 :
			((((long)type.ordinal()) << TYPE_SHIFT) & TYPE_MASK) |
			((((long)depth) << DEPTH_SHIFT) & DEPTH_MASK) |
			((((long)value) << SCORE_SHIFT) & SCORE_MASK) |
			(move & MOVE_MASK);
	}

	@Override
	public int getDepth() {
		return (byte) ((value & DEPTH_MASK) >> DEPTH_SHIFT);
	}

	@Override
	public int getValue() {
		return (short) ((value & SCORE_MASK) >> SCORE_SHIFT);
	}
	
	@Override
	public M getMove() {
		return toMove.apply((int)(value & MOVE_MASK));
	}
	
	@Override
	public String toString() {
		return key+":"+getEntryType()+" "+getValue()+" at "+getDepth()+" -> "+getMove();
	}
}
