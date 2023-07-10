package com.fathzer.games;

/** A class able to compute a <a href="https://en.wikipedia.org/wiki/Zobrist_hashing">Zobrist key</a> for the current game state.
 * <br>Typically, a chess {@link MoveGenerator} which supports Zobrist hashing will implement this interface.
 */
@FunctionalInterface
public interface ZobristProvider {
	/** Gets the hash key of the current game state.
	 * @return a long
	 */
	long getZobristKey();
}
