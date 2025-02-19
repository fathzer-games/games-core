package com.fathzer.games.ai.evaluation;

/** A stateless class that can evaluate a game position.
 * <br>It simplifies the development of stateless evaluation functions by providing default implementation of incremental evaluation specific methods.
 * <br>This class does not support <a href="https://www.chessprogramming.org/Incremental_Updates">incremental evaluator</a>.
 * See {@link Evaluator} if you prefer to implement incremental evaluation.
 * @param <B> The type of the game position
 * @param <M> The type of a move
 */
public interface StaticEvaluator<M, B> extends Evaluator<M, B> {
	@Override
	default void init(B board) {
		// By default, the evaluator is not incremental
	}
	@Override
	default void prepareMove(B board, M move) {
		// By default, the evaluator is not incremental
	}
	@Override
	default void commitMove() {
		// By default, the evaluator is not incremental
	}
	@Override
	default void unmakeMove() {
		// By default, the evaluator is not incremental
	}
	@Override
	default StaticEvaluator<M, B> fork() {
		return this;
	}
}
