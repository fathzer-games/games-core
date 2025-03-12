package com.fathzer.games.ai;

import java.util.ArrayList;
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
	
	/** Gets the lower bound score under which moves will not be considered as one of the best moves returned by {@link #getBestMoves(List)}.
	 * <br>An {@link AI} can returned the lower bound score instead of the real one if it is sure that its real score is lower than the lower bound score.
	 * <br>This is because such a move is guaranteed not to be among the best {@link #getSize()} moves returned by {@link #getBestMoves(List)}.
	 * @param moves A ordered (best first) list of moves. If this list is not sorted, the result is unpredictable.
	 * @param <M> The type of the moves
	 * @return an int
	 */
	public <M> int getLowerBound(List<EvaluatedMove<M>> moves) {
		return moves.size()>=size ? moves.get(size-1).getScore() - accuracy -1 : Integer.MIN_VALUE;
	}
	
	/** Gets the best moves of a sorted move list according to the {@link #getSize()} and {@link #getAccuracy()} of this search parameter.
	 * @param moves The list of moves to cut. This list must be sorted (best first). If it is not sorted, the result is unpredictable.
	 * @param <M> The type of the moves
	 * @return a list of moves restricted to the size and accuracy of this search parameter.
     * <br>Please note that the returned list may have more than {@link #getSize()} elements in case of equivalent moves or almost equivalent moves (according to {@link #getAccuracy()}).
     * It can also have less than {@link #getSize()} elements if there's less than {@link #getSize()} legal moves or search was interrupted before it finished. 
	 */
	public <M> List<EvaluatedMove<M>> getBestMoves(List<EvaluatedMove<M>> moves) {
		final List<EvaluatedMove<M>> cut = new ArrayList<>(moves.size());
		final int low = getLowerBound(moves);
		int currentCount = 0;
		for (EvaluatedMove<M> ev : moves) {
			if (ev.getScore()>low || currentCount<size) {
				cut.add(ev);
				currentCount++;
			}
		}
		return cut;
	}
}
