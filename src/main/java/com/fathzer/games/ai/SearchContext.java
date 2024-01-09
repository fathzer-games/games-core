package com.fathzer.games.ai;

import java.util.function.Supplier;

import com.fathzer.games.Color;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.util.exec.Forkable;

/** The context of a best move search.
 * <br>It encapsulates the game position and an position evaluator.
 * @param <M> The type of moves 
 * @param <B> The type of the evaluator
 */
public class SearchContext<M, B extends MoveGenerator<M>> implements Forkable<SearchContext<M, B>> {
	private B gamePosition;
	private Evaluator<M, B> evaluator;
	
	public SearchContext(B gamePosition, Evaluator<M, B> evaluator) {
		this.gamePosition = gamePosition;
		this.evaluator = evaluator;
	}

	public B getGamePosition() {
		return gamePosition;
	}
	
	public Evaluator<M, B> getEvaluator() {
		return evaluator;
	}
	
	public boolean makeMove(M move, MoveGenerator.MoveConfidence confidence) {
		evaluator.prepareMove(gamePosition, move);
		if (gamePosition.makeMove(move, confidence)) {
			evaluator.commitMove();
			return true;
		}
		return false;
	}
		
	public void unmakeMove() {
		evaluator.unmakeMove();
		gamePosition.unmakeMove();
	}

	@SuppressWarnings("unchecked")
	@Override
	public SearchContext<M, B> fork() {
		final B mg = (B)gamePosition.fork();
		final Evaluator<M, B> ev = evaluator.fork();
		return new SearchContext<>(mg, ev);
	}
	
	public static <M, B extends MoveGenerator<M>> SearchContext<M, B> get(B board, Supplier<Evaluator<M, B>> evaluatorBuilder) {
		@SuppressWarnings("unchecked")
		final B b = (B) board.fork();
		final Evaluator<M, B> evaluator = evaluatorBuilder.get();
		evaluator.init(board);
		return new SearchContext<>(b, evaluator);
	}
}