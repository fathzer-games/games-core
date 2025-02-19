package com.fathzer.games;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StatusTest {
	@Test
	void test() {
		assertNull(Status.DRAW.winner());
		assertNull(Status.PLAYING.winner());
		assertEquals(Color.WHITE, Status.WHITE_WON.winner());
		assertEquals(Color.BLACK, Status.BLACK_WON.winner());
	}
}
