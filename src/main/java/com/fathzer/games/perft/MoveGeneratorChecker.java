package com.fathzer.games.perft;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.util.exec.ContextualizedExecutor;

/** A class that tests a {@link MoveGenerator} against a {@link PerfTBuilder} test Data set.
 * <br>The test executes a list of {@link PerfTBuilder} test and returns the total number of moves found at a specified depth.
 * <br>It can also be used to test speed of move generators. It returns the number of moves found, dividing this result by the time spent in the test
 * you will obtain a number of moves found per second.
 */
public class MoveGeneratorChecker {
	/** Details of PerfT count error.
	 */
	public static class PerfTCountError implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private final String startPosition;
		private final long expectedCount;
		private final long actualCount;
		
		private PerfTCountError(String startPosition, long expectedCount, long actualCount) {
			super();
			this.startPosition = startPosition;
			this.expectedCount = expectedCount;
			this.actualCount = actualCount;
		}

		/** Gets the start position.
		 * @return A string
		 */
		public String getStartPosition() {
			return startPosition;
		}

		/** Gets the expected count.
		 * @return a long
		 */
		public long getExpectedCount() {
			return expectedCount;
		}

		/** Gets the actual count.
		 * @return a long
		 */
		public long getActualCount() {
			return actualCount;
		}
	}
	
	/** An exception throws when PerfT returns an unexpected move count.
	 */
	public static class PerfTCountException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final PerfTCountError countError;

		private PerfTCountException(PerfTCountError countError) {
			super("Error for "+countError.getStartPosition()+" expected "+countError.getExpectedCount()+" got "+countError.getActualCount());
			this.countError = countError;
		}

		/** Gets the count error that triggers the exception.
		 * @return A perft count error
		 */
		public PerfTCountError getCountError() {
			return countError;
		}
	}
	
	private final Collection<PerfTTestData> tests;
	private Consumer<PerfTCountError> countErrorManager = e -> { throw new PerfTCountException(e); };
	private Consumer<RuntimeException> errorManager = e -> { throw e; };
	private AtomicReference<PerfT<?>> current;
	private volatile boolean cancelled;
	private AtomicBoolean running = new AtomicBoolean();
	
	/** Constructor.
	 * @param tests The data set to use to perform the tests
	 */
	public MoveGeneratorChecker(Collection<PerfTTestData> tests) {
		this.tests = tests;
	}
	
	/** Sets the count error manager.
	 * @param countErrorManager A consumer that will receive count errors (when the number of moves found by PerfT is not the expected count in data set.
	 * <br>By default, it throws a {@link PerfTCountException} that is throws by {@link #run(TestableMoveGeneratorSupplier, int, boolean, boolean, int)}.
	 */
	public void setCountErrorManager(Consumer<PerfTCountError> countErrorManager) {
		this.countErrorManager = countErrorManager;
	}

	/** Sets the error manager.
	 * @param errorManager A consumer that will receive exceptions that may occurs during the search.
	 * <br>By default, these exceptions are not caught. So, {@link #run(TestableMoveGeneratorSupplier, int, boolean, boolean, int)} ends throws the exception.
	 * <br>Please note the exceptions that can be sent by count error manager are not concerned by this method.
	 * @see #setCountErrorManager(Consumer)
	 */
	public void setErrorManager(Consumer<RuntimeException> errorManager) {
		this.errorManager = errorManager;
	}

	/** Executes the test
	 * @param <M> The class that represents a move
	 * @param engine The tested engine.
	 * @param depth The search depth.
	 * @param legalMoves true to play only legal moves.
	 * @param playLeaves true to play leaves move (ignored if <i>legalMoves</i> is false. See {@link PerfTBuilder#setPlayLeaves(boolean)} comment).
	 * @param parallelism The number of threads to use to perform the search 
	 * @return The number of moves found.
	 */
	public <M, B extends MoveGenerator<M>> long run(FromPositionMoveGeneratorBuilder<M, B> engine, int depth, boolean legalMoves, boolean playLeaves, int parallelism) {
		if (!running.compareAndSet(false, true)) {
			throw new IllegalStateException("A test is already running");
		}
		cancelled = false;
		long count = 0;
		try (ContextualizedExecutor<MoveGenerator<M>> threads = new ContextualizedExecutor<>(parallelism)) {
			final PerfTBuilder<M> perfT = new PerfTBuilder<>();
			perfT.setExecutor(threads);
			perfT.setPlayLeaves(playLeaves);
			perfT.setLegalMoves(legalMoves);
			for (PerfTTestData test : tests) {
				if (test.getSize()>=depth) {
					try {
						B generator = engine.fromPosition(test.getStartPosition());
						synchronized(this) {
							if (cancelled) {
								break;
							} else {
								current.set(perfT.build(generator, depth));
							}
						}
						count += doTest(test, depth);
					} catch (Exception e) {
						errorManager.accept(new RuntimeException("Exception for "+test.getStartPosition(), e));
						interrupt();
					}
				}
			}
		}
		return count;
	}
	
	private <M> long doTest(PerfTTestData test, int depth) {
		@SuppressWarnings("unchecked")
		final long count = ((PerfT<M>)current.get()).call().getNbLeaves();
		final long expected = test.getExpectedLeaveCount(depth);
		if (count != expected && !cancelled) {
			countErrorManager.accept(new PerfTCountError(test.getStartPosition(), expected, count));
		}
		return count;
	}
	
	/**
	 * Interrupts the test. <br>
	 */
	public synchronized void interrupt() {
		this.cancelled = true;
		if (current!=null) {
			current.get().interrupt();
		}
	}
}
