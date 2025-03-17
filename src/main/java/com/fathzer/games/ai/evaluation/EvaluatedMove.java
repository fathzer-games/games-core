package com.fathzer.games.ai.evaluation;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fathzer.games.ai.evaluation.Evaluation.Type;

/** A move with its evaluation.
 * @param <M> The type of move
 */
public class EvaluatedMove<M> implements Comparable<EvaluatedMove<M>> {
	private final M content;
    private final Evaluation value;
    
	/** Constructor.
	 * @param move the move
	 * @param evaluation the evaluation of the move
	 */
    public EvaluatedMove(M move, Evaluation evaluation) {
    	this.content = move;
    	this.value = evaluation;
    }
    
	/** Gets the move.
	 * @return the move
	 */
    public M getMove() {
		return content;
	}

	/** Gets the evaluation of the move.
	 * @return the evaluation of the move
	 */
	public Evaluation getEvaluation() {
		return value;
	}
	
	/** Gets the score of the move.
	 * @return the score of the move
	 */
	public int getScore() {
		return value.getScore();
	}
	
	/** Tests whether this move's evaluation is a game end (a win/loose score) or an evaluation
	 * @return true if this move's evaluation is a game end
	 */
	public boolean isEnd() {
		return value.getType()!=Type.EVAL;
	}

	@Override
    public int compareTo(EvaluatedMove<M> other) {
        return other.getEvaluation().compareTo(value);
    }

	/**
	 * Two evaluations are equals if their values are equals, even if their moves are different.<br>
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final EvaluatedMove<?> other = (EvaluatedMove<?>) obj;
		return this.value.equals(other.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
	
	/** Converts this evaluated move to a string using a given move to string function.
	 * @param toString The function that converts the move to a string
	 * @return the string representation of this evaluatedmove
	 */
	public String toString(Function<M,String> toString) {
		return toString.apply(content)+"("+this.getEvaluation()+")";
	}
	
	@Override
	public String toString() {
		return toString(Object::toString);
	}

	/** Converts a collection of evaluated moves to a string using a given move to string function.
	 * @param moves The collection of evaluated moves
	 * @param toString The function that converts the move to a string
	 * @param <M> The type of move
	 * @return the string representation of the collection of evaluated moves
	 */
	public static <M> String toString(Collection<EvaluatedMove<M>> moves, Function<M,String> toString) {
		return moves.stream().map(m -> m.toString(toString)).collect(Collectors.joining(", ", "[", "]"));
	}
}
