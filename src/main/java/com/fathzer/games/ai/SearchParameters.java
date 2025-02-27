package com.fathzer.games.ai;

/** The parameters of an AI search.
 * @see SearchResult
 */
public class SearchParameters {
	private static final String SIZE_SHOULD_BE_STRICTLY_POSITIVE = "Size should be strictly positive";
	private static final String ACCURACY_SHOULD_BE_POSITIVE = "Accuracy should be positive";

	private int size;
	private int accuracy;
	
	/** Constructor.
	 * <br>By default search size is 1 and accuracy is 0
	 */
	public SearchParameters() {
		this(1, 0);
	}

	/** Constructor.
	 * @param size How many best moves are requested to have an exact value (Integer.MAX_VALUE to have all moves).
	 * @param accuracy the evaluation gap under which two moves are considered as equivalent.
	 * <br>This allows to obtain moves that are almost equivalent to the last strictly best move.
	 * <br>This could be useful to prevent the engine to always play the same move for a position.
	 * @throws IllegalArgumentException if depth or size &lt;=0, or accuracy &lt;0
	 */
	public SearchParameters(int size, int accuracy) {
		if (size<=0) {
			throw new IllegalArgumentException(SIZE_SHOULD_BE_STRICTLY_POSITIVE);
		}
		if (accuracy<0) {
			throw new IllegalArgumentException(ACCURACY_SHOULD_BE_POSITIVE);
		}
		this.size = size;
		this.accuracy = accuracy;
	}

	/** Gets the number of moves with exact values required.
	 * @return a positive int. Default is 1.
	 */
	public int getSize() {
		return size;
	}

	/** Sets the number of moves with exact values required.
	 * @param size How many best moves are requested to have an exact value (Integer.MAX_VALUE to have all moves).
	 * @throws IllegalArgumentException if size &lt;=0
	 */
	public void setSize(int size) {
		if (size<=0) {
			throw new IllegalArgumentException(SIZE_SHOULD_BE_STRICTLY_POSITIVE);
		}
		this.size = size;
	}

	/** Gets the evaluation gap under which two moves are considered as equivalent.
	 * @return a positive int
	 */
	public int getAccuracy() {
		return accuracy;
	}

	/** Sets the evaluation gap under which two moves are considered as equivalent.
	 * <br>This allows to obtain moves that are almost equivalent to the last strictly best move.
	 * <br>This could be useful to prevent the engine to always play the same move for a position.
	 * <br>Default value is 0
	 * @param accuracy the accuracy
	 * @throws IllegalArgumentException if accuracy &lt;0
	 */
	public void setAccuracy(int accuracy) {
		if (accuracy<0) {
			throw new IllegalArgumentException("Accuracy should be strictly positive");
		}
		this.accuracy = accuracy;
	}
}
