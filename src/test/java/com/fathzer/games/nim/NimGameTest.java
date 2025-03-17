package com.fathzer.games.nim;

import static org.junit.jupiter.api.Assertions.*;
import static com.fathzer.games.MoveGenerator.MoveConfidence.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;

class NimGameTest {

	@Test
	void test() {
		MoveGenerator<Integer> board = new NimGameMoveGenerator(0, true);
		assertEquals(Status.WHITE_WON, board.getEndGameStatus());
		assertThrows(IllegalArgumentException.class, ()->new NimGameMoveGenerator(-1, true));
		
		board = new NimGameMoveGenerator(3, false);
		assertFalse(board.isWhiteToMove());
		assertEquals(Set.of(1,2,3), new HashSet<>(board.getMoves()));

		assertFalse(board.makeMove(4, UNSAFE));
		assertFalse(board.makeMove(0, UNSAFE));

		// black removes 2
		assertTrue(board.makeMove(2, UNSAFE));
		assertEquals(Set.of(1), new HashSet<>(board.getMoves()));

		assertFalse(board.makeMove(2, UNSAFE));
		// white removes last
		assertTrue(board.makeMove(1, UNSAFE));
		assertEquals(Status.BLACK_WON, board.getEndGameStatus());
		
		board.unmakeMove();
		assertEquals(Set.of(1), new HashSet<>(board.getMoves()));
		assertTrue(board.isWhiteToMove());
		board.unmakeMove();
		assertEquals(Set.of(1,2,3), new HashSet<>(board.getMoves()));
		assertFalse(board.isWhiteToMove());
	}

}
