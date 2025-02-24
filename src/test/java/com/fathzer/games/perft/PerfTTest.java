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

import com.fathzer.games.chess.ChessLibMoveGenerator;
import com.fathzer.games.util.PhysicalCores;
import com.github.bhlangonijr.chesslib.move.Move;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class PerfTTest {
	private static final int CORES = Math.max(2, PhysicalCores.count());
	private static ExecutorService exec;
	private static ExecutorService fjp;

	@BeforeAll
	static void setUp() {
		setDefaultPollInterval(ofMillis(25));
		setDefaultPollDelay(ofMillis(25));
		exec = Executors.newFixedThreadPool(CORES);
		fjp = new ForkJoinPool(CORES);
	}
	
	@AfterAll
	static void tearDown() {
		fjp.shutdown();
		exec.shutdown();
		reset();
	}

	@Test
	void test() {
		PerfTTestData test = new PerfTTestData("myTest", "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
		final ChessLibMoveGenerator mg = new ChessLibMoveGenerator(test.getStartPosition(), x->null);
		PerfTBuilder<Move> builder = new PerfTBuilder<>();
		final PerfTResult<Move> result = builder.build(mg, 3).get();
		assertEquals(13744, result.getNbLeaves());
		
		builder.setExecutor(exec);
		final PerfTResult<Move> result2 = builder.build(mg, 3).get();
		assertEquals(result.getNbLeaves(), result2.getNbLeaves());
		assertEquals(result.getNbMovesFound(), result2.getNbMovesFound());
		assertEquals(result.getNbMovesMade(), result2.getNbMovesMade());

		builder.setExecutor(fjp);
		final PerfTResult<Move> result3 = builder.build(mg, 3).get();
		assertEquals(result.getNbLeaves(), result3.getNbLeaves());
		assertEquals(result.getNbMovesFound(), result3.getNbMovesFound());
		assertEquals(result.getNbMovesMade(), result3.getNbMovesMade());
	}

	@Test
	void testPseudoLegalIsFiltered() {
		final ChessLibMoveGenerator board = new ChessLibMoveGenerator("r1b3r1/p2p1pk1/np6/4q1p1/N1P2RPp/1P1PP3/P1RK3P/1QN1B1n1 b - - 0 1", x->null);
		final PerfTBuilder<Move> builder = new PerfTBuilder<>();
		doPseudoLegalTest(builder, board);

		builder.setExecutor(exec);
		doPseudoLegalTest(builder, board);

		builder.setExecutor(fjp);
		doPseudoLegalTest(builder, board);
	}
	
	private void doPseudoLegalTest(PerfTBuilder<Move> builder, ChessLibMoveGenerator board) {
		final int leavesCount = 44;
		assertEquals(leavesCount, builder.build(board, 1).get().getNbLeaves());
		assertEquals(leavesCount, builder.build(board, 2).get().getDivides().size());
		builder.setLegalMoves(true);
		assertEquals(leavesCount, builder.build(board, 1).get().getNbLeaves());
		assertEquals(leavesCount, builder.build(board, 2).get().getDivides().size());
		builder.setPlayLeaves(false);
		assertEquals(leavesCount, builder.build(board, 1).get().getNbLeaves());
		assertEquals(leavesCount, builder.build(board, 2).get().getDivides().size());
		builder.setLegalMoves(false);
	}
	
	@Test
	@EnabledIfSystemProperty(named="PerfTperf", matches="(?i)true")
	void testPerf() {
		final int depth = 6;
		final int threadsCount = PhysicalCores.count();
		PerfTTestData test = new PerfTTestData("myTest", "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
		final ChessLibMoveGenerator mg = new ChessLibMoveGenerator(test.getStartPosition(), x->null);
		PerfTBuilder<Move> builder = new PerfTBuilder<>();
		long start = System.currentTimeMillis();
		final PerfTResult<Move> result = builder.build(mg, depth).get();
		System.out.println("Single threaded: "+(System.currentTimeMillis()-start)+" ms");
		
		{
			builder.setExecutor(fjp);
			start = System.currentTimeMillis();
			final PerfTResult<Move> result2 = builder.build(mg, depth).get();
			System.out.println(String.format("Forked on %d threads: %dms",threadsCount,System.currentTimeMillis()-start));
			assertEquals(result.getNbLeaves(), result2.getNbLeaves());
			assertEquals(result.getNbMovesFound(), result2.getNbMovesFound());
			assertEquals(result.getNbMovesMade(), result2.getNbMovesMade());
		}

		{
			builder.setExecutor(exec);
			start = System.currentTimeMillis();
			final PerfTResult<Move> result2 = builder.build(mg, depth).get();
			System.out.println(String.format("Multi threaded on %d threads: %dms",threadsCount,System.currentTimeMillis()-start));
			assertEquals(result.getNbLeaves(), result2.getNbLeaves());
			assertEquals(result.getNbMovesFound(), result2.getNbMovesFound());
			assertEquals(result.getNbMovesMade(), result2.getNbMovesMade());
		}


		{
			builder.setExecutor(exec);
			start = System.currentTimeMillis();
			final PerfTResult<Move> result2 = builder.build(mg, depth).get();
			System.out.println(String.format("Multi threaded on %d threads: %dms",threadsCount,System.currentTimeMillis()-start));
			assertEquals(result.getNbLeaves(), result2.getNbLeaves());
			assertEquals(result.getNbMovesFound(), result2.getNbMovesFound());
			assertEquals(result.getNbMovesMade(), result2.getNbMovesMade());
		}


		{
			builder.setExecutor(fjp);
			start = System.currentTimeMillis();
			final PerfTResult<Move> result2 = builder.build(mg, depth).get();
			System.out.println(String.format("Forked on %d threads: %dms",threadsCount,System.currentTimeMillis()-start));
			assertEquals(result.getNbLeaves(), result2.getNbLeaves());
			assertEquals(result.getNbMovesFound(), result2.getNbMovesFound());
			assertEquals(result.getNbMovesMade(), result2.getNbMovesMade());
		}
	}
	
	@Test
	void perftParser() throws IOException {
		try (InputStream stream = getClass().getResourceAsStream("/com/fathzer/jchess/perft/Perft960.txt")) {
			final List<PerfTTestData> tests = new PerfTParser().withStartPositionPrefix("position fen").withStartPositionCustomizer(s -> s+" 0 1").read(stream, StandardCharsets.UTF_8);
			assertTrue(tests.size()>=960);
		}
	}

	@Test
	void interruption() {
		PerfTTestData test = new PerfTTestData("myTest", "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
		final ChessLibMoveGenerator mg = new ChessLibMoveGenerator(test.getStartPosition(), x->null);
		PerfTBuilder<Move> builder = new PerfTBuilder<>();
		// Test interruption with thread.interrupt()
		interruption(builder.build(mg, 100), (t,p) -> t.interrupt());
		// Test interruption with PerfT.interrupt()
		interruption(builder.build(mg, 100), (t,p) -> p.interrupt());
		
		builder.setExecutor(exec);
        // Test interruption with thread.interrupt()
        interruption(builder.build(mg, 100), (t,p) -> t.interrupt());
        // Test interruption with PerfT.interrupt()
        interruption(builder.build(mg, 100), (t,p) -> p.interrupt());

		builder.setExecutor(fjp);
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
