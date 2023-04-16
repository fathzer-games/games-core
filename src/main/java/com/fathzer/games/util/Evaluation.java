package com.fathzer.games.util;

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
	public String toString() {
		return content.toString()+"("+this.getValue()+")";
	}
}
