package com.fathzer.games;

import static org.junit.jupiter.api.Assertions.*;
import static com.fathzer.games.GameHistory.TerminationCause.*;
import static com.github.bhlangonijr.chesslib.Square.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.chess.ChessLibMoveGenerator;
import com.github.bhlangonijr.chesslib.move.Move;

class GameHistoryTest {

	@Test
	void test() {
		final ChessLibMoveGenerator board = new ChessLibMoveGenerator("rnb1k1nr/pp1p1ppp/2pqp3/8/2PP2Q1/6B1/P3PP1P/R2BKRN1 b Qkq - 0 1", x->null);
		final GameHistory<Move, ChessLibMoveGenerator> history = new GameHistory<>(board);
		assertThrows(IllegalArgumentException.class, () -> history.earlyEnd(null, NORMAL));
		assertThrows(IllegalArgumentException.class, () -> history.earlyEnd(Status.PLAYING, ADJUDICATION));
		assertThrows(IllegalArgumentException.class, () -> history.earlyEnd(Status.DRAW, null));
		assertThrows(IllegalArgumentException.class, () -> history.earlyEnd(Status.DRAW, UNTERMINATED));
		assertEquals(UNTERMINATED, history.getTerminationCause());
		assertFalse(history.add(new Move(D6, D7)));
		assertEquals(UNTERMINATED, history.getTerminationCause());
		assertTrue(history.add(new Move(D6, B4)));
		assertEquals(NORMAL, history.getTerminationCause());
		assertThrows(IllegalStateException.class, () -> history.earlyEnd(Status.DRAW, NORMAL));
	}

}
