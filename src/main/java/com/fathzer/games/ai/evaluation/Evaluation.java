package com.fathzer.games.ai.evaluation;

import java.util.Comparator;

/** An evaluation. */
public class Evaluation implements Comparable<Evaluation>  {
	
	/** The type of the evaluation (win loose, etc..). */
	public enum Type {
		/** The player is loosing the game.*/
		LOOSE,
		/** A standard evaluation when no player is winning the game. */
		EVAL,
		/** The player is winning the game.*/
		WIN,
		/** The evaluation is unknown.*/
		UNKNOWN}
	
	/** A comparator to sort evaluations according to their score in reverse order (higher first).
	 */
	public static final Comparator<Evaluation> REVERSE = Comparator.comparingInt(Evaluation::getScore).reversed();
	
	private final Type type;
	private final int score;
	private final int countToEnd;
	
	private Evaluation(Type type, int score, int nbMovesToEnd) {
		this.type = type;
		this.score = score;
		this.countToEnd = nbMovesToEnd;
	}
	
	/** The UNKNOWN evaluation.*/
	public static final Evaluation UNKNOWN = new Evaluation(Type.UNKNOWN, 0, Integer.MAX_VALUE);
	
	/** Creates a win evaluation.
	 * @param nbMoves The number of moves to the end of the game.
	 * @param score The score to give to the player who wins the game (Typically higher that all <code>EVAL</code> evaluations and higher when <code>nbMoves</code> is low).
	 * @return A win evaluation.
	 */
	public static Evaluation win(int nbMoves, int score) {
		return new Evaluation(Type.WIN, score, nbMoves);
	}
	
	/** Creates a loose evaluation.
	 * @param nbMoves The number of moves to the end of the game.
	 * @param score The score to give to the player who looses the game (Typically lower that all <code>EVAL</code> evaluations and lower when <code>nbMoves</code> is low).
	 * @return A loose evaluation.
	 */
	public static Evaluation loose(int nbMoves, int score) {
		return new Evaluation(Type.LOOSE, score, nbMoves);
	}
	
	/** Creates an evaluation that is not a win or loose one.
	 * @param score The score to give to the player (Typically higher if the player has a higher probability to win).
	 * @return An evaluation.
	 */
	public static Evaluation score(int score) {
		return new Evaluation(Type.EVAL, score, Integer.MAX_VALUE);
	}

	/** Gets the type of the evaluation.
	 * @return The type of the evaluation
	 */
	public Type getType() {
		return type;
	}

	/** Gets the score of the evaluation.
	 * @return The score of the evaluation
	 */
	public int getScore() {
		return score;
	}
	
	/** Gets the number of moves to the end of the game.
	 * @return The number of moves to the end of the game (Integer.MAX_VALUE if the evaluation is not a game end nor an unknown one)
	 */
	public int getCountToEnd() {
		return countToEnd;
	}

	@Override
	public int compareTo(Evaluation other) {
		final long diff = (long)getScore() - other.getScore();
		if (diff==0) {
			return 0;
		} else {
			return diff < 0 ? -1 : 1;
		}
	}

	@Override
	public int hashCode() {
		return getScore();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Evaluation other = (Evaluation) obj;
		return other.type == type && other.countToEnd==countToEnd && score == other.score && type == other.type;
	}

	@Override
	public String toString() {
		return type==Type.EVAL ? Integer.toString(score) : type+"+"+getCountToEnd();
	}
}