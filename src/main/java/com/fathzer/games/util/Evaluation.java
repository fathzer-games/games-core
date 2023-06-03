package com.fathzer.games.util;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Evaluation<M> implements Comparable<Evaluation<M>> {
	private final M content;
    private final int value;
    
    public Evaluation(M what, int evaluation) {
    	this.content = what;
    	this.value = evaluation;
    }
    
    public M getContent() {
		return content;
	}

	public int getValue() {
		return value;
	}

	@Override
    public int compareTo(Evaluation<M> move) {
        return move.value - value;
    }

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
		final Evaluation<?> other = (Evaluation<?>) obj;
		return this.value==other.value;
	}

	@Override
	public int hashCode() {
		return value;
	}
	
	public String toString(Function<M,String> toString) {
		return toString.apply(content)+"("+this.getValue()+")";
	}
	
	public static <M> String toString(Collection<Evaluation<M>> moves, Function<M,String> toString) {
		return moves.stream().map(m -> m.toString(toString)).collect(Collectors.joining(", ", "[", "]"));
	}
}
