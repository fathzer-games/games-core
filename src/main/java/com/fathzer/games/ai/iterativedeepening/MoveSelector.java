package com.fathzer.games.ai.iterativedeepening;

import java.util.List;

import com.fathzer.games.ai.evaluation.EvaluatedMove;

public abstract class MoveSelector<M> {
	private MoveSelector<M> next;
	
	protected MoveSelector() {
		super();
	}
	
	public MoveSelector<M> setNext(MoveSelector<M> next) {
		this.next = next;
		return this;
	}

	public List<EvaluatedMove<M>> select(IterativeDeepeningSearch<M> search, List<EvaluatedMove<M>> result) {
		if (result.size()>1 && next!=null) {
			return next.select(search, result);
		}
		return result;
	}
}
