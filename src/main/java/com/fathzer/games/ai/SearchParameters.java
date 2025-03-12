package com.fathzer.games.ai;

import java.util.List;

import com.fathzer.games.ai.evaluation.EvaluatedMove;

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
			throw new IllegalArgumentException(ACCURACY_SHOULD_BE_POSITIVE);
		}
		this.accuracy = accuracy;
	}
	
	/** Gets the lower bound score under which moves will not be considered as accurate by {@link #getAccurateMovesCount(List)}.
	 * <br>An {@link AI} can return the lower bound score instead of the real one if it is sure that its real score is &lt;= the lower bound score.
	 * @param moves A ordered (best first) list of moves. If this list is not sorted, the result is unpredictable.
	 * @param <M> The type of the moves
	 * @return an int, Integer.MIN_VALUE if list is empty
	 */
	public <M> int getLowerBound(List<EvaluatedMove<M>> moves) {
		return moves.size()>=size ? moves.get(size-1).getScore() - accuracy -1 : Integer.MIN_VALUE;
	}
	
	/** Gets the number moves of a sorted move list that have an exact evaluation according to the {@link #getSize()} and {@link #getAccuracy()} of this search parameter.
	 * @param moves The list of moves to analyze. This list must be sorted (best first). If it is not sorted, the result is unpredictable.
	 * @param <M> The type of the moves
	 * @return a positive integer (0 if the <code>moves</code> is empty).
     * <br>Please note that the returned may be greater than {@link #getSize()} elements in case of equivalent moves or almost equivalent moves (according to {@link #getAccuracy()}).
     * It can also be less than {@link #getSize()} if there's less than {@link #getSize()} legal moves or search was interrupted before it finished. 
	 */
	public <M> int getAccurateMovesCount(List<EvaluatedMove<M>> moves) {
		final int low = getLowerBound(moves);
		int count = 0;
		for (EvaluatedMove<M> ev : moves) {
			if (ev.getScore()>low || count<size) {
				count++;
			} else {
				break;
			}
		}
		return count;
	}

	/** Gets the number of best moves of a sorted move list according to the {@link #getAccuracy()} of this search parameter.
	 * @param moves The list of moves to cut. This list must be sorted (best first). If it is not sorted, the result is unpredictable.
	 * @param <M> The type of the moves
	 * @return a positive integer (0 if the <code>moves</code> is empty).
     * <br>Please note that the returned may be greater than {@link #getSize()} elements in case of equivalent moves or almost equivalent moves (according to {@link #getAccuracy()}).
     * It can also be less than {@link #getSize()} if there's less than {@link #getSize()} legal moves or search was interrupted before it finished. 
	 */
	public <M> int getBestMovesCount(List<EvaluatedMove<M>> moves) {
		if (moves.isEmpty()) {
			return 0;
		}
		final int low = moves.get(0).getScore()-accuracy; 
		int count = 0;
		for (EvaluatedMove<M> ev : moves) {
			if (ev.getScore()>=low) {
				count++;
			} else {
				break;
			}
		}
		return count;
	}
}
