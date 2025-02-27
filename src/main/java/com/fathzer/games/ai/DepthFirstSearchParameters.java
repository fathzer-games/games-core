package com.fathzer.games.ai;

/** The parameters of a <a href="https://en.wikipedia.org/wiki/Depth-first_search">depth first</a> AI search.
 * @see SearchResult
 */
public class DepthFirstSearchParameters extends SearchParameters {
	private static final String DEPTH_SHOULD_BE_STRICTLY_POSITIVE = "Depth should be strictly positive";

	private int depth;
	
	/** Constructor.
	 * <br>By default search size is 1 and accuracy is 0
	 * @param depth The search depth (must be &gt; 0)
	 * @throws IllegalArgumentException if depth &lt;=0
	 */
	public DepthFirstSearchParameters(int depth) {
		this(depth, 1, 0);
	}

	/** Constructor.
	 * @param depth The search depth (must be &gt; 0)
	 * @param size How many best moves are requested to have an exact value (Integer.MAX_VALUE to have all moves).
	 * @param accuracy the evaluation gap under which two moves are considered as equivalent.
	 * <br>This allows to obtain moves that are almost equivalent to the last strictly best move.
	 * <br>This could be useful to prevent the engine to always play the same move for a position.
	 * @throws IllegalArgumentException if depth or size &lt;=0, or accuracy &lt;0
	 */
	public DepthFirstSearchParameters(int depth, int size, int accuracy) {
		super(size, accuracy);
		if (depth<=0) {
			throw new IllegalArgumentException(DEPTH_SHOULD_BE_STRICTLY_POSITIVE);
		}
		this.depth = depth;
	}

	/** Gets the search depth
	 * @return a positive int
	 */
	public int getDepth() {
		return depth;
	}

	/** Sets the search depth.
	 * @param depth The search depth (must be &gt; 0)
	 * @throws IllegalArgumentException if depth &lt;=0
	 */
	public void setDepth(int depth) {
		if (depth<=0) {
			throw new IllegalArgumentException(DEPTH_SHOULD_BE_STRICTLY_POSITIVE);
		}
		this.depth = depth;
	}
}
