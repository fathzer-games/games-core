package com.fathzer.games.ai.recursive;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.Evaluator;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.SearchStatistics;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.ai.transposition.TranspositionTable;
import com.fathzer.games.util.Evaluation;

public abstract class AbstractRecursiveEngine<M, B extends MoveGenerator<M>> implements Function<B, M> {
	public interface EventLogger<T> {
		default void logSearch(int depth, SearchStatistics stats, SearchResult<T> results) {
			// Does nothing by default
		}
		
		default void logTimeOut(int depth) {
			// Does nothing by default
		}

		default void logEndDetected(int depth) {
			// Does nothing by default
		}
	}
	
	public static final class Mute<T> implements EventLogger<T> {}
	private static final Random RND = new Random(); 

	private final Evaluator<B> evaluator;
	private SearchParameters searchParams;
	private long maxTime = Long.MAX_VALUE;
	private TranspositionTable<M> transpositionTable;
	private int parallelism;
	private EventLogger<M> logger;
	
	protected AbstractRecursiveEngine(Evaluator<B> evaluator, int maxDepth, TranspositionTable<M> tt) {
		this.parallelism = 1;
		this.searchParams = new SearchParameters(maxDepth);
		this.evaluator = evaluator;
		this.transpositionTable = tt;
		this.logger = new Mute<>();
	}
	
	public synchronized void interrupt() {
		//FIXME
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public int getParallelism() {
		return parallelism;
	}

	public void setParallelism(int parallelism) {
		this.parallelism = parallelism;
	}

	public SearchParameters getSearchParams() {
		return searchParams;
	}

	public void setMaxTime(long maxTime) {
		this.maxTime = maxTime;
	}

	public void setLogger(EventLogger<M> logger) {
		this.logger = logger;
	}

	public Evaluator<B> getEvaluator() {
		return evaluator;
	}

	public TranspositionTable<M> getTranspositionTable() {
		return transpositionTable;
	}
	
	@Override
	public M apply(B board) {
		final List<Evaluation<M>> bestMoves = getBestMoves(board);
		return bestMoves.get(RND.nextInt(bestMoves.size())).getContent();
	}

	public List<Evaluation<M>> getBestMoves(B board) {
		setViewPoint(evaluator, board);
		// TODO Test if it is really a new position?
		transpositionTable.newPosition();
		try (ExecutionContext<M> context = buildExecutionContext(board)) {
			final Negamax<M> internal = buildNegaMax(context);
			internal.setTranspositonTable(transpositionTable);
			final RecursiveSearch<M, B> rs = new RecursiveSearch<>(evaluator, internal, searchParams, maxTime);
			rs.setEventLogger(logger);
			return rs.getBestMoves();
		}
	}
	
	protected abstract ExecutionContext<M> buildExecutionContext(B board);
	
	protected abstract Negamax<M> buildNegaMax(ExecutionContext<M> context);
	
	protected abstract void setViewPoint(Evaluator<B> evaluator, B board);
}
