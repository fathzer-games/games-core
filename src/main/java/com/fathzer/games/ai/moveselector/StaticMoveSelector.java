package com.fathzer.games.ai.moveselector;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.ToIntFunction;

import com.fathzer.games.ai.evaluation.EvaluatedMove;

/**
 * A move selector that selects moves with the highest evaluation given by a function.
 * @param <M> The type of moves
 * @param <D> The type of data passed by the engine to the MoveSelector
 */
public class StaticMoveSelector<M,D> extends MoveSelector<M,D> {
	private final ToIntFunction<M> evaluator;
	
	/**
	 * Constructor.
	 * @param evaluator The function that evaluates a move
	 */
	public StaticMoveSelector(ToIntFunction<M> evaluator) {
		this.evaluator = evaluator;
	}

	@Override
	public List<EvaluatedMove<M>> select(D data, List<EvaluatedMove<M>> result) {
		final OptionalInt maxImmediateValue = result.stream().mapToInt(em->evaluator.applyAsInt(em.getMove())).max();
		result = result.stream().filter(em -> evaluator.applyAsInt(em.getMove())==maxImmediateValue.getAsInt()).toList();
		return super.select(data, result);
	}
}
