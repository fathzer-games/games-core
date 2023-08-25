package com.fathzer.games.ai.moveSelector;

import java.util.List;

import com.fathzer.games.ai.evaluation.EvaluatedMove;

/** A move selector.
 * <br>An AI search can return more than a "<i>best</i>" move.
 * A move selector can narrow the <i>best</i> moves list using its own algorithm.
 * <br>The most obvious one is {@link RandomMoveSelector} that randomly selects a move.  
 * @param <M> The type of moves
 * @param <D> The data used by the MoveSelector
 */
public abstract class MoveSelector<M,D> {
	private MoveSelector<M,D> next;
	
	protected MoveSelector() {
		super();
	}
	
	public MoveSelector<M,D> setNext(MoveSelector<M,D> next) {
		this.next = next;
		return this;
	}

	public List<EvaluatedMove<M>> select(D data, List<EvaluatedMove<M>> result) {
		if (result.size()>1 && next!=null) {
			return next.select(data, result);
		}
		return result;
	}
}
