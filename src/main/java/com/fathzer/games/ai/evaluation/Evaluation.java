package com.fathzer.games.ai.evaluation;

import java.util.Comparator;

public class Evaluation implements Comparable<Evaluation>  {
	public enum Type {LOOSE, EVAL, WIN}
	
	public static final Comparator<Evaluation> REVERSE = Comparator.comparingInt(Evaluation::getScore).reversed();
	
	private final Type type;
	private final int score;
	private final int countToEnd;
	
	protected Evaluation(Type type, int score, int nbMovesToEnd) {
		this.type = type;
		this.score = score;
		this.countToEnd = nbMovesToEnd;
	}
	
	public static Evaluation win(int nbMoves, int score) {
		return new Evaluation(Type.WIN, score, nbMoves);
	}
	public static Evaluation loose(int nbMoves, int score) {
		return new Evaluation(Type.LOOSE, score, nbMoves);
	}
	public static Evaluation score(int score) {
		return new Evaluation(Type.EVAL, score, Integer.MAX_VALUE);
	}

	public Type getType() {
		return type;
	}

	public int getScore() {
		return score;
	}
	
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