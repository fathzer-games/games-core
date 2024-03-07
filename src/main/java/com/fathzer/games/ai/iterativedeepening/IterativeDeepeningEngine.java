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
import com.fathzer.games.movelibrary.MoveLibrary;
import com.fathzer.games.util.exec.ContextualizedExecutor;
import com.fathzer.games.util.exec.ExecutionContext;
import com.fathzer.games.util.exec.MultiThreadsContext;
import com.fathzer.games.util.exec.SingleThreadContext;

/** An engine that iteratively deepens the search. 
 * @param <M> The class that represents a move
 * @param <B> The class that represents the move generator 
 */
public class IterativeDeepeningEngine<M, B extends MoveGenerator<M>> implements Function<B, M> {
	/** A class that logs events during search at a specific level.
	 * <br>By default, a logger does nothing.
	 * @param <M> The class that represents a move
	 */
	public interface SearchEventLogger<M> {
		default void logSearchAtDepth(int depth, SearchStatistics stats, SearchResult<M> results) {
			// Does nothing by default
		}
		
		default void logTimeOut(int depth) {
			// Does nothing by default
		}

		default void logEndedByPolicy(int depth) {
			// Does nothing by default
		}
	}
	
	/** A class that logs events during the search.
	 * <br>By default, a logger does nothing.
	 * @param <M> The class that represents a move
	 * @param <B> The class that represents the move generator 
	 */
	public interface EngineEventLogger<M, B extends MoveGenerator<M>> extends SearchEventLogger<M> {
		default void logLibraryMove(B board, M move) {
			// Does nothing by default
		}

		default void logSearchStart(B board, IterativeDeepeningEngine<M, B> engine) {
			// Does nothing by default
		}

		default void logMoveChosen(B board, EvaluatedMove<M> evaluatedMove) {
			// Does nothing by default
		}

		default void logSearchEnd(B board, IterativeDeepeningSearch<M> result) {
			// Does nothing by default
		}
	}
	
	/** A logger that logs nothing.
	 * @param <M> The class that represents a move
	 * @param <B> The class that represents the move generator 
	 */
	public static final class Mute<M, B extends MoveGenerator<M>> implements EngineEventLogger<M, B> {}
	
	private MoveLibrary<M, B> movesLibrary;
	private Supplier<Evaluator<M, B>> evaluatorSupplier;
	private DeepeningPolicy deepeningPolicy;
	private TranspositionTable<M> transpositionTable;
	private int parallelism;
	private EngineEventLogger<M, B> logger;
	private Function<B, MoveSelector<M,IterativeDeepeningSearch<M>>> moveSelectorBuilder;
	private IterativeDeepeningSearch<M> rs;
	private AtomicBoolean running;
	
	/** Constructor
	 * <br>By default, the parallelism of the search is 1, the event logger logs nothing and the engine select randomly a move in the best move list.
	 * @param deepeningPolicy The policy to decide what move to deepen, how to merge results at different depth, etc...
	 * @param tt A transposition table used across different depth 
	 * @param evaluatorSupplier An evaluation function supplier
	 * @see #setMoveSelectorBuilder(Function)
	 * @see #setParallelism(int)
	 * @see #setLogger(EngineEventLogger)
	 */
	public IterativeDeepeningEngine(DeepeningPolicy deepeningPolicy, TranspositionTable<M> tt, Supplier<Evaluator<M, B>> evaluatorSupplier) {
		this.parallelism = 1;
		this.transpositionTable = tt;
		this.evaluatorSupplier = evaluatorSupplier;
		this.running = new AtomicBoolean();
		this.logger = new Mute<>();
		this.deepeningPolicy = deepeningPolicy;
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

	/** Sets how many threads are used to perform the searches.
	 * <br>Calling this method while performing a search may have unpredictable results
	 * @param parallelism The number of threads used to perform the search (default is 1)
	 */
	public void setParallelism(int parallelism) {
		this.parallelism = parallelism;
	}
	
	/** Sets a move library (typically an openings library) of this engine.
	 * @param library The opening library or null, the default value, to play without such library.
	 * <br>An openings library is a function that should return null if the library does not known what to play here.
	 */
	public void setOpenings(MoveLibrary<M, B> library) {
		this.movesLibrary = library;
	}

	public void setEvaluatorSupplier(Supplier<Evaluator<M, B>> evaluatorSupplier) {
		this.evaluatorSupplier = evaluatorSupplier;
	}

	/** Sets the move selector builder.
	 * <br>When {@link #apply(MoveGenerator)} is called, the move selector choose the move in the search results.
	 * <br>By default, it choose randomly a move in the best moves. 
	 * @param moveSelectorBuilder The new move selector builder.
	 */
	public void setMoveSelectorBuilder(Function<B, MoveSelector<M,IterativeDeepeningSearch<M>>> moveSelectorBuilder) {
		this.moveSelectorBuilder = moveSelectorBuilder;
	}

	public EngineEventLogger<M, B> getLogger() {
		return logger;
	}

	/** Sets the logger.
	 * @param logger The new logger (The default one does nothing)
	 */
	public void setLogger(EngineEventLogger<M, B> logger) {
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
	
	/** Should be called when a new game is started.
	 * <br>The default implementation call the {@link MoveLibrary#newGame} and {@link TranspositionTable#newGame} methods 
	 */
	public void newGame() {
		if (movesLibrary!=null) {
			movesLibrary.newGame();
		}
		if (transpositionTable!=null) {
			transpositionTable.newGame();
		}
	}

	@Override
	public M apply(B board) {
		M move = movesLibrary==null ? null : movesLibrary.apply(board).orElse(null);
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
		logger.logSearchStart(board, this);
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
				logger.logSearchEnd(board, rs);
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
