package com.fathzer.games.ai.iterativedeepening;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import com.fathzer.games.ai.evaluation.EvaluatedMove;

public class StaticMoveSelector<M> extends MoveSelector<M> {
	private final ToIntFunction<M> evaluator;
	
	public StaticMoveSelector(ToIntFunction<M> evaluator) {
		this.evaluator = evaluator;
	}

	@Override
	public List<EvaluatedMove<M>> select(IterativeDeepeningSearch<M> search, List<EvaluatedMove<M>> result) {
		final OptionalInt maxImmediateValue = result.stream().mapToInt(em->evaluator.applyAsInt(em.getContent())).max();
		result = result.stream().filter(em -> evaluator.applyAsInt(em.getContent())==maxImmediateValue.getAsInt()).collect(Collectors.toList());
		return super.select(search, result);
	}

}
