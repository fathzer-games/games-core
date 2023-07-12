package com.fathzer.games.ai;

/**
 * A transposition table that can associate a non zero long to a key.
 * <br>non zero, because 0 is reserved to define unknown keys.
 * //TODO Should be moved from here, it seems specific to chess 
 */
public class TranspositionTable {
	private long[] table; // Used for transposition table
	private final int size; // The number of slots either table will have
	private static final int SLOTS = 2; 
	
	public TranspositionTable(int sizeInMB) {
		this.size = (1024 * 1024 / 8 / SLOTS)*sizeInMB;
		table = new long[size * SLOTS];
	}
	
	public Object getLock() {
		//TODO Probably better not to synchronize all threads on the whole transposition table
		return this;
	}

	/**
	 * Clears the transposition table
	 */
	public void clear() {
		table = new long[size * SLOTS];
	}
	
	/** Puts an entry in the table.
	 * <br>This method always replace the value already stored
	 * <br>WARNING: This method is not thread safe, it should be synchronized on the object returned by {@link #getLock()}
	 * @param key The table key
	 * @param value The value to associate with the key
	 */
	public void put(long key, long value) {
		final int index = (int) (key % size) * SLOTS;
		table[index]=key;
		table[index+1]=value;
	}
	
	/** Gets an entry in the table.
	 * <br>WARNING: This method is not thread safe, it should be synchronized on the object returned by {@link #getLock()}
	 * @param key The table key
	 * @return 0 if the key is unknown, value in other cases
	 */
	public long get(long key) {
		final int index = (int) (key % size) * SLOTS;
		return table[index]==key ? table[index+1] : 0;
	}

	/**
	 * Records the entry if the spot is empty or new position has deeper depth
	 * or old position has wrong ancientNodeSwitch
	 * 
	 * @param zobrist The zobrist key
	 * @param depth The depth at which the move game evaluation has been made
	 * @param flag //TODO Don't remember what it is!
	 * @param eval The position evaluation
	 * @param move //TODO Not sure, the move that leads to this position
	 */
	public void put(long zobrist, int depth, int flag, int eval, int move) {
		// Always replace scheme

		int hashkey = (int) (zobrist % size) * SLOTS;

		
		table[hashkey] = 0 | (eval + 0x1FFFF)
				| ((1) << 18) | (flag << 20)
				| (depth << 22);
		table[hashkey + 1] = move;
		table[hashkey + 2] = (int) (zobrist >> 32);
		table[hashkey + 3] = (int) (zobrist & 0xFFFFFFFF);
	}

	/**
	 * Returns true if the entry at the right index is 0 which means we have an
	 * entry stored
	 * 
	 * @param zobrist A zobrist key
	 * @return true if the entry exists
	 */
	public boolean entryExists(long zobrist) {
		int hashkey = (int) (zobrist % size) * SLOTS;
		
		return table[hashkey + 2] == (int) (zobrist >> 32) && table[hashkey + 3] == (int) (zobrist & 0xFFFFFFFF) &&
				table[hashkey] != 0;
			
	}

//	/**
//	 * Returns the eval at the right index if the zobrist matches
//	 * 
//	 * @param zobrist
//	 */
//	public int getEval(long zobrist) {
//		int hashkey = (int) (zobrist % size) * SLOTS;
//		
//		if (table[hashkey + 2] == (int) (zobrist >> 32) && table[hashkey + 3] == (int) (zobrist & 0xFFFFFFFF))
//			return ((table[hashkey] & 0x3FFFF) - 0x1FFFF);
//
//		return 0;
//	}
//
//	/**
//	 * Returns the flag at the right index if the zobrist matches
//	 * 
//	 * @param zobrist
//	 */
//	public int getFlag(long zobrist) {
//		int hashkey = (int) (zobrist % size) * SLOTS;
//		if (table[hashkey + 2] == (int) (zobrist >> 32) && table[hashkey + 3] == (int) (zobrist & 0xFFFFFFFF))
//			return ((table[hashkey] >> 20) & 3);
//
//		return 0;
//	}
//
//	/**
//	 * Returns the move at the right index if the zobrist matches
//	 * 
//	 * @param zobrist
//	 */
//	public int getMove(long zobrist) {
//		int hashkey = (int) (zobrist % size) * SLOTS;
//		if (table[hashkey + 2] == (int) (zobrist >> 32) && table[hashkey + 3] == (int) (zobrist & 0xFFFFFFFF))
//			return table[hashkey + 1];
//
//		return 0;
//	} // END getMove
//
//	/**
//	 * Returns the depth at the right index if the zobrist matches
//	 * 
//	 * @param zobrist
//	 */
//	public int getDepth(long zobrist) {
//		int hashkey = (int) (zobrist % size) * SLOTS;
//		if (table[hashkey + 2] == (int) (zobrist >> 32) && table[hashkey + 3] == (int) (zobrist & 0xFFFFFFFF))
//			return (table[hashkey] >> 22);
//
//		return 0;
//	}

	/**
	 * Collects the principal variation starting from the position on the board
	 * 
	 * @param board
	 *            The position to collect pv from
	 * @param current_depth
	 *            How deep the pv goes (avoids situations where keys point to
	 *            each other infinitely)
	 * @return collectString The moves in a string
	 */
/*	public int[] collectPV(Board board, int current_depth) {
		int[] arrayPV = new int[128];
		int move = getMove(board.zobristKey);

		int i = current_depth;
		int index = 0;
		while (i > 0) {
			if (move == 0 || !board.validateHashMove(move))
				break;
			arrayPV[index] = move;
			board.makeMove(move);
			move = getMove(board.zobristKey);
			i--;
			index++;
		}

		// Unmake the moves
		for (i = index - 1; i >= 0; i--) {
			board.unmakeMove(arrayPV[i]);
		}
		return arrayPV;
	}*/
}
