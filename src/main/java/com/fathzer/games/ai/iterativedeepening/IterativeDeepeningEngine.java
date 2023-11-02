package com.fathzer.games.ai.iterativedeepening;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.SearchStatistics;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.ai.moveSelector.MoveSelector;
import com.fathzer.games.ai.moveSelector.RandomMoveSelector;
import com.fathzer.games.ai.transposition.TranspositionTable;

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

		default void logEndedByPolicy(int depth) {
			// Does nothing by default
		}
	}
	
	/** A logger that logs nothing.
	 * @param <T> The class that represents a move
	 */
	public static final class Mute<T> implements EventLogger<T> {}
	

	private Evaluator<B> evaluator;
	private Supplier<DeepeningPolicy> deepBuilder;
	private SearchParameters searchParams;
	private long maxTime = Long.MAX_VALUE;
	private TranspositionTable<M> transpositionTable;
	private int parallelism;
	private EventLogger<M> logger;
	private MoveSelector<M,IterativeDeepeningSearch<M>> moveSelector;
	private IterativeDeepeningSearch<M> rs;
	private AtomicBoolean running;
	
	protected IterativeDeepeningEngine(Evaluator<B> evaluator, int maxDepth, TranspositionTable<M> tt) {
		this.parallelism = 1;
		this.searchParams = new SearchParameters(maxDepth);
		this.evaluator = evaluator;
		this.transpositionTable = tt;
		this.running = new AtomicBoolean();
		this.logger = new Mute<>();
		this.deepBuilder = () -> new DeepeningPolicy(Long.MAX_VALUE) {};
		this.moveSelector = new RandomMoveSelector<>();
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
	
	public void setEvaluator(Evaluator<B> evaluator) {
		this.evaluator = evaluator;
	}
	
	public MoveSelector<M,IterativeDeepeningSearch<M>> getMoveSelector() {
		return moveSelector;
	}

	public void setMoveSelector(MoveSelector<M,IterativeDeepeningSearch<M>> moveSelector) {
		this.moveSelector = moveSelector;
	}

	public SearchParameters getSearchParams() {
		return searchParams;
	}

	public long getMaxTime() {
		return maxTime;
	}

	public void setMaxTime(long maxTimeMs) {
		this.maxTime = maxTimeMs;
	}

	public EventLogger<M> getLogger() {
		return logger;
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
	
	public void setTranspositionTable(TranspositionTable<M> transpositionTable) {
		this.transpositionTable = transpositionTable;
	}

	public void setDeepeningPolicyBuilder(Supplier<DeepeningPolicy> deepBuilder) {
		this.deepBuilder = deepBuilder;
	}

	@Override
	public M apply(B board) {
		final IterativeDeepeningSearch<M> search = search(board);
		return this.moveSelector.select(search, search.getBestMoves()).get(0).getContent();
	}

	public List<EvaluatedMove<M>> getBestMoves(B board) {
		return search(board).getBestMoves();
	}
	
	protected IterativeDeepeningSearch<M> search(B board) {
		setViewPoint(evaluator, board);
		// TODO Test if it is really a new position?
		if (transpositionTable!=null) {
			transpositionTable.newPosition();
		}
		try (ExecutionContext<M,B> context = buildExecutionContext(board)) {
			final Negamax<M,B> internal = buildNegaMax(context, evaluator);
			internal.setTranspositonTable(transpositionTable);
			if (!running.compareAndSet(false, true)) {
				throw new IllegalStateException();
			}
			try {
				rs = new IterativeDeepeningSearch<>(internal, searchParams, deepBuilder.get(), maxTime);
				rs.setEventLogger(logger);
				final List<EvaluatedMove<M>> result = rs.getBestMoves();
				for (EvaluatedMove<M> ev:result) {
					ev.setPvBuilder(m -> getTranspositionTable().collectPV(board, m, searchParams.getDepth()));
				}
				return rs;
			} finally {
				running.set(false);
			}
		}
	}
	
	protected abstract ExecutionContext<M,B> buildExecutionContext(B board);
	
	protected Negamax<M,B> buildNegaMax(ExecutionContext<M,B> context, Evaluator<B> evaluator) {
		return new Negamax<>(context, evaluator);
	}
	
	protected abstract void setViewPoint(Evaluator<B> evaluator, B board);
}
