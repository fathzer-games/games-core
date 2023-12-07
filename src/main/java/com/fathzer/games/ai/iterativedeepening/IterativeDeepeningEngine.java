package com.fathzer.games.ai.iterativedeepening;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchContext;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.SearchStatistics;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.moveSelector.MoveSelector;
import com.fathzer.games.ai.moveSelector.RandomMoveSelector;
import com.fathzer.games.ai.transposition.TranspositionTable;
import com.fathzer.games.util.exec.ExecutionContext;

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
	

	private DeepeningPolicy deepeningPolicy;
	private TranspositionTable<M> transpositionTable;
	private int parallelism;
	private EventLogger<M> logger;
	private MoveSelector<M,IterativeDeepeningSearch<M>> moveSelector;
	private IterativeDeepeningSearch<M> rs;
	private AtomicBoolean running;
	
	protected IterativeDeepeningEngine(int maxDepth, TranspositionTable<M> tt) {
		this.parallelism = 1;
		this.transpositionTable = tt;
		this.running = new AtomicBoolean();
		this.logger = new Mute<>();
		this.deepeningPolicy = new DeepeningPolicy(maxDepth);
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
	
	public MoveSelector<M,IterativeDeepeningSearch<M>> getMoveSelector() {
		return moveSelector;
	}

	public void setMoveSelector(MoveSelector<M,IterativeDeepeningSearch<M>> moveSelector) {
		this.moveSelector = moveSelector;
	}

	public EventLogger<M> getLogger() {
		return logger;
	}

	public void setLogger(EventLogger<M> logger) {
		this.logger = logger;
	}

	public TranspositionTable<M> getTranspositionTable() {
		return transpositionTable;
	}
	
	public void setTranspositionTable(TranspositionTable<M> transpositionTable) {
		this.transpositionTable = transpositionTable;
	}

	public DeepeningPolicy getDeepeningPolicy() {
		return deepeningPolicy;
	}

	public void setDeepeningPolicy(DeepeningPolicy policy) {
		this.deepeningPolicy = policy;
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
		// TODO Test if it is really a new position?
		if (transpositionTable!=null) {
			transpositionTable.newPosition();
		}
		try (ExecutionContext<SearchContext<M,B>> context = buildExecutionContext(board)) {
			final Negamax<M,B> internal = buildNegaMax(context);
			internal.setTranspositonTable(transpositionTable);
			if (!running.compareAndSet(false, true)) {
				throw new IllegalStateException();
			}
			try {
				rs = new IterativeDeepeningSearch<>(internal, deepeningPolicy);
				rs.setEventLogger(logger);
				final List<EvaluatedMove<M>> result = rs.getBestMoves();
				for (EvaluatedMove<M> ev:result) {
					ev.setPvBuilder(m -> getTranspositionTable().collectPV(board, m, deepeningPolicy.getDepth()));
				}
				return rs;
			} finally {
				running.set(false);
			}
		}
	}
	
	protected abstract ExecutionContext<SearchContext<M,B>> buildExecutionContext(B board);
	
	protected Negamax<M,B> buildNegaMax(ExecutionContext<SearchContext<M,B>> context) {
		return new Negamax<>(context);
	}
}
