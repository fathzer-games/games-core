package com.fathzer.games.ai.evaluation;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fathzer.games.ai.evaluation.Evaluation.Type;

/** A move with its evaluation.
 * @param <M> The type of move
 */
public class EvaluatedMove<M> implements Comparable<EvaluatedMove<M>> {
	private final M content;
    private final Evaluation value;
    private List<M> pv;
    private Function<M,List<M>> pvBuilder;
    
    public EvaluatedMove(M what, Evaluation evaluation) {
    	this.content = what;
    	this.value = evaluation;
    }
    
    public M getContent() {
		return content;
	}

	public Evaluation getEvaluation() {
		return value;
	}
	
	public int getScore() {
		return value.getScore();
	}
	
	public boolean isEnd() {
		return value.getType()!=Type.EVAL;
	}
	
	/** Gets the <a href="https://en.wikipedia.org/wiki/Variation_(game_tree)">principal variation</a> of this move.
	 * @return a list of moves or null if the principal variation is not available (for instance if the evaluation has been
	 * computed by an algorithm that can't compute the principal variation).
	 */
	public List<M> getPrincipalVariation() {
		if (pv==null && pvBuilder!=null) {
			pv = pvBuilder.apply(content);
		}
		return pv;
	}

	/** Sets a function that can lazily compute the <a href="https://en.wikipedia.org/wiki/Variation_(game_tree)">principal variation</a> of this move.
	 * @param pvBuilder A function that converts a move to its principal variation.
	 * <br>This function will be invoked once with {@link #getContent()} as parameter first time {@link #getPrincipalVariation()} is called.
	 * <br>If null, which is the default value, the principal variation will not be computed and null will be returned
	 */
	public void setPvBuilder(Function<M,List<M>> pvBuilder) {
		this.pvBuilder = pvBuilder;
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
	
	public String toString(Function<M,String> toString) {
		return toString.apply(content)+"("+this.getEvaluation()+")";
	}
	
	@Override
	public String toString() {
		return toString(Object::toString);
	}

	public static <M> String toString(Collection<EvaluatedMove<M>> moves, Function<M,String> toString) {
		return moves.stream().map(m -> m.toString(toString)).collect(Collectors.joining(", ", "[", "]"));
	}
}
