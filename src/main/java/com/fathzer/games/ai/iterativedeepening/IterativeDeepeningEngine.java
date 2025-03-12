package com.fathzer.games.ai.iterativedeepening;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchContext;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.SearchStatistics;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.transposition.TTAi;
import com.fathzer.games.ai.transposition.TranspositionTable;
import com.fathzer.games.movelibrary.MoveLibrary;
import com.fathzer.games.util.exec.ExecutionContext;

/** An engine that iteratively deepens the search. 
 * @param <M> The class that represents a move
 * @param <B> The class that represents the move generator 
 */
public class IterativeDeepeningEngine<M, B extends MoveGenerator<M>> {
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
		/** Called when the move is choosen from the library.
		 * @param board The board
		 * @param move The move
		 */
		default void logLibraryMove(B board, EvaluatedMove<M> move) {
			// Does nothing by default
		}

		/** Called when the search starts (at the beginning of {@link #doSearch(MoveGenerator, List)}).
		 * @param board The board
		 * @param engine The engine
		*/
		default void logSearchStart(B board, IterativeDeepeningEngine<M, B> engine) {
			// Does nothing by default
		}
 
		@Deprecated
		default void logMoveChosen(B board, EvaluatedMove<M> evaluatedMove) {
			// Does nothing by default
		}

		/** Called when the search ends (at the end of {@link #doSearch(MoveGenerator, List)}).
		 * @param board The board
		 * @param result The result
		*/
		default void logSearchEnd(B board, SearchHistory<M> result) {
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
	private TranspositionTable<M, B> transpositionTable;
	private int parallelism;
	private EngineEventLogger<M, B> logger;
	private IterativeDeepeningSearch<M> rs;
	private AtomicBoolean running;
	
	/** Constructor
	 * <br>By default, the parallelism of the search is 1, the event logger logs nothing and the engine select randomly a move in the best move list.
	 * @param deepeningPolicy The policy to decide what move to deepen, how to merge results at different depth, etc...
	 * @param tt A transposition table used across different depth 
	 * @param evaluatorSupplier An evaluation function supplier
	 * @see #setParallelism(int)
	 * @see #setLogger(EngineEventLogger)
	 */
	public IterativeDeepeningEngine(DeepeningPolicy deepeningPolicy, TranspositionTable<M, B> tt, Supplier<Evaluator<M, B>> evaluatorSupplier) {
		this.parallelism = 1;
		this.transpositionTable = tt;
		this.evaluatorSupplier = evaluatorSupplier;
		this.running = new AtomicBoolean();
		this.logger = new Mute<>();
		this.deepeningPolicy = deepeningPolicy;
	}
	
	/** Interrupts the current search if any.
	 * <br>Does nothing if there's no search running.
	 */
	public void interrupt() {
		if (running.get()) {
			rs.interrupt();
		}
	}

	/** Gets the number of threads used to perform the searches.
	 * @return the number of threads used to perform the search (default is 1)
	 */
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

	/** Gets the supplier of the evaluation function.
	 * @return the supplier of the evaluation function
	 */
	public Supplier<Evaluator<M, B>> getEvaluationSupplier() {
		return this.evaluatorSupplier;
	}

	/** Sets the supplier of the evaluation function.
	 * @param evaluatorSupplier The supplier of the evaluation function
	 */
	public void setEvaluatorSupplier(Supplier<Evaluator<M, B>> evaluatorSupplier) {
		this.evaluatorSupplier = evaluatorSupplier;
	}

	/** Gets the logger.
	 * @return the logger (The default one does nothing - see {@link Mute})
	 */
	public EngineEventLogger<M, B> getLogger() {
		return logger;
	}

	/** Sets the logger.
	 * @param logger The new logger (The default one does nothing)
	 */
	public void setLogger(EngineEventLogger<M, B> logger) {
		this.logger = logger;
	}

	/** Gets the transposition table.
	 * @return the transposition table or null, the default value, if no transposition table is set
	 */
	public TranspositionTable<M, B> getTranspositionTable() {
		return transpositionTable;
	}
	
	/** Sets the transposition table.
	 * @param transpositionTable The transposition table or null to use no transposition table (not recommended)
	 */
	public void setTranspositionTable(TranspositionTable<M, B> transpositionTable) {
		this.transpositionTable = transpositionTable;
	}

	/** Gets the deepening policy.
	 * @return the deepening policy
	 */
	public DeepeningPolicy getDeepeningPolicy() {
		return deepeningPolicy;
	}

	/** Sets the deepening policy.
	 * @param policy The new deepening policy
	 */
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

	/** Searches for the best moves.
	 * <br>By default, this method will return the moves from the library if any, otherwise the result of the {@link #doSearch(MoveGenerator, List)} method.
	 * @param board The board
	 * @param searchedMoves A restricted list of moves to search, null to search all possible moves
	 * @return A search history
	 */
	public SearchHistory<M> getBestMoves(B board, List<M> searchedMoves) {
		final SearchHistory<M> result = new SearchHistory<>(deepeningPolicy);
		//TODO Filter library with candidates + return more than one move if search params requires more than one move
		EvaluatedMove<M> move = movesLibrary==null ? null : movesLibrary.apply(board).orElse(null);
		if (move!=null) {
			logger.logLibraryMove(board, move);
			result.add(Collections.singletonList(move), 0);
			return result;
		}
		final IterativeDeepeningSearch<M> search = doSearch(board, searchedMoves);
		return search.getSearchHistory();
	}

	/** Searches for the best moves.
	 * <br>Calls {@link #getBestMoves(MoveGenerator, List)} with null as second parameter.
	 * @param board The board
	 * @return A search history
	 */
	public SearchHistory<M> getBestMoves(B board) {
		return getBestMoves(board, null);
	}
	
	/** Performs the iterative deepening search.
	 * @param board The board
	 * @param searchedMoves A restricted list of moves to search, null to search all possible moves
	 * @return A search history
	 * @throws IllegalStateException If a search is already running
	 */
	protected IterativeDeepeningSearch<M> doSearch(B board, List<M> searchedMoves) {
		if (!running.compareAndSet(false, true)) {
			throw new IllegalStateException();
		}
		try {
			logger.logSearchStart(board, this);
			if (transpositionTable!=null) {
				transpositionTable.newPosition(board);
			}
			try (ExecutionContext<SearchContext<M,B>> context = buildExecutionContext(board)) {
				final TTAi<M, B> internal = buildAI(context);
				internal.setTranspositonTable(transpositionTable);
				rs = new IterativeDeepeningSearch<>(internal, deepeningPolicy);
				rs.setEventLogger(logger);
				rs.setSearchedMoves(searchedMoves);
				final List<EvaluatedMove<M>> result = rs.getSearchHistory().getAccurateMoves();
				for (EvaluatedMove<M> ev:result) {
					ev.setPvBuilder(m -> getTranspositionTable().collectPV(board, m, deepeningPolicy.getDepth()));
				}
				logger.logSearchEnd(board, rs.getSearchHistory());
				return rs;
			}
		} finally {
			running.set(false);
		}
	}
	
	/** Builds the execution context used for a search.
	 * <br>The default implementation builds a new execution context with the given board and evaluator supplier using {@link #getParallelism()} threads. 
	 * @param board The board to search
	 * @return The execution context
	 */
	protected ExecutionContext<SearchContext<M,B>> buildExecutionContext(B board) {
		final SearchContext<M, B> context = SearchContext.get(board, evaluatorSupplier);
		return ExecutionContext.get(getParallelism(), context);
	}
	
	/** Builds the AI used to search best moves at different depth. 
	 * @param context An execution context that can be used by the AI.
	 * @return An AI that supports transposition tables. THe default implementation returns a Negamax instance.
	 */
	protected TTAi<M, B> buildAI(ExecutionContext<SearchContext<M,B>> context) {
		return new Negamax<>(context);
	}
}
