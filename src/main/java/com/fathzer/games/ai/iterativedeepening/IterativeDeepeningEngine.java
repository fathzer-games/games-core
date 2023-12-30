package com.fathzer.games.ai.iterativedeepening;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchContext;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.SearchStatistics;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.moveselector.MoveSelector;
import com.fathzer.games.ai.moveselector.RandomMoveSelector;
import com.fathzer.games.ai.transposition.TranspositionTable;
import com.fathzer.games.util.exec.ContextualizedExecutor;
import com.fathzer.games.util.exec.ExecutionContext;
import com.fathzer.games.util.exec.MultiThreadsContext;
import com.fathzer.games.util.exec.SingleThreadContext;

public class IterativeDeepeningEngine<M, B extends MoveGenerator<M>> implements Function<B, M> {
	/** A class that logs events during search.
	 * @param <M> The class that represents a move
	 * @param <B> The class that represents the move generator 
	 */
	public interface EventLogger<M, B extends MoveGenerator<M>> {
		default void logSearch(int depth, SearchStatistics stats, SearchResult<M> results) {
			// Does nothing by default
		}
		
		default void logTimeOut(int depth) {
			// Does nothing by default
		}

		default void logEndedByPolicy(int depth) {
			// Does nothing by default
		}
		
		default void logLibraryMove(B board, M move) {
			// Does nothing by default
		}

		default void logMoveChosen(B board, EvaluatedMove<M> evaluatedMove) {
			// Does nothing by default
		}
	}
	
	/** A logger that logs nothing.
	 * @param <M> The class that represents a move
	 * @param <B> The class that represents the move generator 
	 */
	public static final class Mute<M, B extends MoveGenerator<M>> implements EventLogger<M, B> {}
	
	private Function<B, M> movesLibrary;
	private Supplier<Evaluator<M, B>> evaluatorSupplier;
	private DeepeningPolicy deepeningPolicy;
	private TranspositionTable<M> transpositionTable;
	private int parallelism;
	private EventLogger<M, B> logger;
	private Function<B, MoveSelector<M,IterativeDeepeningSearch<M>>> moveSelectorBuilder;
	private IterativeDeepeningSearch<M> rs;
	private AtomicBoolean running;
	
	protected IterativeDeepeningEngine(int maxDepth, TranspositionTable<M> tt, Supplier<Evaluator<M, B>> evaluatorSupplier) {
		this.parallelism = 1;
		this.transpositionTable = tt;
		this.evaluatorSupplier = evaluatorSupplier;
		this.running = new AtomicBoolean();
		this.logger = new Mute<>();
		this.deepeningPolicy = new DeepeningPolicy(maxDepth);
		this.moveSelectorBuilder = b -> new RandomMoveSelector<>();
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
	
	/** Sets a move library (typically an openings library) of this engine.
	 * @param library The opening library or null, the default value, to play without such library.
	 * <br>An openings library is a function that should return null if the library does not known what to play here.
	 */
	public void setOpenings(Function<B, M> library) {
		this.movesLibrary = library;
	}

	public void setEvaluatorSupplier(Supplier<Evaluator<M, B>> evaluatorSupplier) {
		this.evaluatorSupplier = evaluatorSupplier;
	}

	public void setMoveSelectorBuilder(Function<B, MoveSelector<M,IterativeDeepeningSearch<M>>> moveSelectorBuilder) {
		this.moveSelectorBuilder = moveSelectorBuilder;
	}

	public EventLogger<M, B> getLogger() {
		return logger;
	}

	public void setLogger(EventLogger<M, B> logger) {
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
		M move = movesLibrary==null ? null : movesLibrary.apply(board);
		if (move==null) {
			final IterativeDeepeningSearch<M> search = search(board);
			final EvaluatedMove<M> evaluatedMove = this.moveSelectorBuilder.apply(board).select(search, search.getBestMoves()).get(0);
			move = evaluatedMove.getContent();
			logger.logMoveChosen(board, evaluatedMove);
		} else {
			logger.logLibraryMove(board, move);
		}
		return move;
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
	
	protected ExecutionContext<SearchContext<M,B>> buildExecutionContext(B board) {
		final SearchContext<M, B> context = SearchContext.get(board, evaluatorSupplier);
		if (getParallelism()==1) {
			return new SingleThreadContext<>(context);
		} else {
			final ContextualizedExecutor<SearchContext<M, B>> contextualizedExecutor = new ContextualizedExecutor<>(getParallelism());
			return new MultiThreadsContext<>(context, contextualizedExecutor);
		}
	}
	
	protected Negamax<M,B> buildNegaMax(ExecutionContext<SearchContext<M,B>> context) {
		return new Negamax<>(context);
	}
}
