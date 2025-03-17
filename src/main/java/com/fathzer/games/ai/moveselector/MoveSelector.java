package com.fathzer.games.ai.moveselector;

import java.util.List;
import java.util.Optional;

import com.fathzer.games.ai.evaluation.EvaluatedMove;

/** A best move selector.
 * <br>An AI search can return more than one "<i>best</i>" move.
 * Move selectors sort the <i>best</i> moves list using their own algorithm, not only based of the raw evaluation value.
 * <br>The most obvious one is {@link RandomMoveSelector} that randomly sorts moves with same evaluation.  
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
	 * @param moveLIst The move list to narrow.
	 * @return The narrowed list.
	 */
	public List<EvaluatedMove<M>> select(D data, List<EvaluatedMove<M>> moveLIst) {
		if (moveLIst.size()>1 && next!=null) {
			return next.select(data, moveLIst);
		}
		return moveLIst;
	}
	
	/** Gets the selected move.
	 * <br>The default implementation returns the first element of the list returned by {@link #select(Object, List)}.
	 * @param data The data passed by the engine. The selector is free to use this data or not.
	 * @param moveList The move list in which to choose the move.
	 * @return The selected move, if any.
	 */
	public Optional<EvaluatedMove<M>> get(D data, List<EvaluatedMove<M>> moveList) {
		final List<EvaluatedMove<M>> selected = select(data, moveList);
		return selected==null || selected.isEmpty() ? Optional.empty() : Optional.of(selected.get(0));
	}
}
