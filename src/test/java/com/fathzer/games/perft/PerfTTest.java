package com.fathzer.games.perft;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static java.time.Duration.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.chess.ChessLibMoveGenerator;
import com.fathzer.games.util.exec.ContextualizedExecutor;
import com.github.bhlangonijr.chesslib.move.Move;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PerfTTest {
	@BeforeAll
	static void setUp() {
		setDefaultPollInterval(ofMillis(25));
		setDefaultPollDelay(ofMillis(25));
	}
	
	@AfterAll
	static void tearDown() {
		reset();
	}

	@Test
	void test() throws IOException {
		PerfTTestData test = new PerfTTestData("myTest", "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
		final ChessLibMoveGenerator mg = new ChessLibMoveGenerator(test.getStartPosition(), x->null);
		PerfTBuilder<Move> builder = new PerfTBuilder<>();
		final PerfTResult<Move> result = builder.build(mg, 3).call();
		assertEquals(13744, result.getNbLeaves());

		try (ContextualizedExecutor<MoveGenerator<Move>> ctx = new ContextualizedExecutor<>(4)) {
			builder.setExecutor(ctx);
			final PerfTResult<Move> result2 = builder.build(mg, 3).call();
			assertEquals(result.getNbLeaves(), result2.getNbLeaves());
		}
		
		try (InputStream stream = getClass().getResourceAsStream("/com/fathzer/jchess/perft/Perft960.txt")) {
			final List<PerfTTestData> tests = new PerfTParser().withStartPositionPrefix("position fen").withStartPositionCustomizer(s -> s+" 0 1").read(stream, StandardCharsets.UTF_8);
			assertTrue(tests.size()>=960);
		}
	}

	@Test
	void testInterruption() {
		PerfTTestData test = new PerfTTestData("myTest", "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
		final ChessLibMoveGenerator mg = new ChessLibMoveGenerator(test.getStartPosition(), x->null);
		PerfTBuilder<Move> builder = new PerfTBuilder<>();
		// Test interruption with thread.interrupt()
		testInterruption(builder.build(mg, 100), (t,p) -> t.interrupt());
		// Test interruption with PerfT.interrupt()
		testInterruption(builder.build(mg, 100), (t,p) -> p.interrupt());
		
		try (ContextualizedExecutor<MoveGenerator<Move>> ctx = new ContextualizedExecutor<>(4)) {
			builder.setExecutor(ctx);
            // Test interruption with thread.interrupt()
            testInterruption(builder.build(mg, 100), (t,p) -> t.interrupt());
            // Test interruption with PerfT.interrupt()
            testInterruption(builder.build(mg, 100), (t,p) -> p.interrupt());
		}
	}
	
	<M> void testInterruption(PerfT<M> perft, BiConsumer<Thread, PerfT<M>> interruptor) {
		final AtomicReference<PerfTResult<M>> result = new AtomicReference<>();
		Thread t = new Thread(() -> {
			result.set(perft.call());
		});
        t.start();
        await().pollDelay(ofMillis(0)).pollInterval(ofMillis(30)).until(() -> true);
        interruptor.accept(t, perft);
        await().atMost(ofMillis(50)).until(() -> result.get()!=null);
        assertTrue(result.get().isInterrupted());
        assertFalse(t.isAlive());
	}
}
