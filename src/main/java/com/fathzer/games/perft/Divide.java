package com.fathzer.games.perft;

/** A <a href="https://www.chessprogramming.org/Perft#Divide">perft divide</a> result for a specific move.
 * @param <M> The type of move
*/
public class Divide<M> {
	private final M move;
	private final long nbLeaves;

	/** Creates a new instance of Divide
	 * @param move The move
	 * @param nbLeaves The number of leaves found for this move
	 */
	public Divide(M move, long nbLeaves) {
		this.move = move;
		this.nbLeaves = nbLeaves;
	}

	/** Gets the move
	 * @return The move
	 */
	public M getMove() {
		return move;
	}

	/** Gets the number of leaves found for this move
	 * @return The number of leaves found for this move
	 */
	public long getNbLeaves() {
		return nbLeaves;
	}

	@Override
	public String toString() {
		return move.toString()+": "+nbLeaves;
	}
}

