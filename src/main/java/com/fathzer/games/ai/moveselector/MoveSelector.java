package com.fathzer.games.ai.moveselector;

import java.util.List;

import com.fathzer.games.ai.evaluation.EvaluatedMove;

/** A move selector.
 * <br>An AI search can return more than one "<i>best</i>" move.
 * Move selectors are use to narrow the <i>best</i> moves list using their own algorithm.
 * <br>The most obvious one is {@link RandomMoveSelector} that randomly selects a move.  
 * <br>Move selectors can be chained (using their {@link #setNext(MoveSelector)} method) in order to incrementally reduce the list.
 * @param <M> The type of moves
 * @param <D> The type of data passed by the engine to the MoveSelector
 */
public abstract class MoveSelector<M,D> {
	private MoveSelector<M,D> next;
	
	/** Constructor.
	 */
	protected MoveSelector() {
		super();
	}
	
	/** Sets a selector to call after this.
	 * <br>This method allows you to chain selectors
	 * @param next The next selector
	 * @return this
	 */
	public MoveSelector<M,D> setNext(MoveSelector<M,D> next) {
		this.next = next;
		return this;
	}

	/** Narrows a list of evaluated moves.
	 * <br>The default implementation does nothing except calling, if it exists and the list to narrow has more than one element, the chained selector.
	 * <br>A concrete selector class should override this method to narrow the list and then call super.select to launch chained selectors.
	 * @param data The data passed by the engine. The selector is free to use this data or not.
	 * @param result The list to narrow.
	 * @return The narrowed list.
	 */
	public List<EvaluatedMove<M>> select(D data, List<EvaluatedMove<M>> result) {
		if (result.size()>1 && next!=null) {
			return next.select(data, result);
		}
		return result;
	}
}
