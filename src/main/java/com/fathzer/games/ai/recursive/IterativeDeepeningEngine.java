package com.fathzer.games.ai.recursive;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.Evaluator;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.SearchStatistics;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.ai.transposition.TranspositionTable;
import com.fathzer.games.util.EvaluatedMove;

public abstract class IterativeDeepeningEngine<M, B extends MoveGenerator<M>> implements Function<B, M> {
	/** A class that logs events during search.
	 * @param <T> The class that represents a move
	 */
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
	
	/** A logger that logs nothing.
	 * @param <T> The class that represents a move
	 */
	public static final class Mute<T> implements EventLogger<T> {}
	
	private static final Random RND = new Random(); 

	private final Evaluator<B> evaluator;
	private SearchParameters searchParams;
	private long maxTime = Long.MAX_VALUE;
	private TranspositionTable<M> transpositionTable;
	private int parallelism;
	private EventLogger<M> logger;
	private IerativeDeepeningSearch<M, B> rs;
	private AtomicBoolean running;
	
	protected IterativeDeepeningEngine(Evaluator<B> evaluator, int maxDepth, TranspositionTable<M> tt) {
		this.parallelism = 1;
		this.searchParams = new SearchParameters(maxDepth);
		this.evaluator = evaluator;
		this.transpositionTable = tt;
		this.running = new AtomicBoolean();
		this.logger = new Mute<>();
	}
	
	public void interrupt() {
		if (running.get()) {
			rs.interrupt();
		}
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
		final List<EvaluatedMove<M>> bestMoves = getBestMoves(board);
		return bestMoves.get(RND.nextInt(bestMoves.size())).getContent();
	}

	public List<EvaluatedMove<M>> getBestMoves(B board) {
		setViewPoint(evaluator, board);
		// TODO Test if it is really a new position?
		transpositionTable.newPosition();
		try (ExecutionContext<M,B> context = buildExecutionContext(board)) {
			final Negamax<M,B> internal = buildNegaMax(context, evaluator);
			internal.setTranspositonTable(transpositionTable);
			if (!running.compareAndSet(false, true)) {
				throw new IllegalStateException();
			}
			try {
				rs = new IerativeDeepeningSearch<>(internal, searchParams, maxTime);
				rs.setEventLogger(logger);
				final List<EvaluatedMove<M>> result = rs.getBestMoves();
				for (EvaluatedMove<M> ev:result) {
					ev.setPvBuilder(m -> getTranspositionTable().collectPV(board, m, searchParams.getDepth()));
				}
				return result;
			} finally {
				running.set(false);
			}
		}
	}
	
	protected abstract ExecutionContext<M,B> buildExecutionContext(B board);
	
	protected abstract Negamax<M,B> buildNegaMax(ExecutionContext<M,B> context, Evaluator<B> evaluator);
	
	protected abstract void setViewPoint(Evaluator<B> evaluator, B board);
}
