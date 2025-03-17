package com.fathzer.games.ai;

import java.util.function.Supplier;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.util.exec.Forkable;

/** The context of a best move search.
 * <br>It encapsulates the game position and an position evaluator.
 * @param <M> The type of moves 
 * @param <B> The type of the evaluator
 */
public class SearchContext<M, B extends MoveGenerator<M>> implements Forkable<SearchContext<M, B>> {
	private final B gamePosition;
	private final Evaluator<M, B> evaluator;
	private SearchStatistics statistics;
	
	private SearchContext(B gamePosition, Evaluator<M, B> evaluator, SearchStatistics statistics) {
		this.gamePosition = gamePosition;
		this.evaluator = evaluator;
		this.statistics = statistics;
	}

	/** Gets the game position.
	 * @return a {@link MoveGenerator} instance
	 */
	public B getGamePosition() {
		return gamePosition;
	}
	
	/** Gets the evaluator.
	 * @return an evaluator instance
	 */
	public Evaluator<M, B> getEvaluator() {
		return evaluator;
	}
	
	/** Gets the search statistics.
	 * @return a {@link SearchStatistics} instance
	 */
	public SearchStatistics getStatistics() {
		return statistics;
	}

	/** Makes a move.
	 * @param move The move to make
	 * @param confidence The confidence of the move
	 * @return true if the move was made, false otherwise (if it was illegal)
	 */
	public boolean makeMove(M move, MoveGenerator.MoveConfidence confidence) {
		evaluator.prepareMove(gamePosition, move);
		if (gamePosition.makeMove(move, confidence)) {
			evaluator.commitMove();
			return true;
		}
		return false;
	}
		
	/** Unmakes the last move. */
	public void unmakeMove() {
		evaluator.unmakeMove();
		gamePosition.unmakeMove();
	}

	@SuppressWarnings("unchecked")
	@Override
	public SearchContext<M, B> fork() {
		final B mg = (B)gamePosition.fork();
		final Evaluator<M, B> ev = evaluator.fork();
		return new SearchContext<>(mg, ev, statistics);
	}
	
	/** Gets a new search context.
	 * @param board The game position
	 * @param evaluatorBuilder A supplier that can create an evaluator
	 * @param <M> The type of moves
	 * @param <B> The type of the move generator
	 * @return a new search context
	 */
	public static <M, B extends MoveGenerator<M>> SearchContext<M, B> get(B board, Supplier<Evaluator<M, B>> evaluatorBuilder) {
		@SuppressWarnings("unchecked")
		final B b = (B) board.fork();
		final Evaluator<M, B> evaluator = evaluatorBuilder.get();
		evaluator.init(board);
		return new SearchContext<>(b, evaluator, new SearchStatistics());
	}
}