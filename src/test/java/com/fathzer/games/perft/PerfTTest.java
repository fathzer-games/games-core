package com.fathzer.games.perft;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static java.time.Duration.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.chess.ChessLibMoveGenerator;
import com.fathzer.games.util.PhysicalCores;
import com.github.bhlangonijr.chesslib.move.Move;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PerfTTest {
	private static final int CORES = Math.max(2, PhysicalCores.count());
	private static ExecutorService mt;
	private static ExecutorService fjp;

	static enum Services {
		SINGLE_THREAD,
		MULTI_THREAD,
		FORK_JOIN;

		public ExecutorService getExecutor() {
			switch(this) {
				case SINGLE_THREAD: return null;
				case MULTI_THREAD: return mt;
				case FORK_JOIN: return fjp;
				default: throw new IllegalArgumentException("Unknown service: "+this);
			}
		}
	}

	@BeforeAll
	static void setUp() {
		setDefaultPollInterval(ofMillis(25));
		setDefaultPollDelay(ofMillis(25));
		mt = Executors.newFixedThreadPool(CORES);
		fjp = new ForkJoinPool(CORES);
	}
	
	@AfterAll
	static void tearDown() {
		fjp.shutdown();
		mt.shutdown();
		reset();
	}
	
	@Test
	void perftParser() throws IOException {
		try (InputStream stream = getClass().getResourceAsStream("/com/fathzer/jchess/perft/Perft960.txt")) {
			final List<PerfTTestData> tests = new PerfTParser().withStartPositionPrefix("position fen").withStartPositionCustomizer(s -> s+" 0 1").read(stream, StandardCharsets.UTF_8);
			assertTrue(tests.size()>=960);
		}
	}

	@Test
	void test() {
		PerfTTestData test = new PerfTTestData("myTest", "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
		final ChessLibMoveGenerator mg = new ChessLibMoveGenerator(test.getStartPosition(), x->null);
		PerfTBuilder<Move> builder = new PerfTBuilder<>();
		final PerfTResult<Move> result = builder.build(mg, 3).get();
		assertEquals(13744, result.getNbLeaves());
		
		builder.setExecutor(mt);
		final PerfTResult<Move> result2 = builder.build(mg, 3).get();
		assertEquals(result.getNbLeaves(), result2.getNbLeaves());
		assertEquals(result.getNbMovesFound(), result2.getNbMovesFound());
		assertEquals(result.getNbMovesMade(), result2.getNbMovesMade());

		builder.setExecutor(fjp);
		final PerfTResult<Move> result3 = builder.build(mg, 3).get();
		assertEquals(result.getNbLeaves(), result3.getNbLeaves());
		assertEquals(result.getNbMovesFound(), result3.getNbMovesFound());
		assertEquals(result.getNbMovesMade(), result3.getNbMovesMade());

		assertThrows(IllegalArgumentException.class, () -> builder.build(mg, 0));
		assertThrows(IllegalArgumentException.class, () -> builder.build(null, 2));
	}

	@ParameterizedTest
	@EnumSource(Services.class)
	void pseudoLegalMovesShouldBeFiltered(Services service) {
		final int leavesCount = 44;
		final ChessLibMoveGenerator board = new ChessLibMoveGenerator("r1b3r1/p2p1pk1/np6/4q1p1/N1P2RPp/1P1PP3/P1RK3P/1QN1B1n1 b - - 0 1", x->null);
		final PerfTBuilder<Move> builder = new PerfTBuilder<>();
		builder.setExecutor(service.getExecutor());
		assertEquals(leavesCount, builder.build(board, 1).get().getNbLeaves());
		assertEquals(leavesCount, builder.build(board, 2).get().getDivides().size());
		builder.setLegalMoves(true);
		assertEquals(leavesCount, builder.build(board, 1).get().getNbLeaves());
		assertEquals(leavesCount, builder.build(board, 2).get().getDivides().size());
		builder.setPlayLeaves(false);
		assertEquals(leavesCount, builder.build(board, 1).get().getNbLeaves());
		assertEquals(leavesCount, builder.build(board, 2).get().getDivides().size());
}

	@ParameterizedTest
	@EnumSource(Services.class)
	void matMovesShouldBeInDivides(Services service) {
		final ChessLibMoveGenerator board = new ChessLibMoveGenerator("rnbqkbnr/pppp1ppp/4p3/8/Q4PP1/1NP5/PP1PP2P/2BRKBNR b Kkq - 0 1", x->null);
		final PerfTBuilder<Move> builder = new PerfTBuilder<>();
		builder.setExecutor(service.getExecutor());
		var result = builder.build(board, 2).get();
		var matMove = result.getDivides().stream().filter(d -> d.getMove().toString().equals("d8h4")).findFirst();
		assertFalse(matMove.isEmpty());
		assertEquals(0, matMove.get().getNbLeaves());
	}

	@Test
	@EnabledIfSystemProperty(named="PerfTperf", matches="(?i)true")
	void testPerf() {
		PerfTTestData test = new PerfTTestData("myTest", "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
		final ChessLibMoveGenerator mg = new ChessLibMoveGenerator(test.getStartPosition(), x->null);
		final PerfTest<Move> tester = new PerfTest<>(new PerfTBuilder<>(), mg, 6);

		tester.init();
		tester.run(fjp);
		tester.run(mt);
		tester.run(mt);
		tester.run(fjp);
	}

	private static class PerfTest<M> {
		private final PerfTBuilder<M> builder;
		private final MoveGenerator<M> board;
		private final int depth;
		private PerfTResult<M> result;

		PerfTest(PerfTBuilder<M> builder, MoveGenerator<M> board, int depth) {
			this.builder = builder;
			this.board = board;
			this.depth = depth;
		}
		private void init() {
			builder.setExecutor(null);
			long start = System.currentTimeMillis();
			result = builder.build(board, depth).get();
			System.out.println(String.format("Single threaded: %dms",System.currentTimeMillis()-start));
		}
		private void run(ExecutorService exec) {
			builder.setExecutor(exec);
			long start = System.currentTimeMillis();
			final PerfTResult<M> result2 = builder.build(board, depth).get();
			final String prefix = exec instanceof ForkJoinPool ? "Forked" : "Multi threaded";
			System.out.println(String.format("%s on %d threads: %dms",prefix, CORES, System.currentTimeMillis()-start));
			assertEquals(result.getNbLeaves(), result2.getNbLeaves());
			assertEquals(result.getNbMovesFound(), result2.getNbMovesFound());
			assertEquals(result.getNbMovesMade(), result2.getNbMovesMade());
		}
	}
	
	@ParameterizedTest
	@EnumSource(Services.class)
	void interruption(Services service) {
		final PerfTTestData test = new PerfTTestData("myTest", "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
		final ChessLibMoveGenerator mg = new ChessLibMoveGenerator(test.getStartPosition(), x->null);
		final PerfTBuilder<Move> builder = new PerfTBuilder<>();
		builder.setExecutor(service.getExecutor());
		// Test interruption with thread.interrupt()
		interruption(builder.build(mg, 100), (t,p) -> t.interrupt());
		// Test interruption with PerfT.interrupt()
		interruption(builder.build(mg, 100), (t,p) -> p.interrupt());
	}

	<M> void interruption(PerfT<M> perft, BiConsumer<Thread, PerfT<M>> interruptor) {
		final AtomicReference<PerfTResult<M>> result = new AtomicReference<>();
		Thread t = new Thread(() -> result.set(perft.get()));
        t.start();
        await().pollDelay(ofMillis(0)).pollInterval(ofMillis(30)).until(() -> true);
        interruptor.accept(t, perft);
        await().atMost(ofMillis(200)).until(() -> result.get()!=null);
        assertFalse(t.isAlive());
        assertTrue(perft.isInterrupted());
        assertTrue(result.get().isInterrupted());
	}
}
