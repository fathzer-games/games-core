package com.fathzer.games.ai.moveselector;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.ToIntFunction;

import com.fathzer.games.ai.evaluation.EvaluatedMove;

public class StaticMoveSelector<M,D> extends MoveSelector<M,D> {
	private final ToIntFunction<M> evaluator;
	
	public StaticMoveSelector(ToIntFunction<M> evaluator) {
		this.evaluator = evaluator;
	}

	@Override
	public List<EvaluatedMove<M>> select(D data, List<EvaluatedMove<M>> result) {
		final OptionalInt maxImmediateValue = result.stream().mapToInt(em->evaluator.applyAsInt(em.getContent())).max();
		result = result.stream().filter(em -> evaluator.applyAsInt(em.getContent())==maxImmediateValue.getAsInt()).toList();
		return super.select(data, result);
	}
}
