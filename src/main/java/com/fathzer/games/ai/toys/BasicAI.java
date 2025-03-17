package com.fathzer.games.ai.toys;

import java.util.List;
import java.util.function.Predicate;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.AI;
import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.Evaluation;

/** A basic AI
 * @param <M> The type of moves
 * @param <B> The type of move generator
 */
public abstract class BasicAI<M,B extends MoveGenerator<M>> implements AI<M, SearchParameters> {

	/** The board on which the AI plays */
	protected final B board;

	/** Constructor.
	 * @param board The board on which the AI plays
	 */
	protected BasicAI(B board) {
		this.board = board;
	}

	@Override
	public SearchResult<M> getBestMoves(SearchParameters parameters) {
		return getBestMoves(null, parameters);
	}

	@Override
	public SearchResult<M> getBestMoves(List<M> possibleMoves, SearchParameters parameters) {
		final SearchResult<M> result = new SearchResult<>(parameters);
		final List<M> moves = board.getLegalMoves();
		final Predicate<M> filter = possibleMoves==null ? m->true : possibleMoves::contains;
		moves.stream().filter(filter).forEach(m -> result.add(m, getEvaluation(m)));
		return result;
	}
	
	/** Gets the evaluation of a move
	 * @param move The move
	 * @return The evaluation of the move
	 */
	protected abstract Evaluation getEvaluation(M move);
}
