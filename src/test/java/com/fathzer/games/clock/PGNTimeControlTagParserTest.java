package com.fathzer.games.clock;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PGNTimeControlTagParserTest {
	
	@Test
	void testWrongTags() {
		final PGNTimeControlTagParser parser = new PGNTimeControlTagParser();
		assertEquals(null ,parser.toClockSettings("-"));
		assertEquals("-", parser.toTag(null));
		assertThrows(IllegalArgumentException.class, () -> parser.toClockSettings(null));
		assertThrows(IllegalArgumentException.class, () -> parser.toClockSettings("?"));
		assertThrows(IllegalArgumentException.class, () -> parser.toClockSettings("300+5+4"));
		assertThrows(IllegalArgumentException.class, () -> parser.toClockSettings("40/20/300"));
		assertThrows(IllegalArgumentException.class, () -> parser.toClockSettings("300:30+30"));
	}

	@Test
	void test() {
		final PGNTimeControlTagParser parser = new PGNTimeControlTagParser();
		
		ClockSettings settings = parser.toClockSettings("300+5");
		assertEquals(300, settings.getInitialTime());
		assertEquals(5, settings.getIncrement());
		assertEquals(1, settings.getMovesNumberBeforeIncrement());
		assertNull(settings.getNext());
		assertEquals("300+5", parser.toTag(settings));
		
		settings = parser.toClockSettings("40/9000");
		assertEquals(9000, settings.getInitialTime());
		assertNull(settings.getNext());
		assertEquals(40, settings.getMovesNumberBeforeIncrement());
		assertEquals(9000, settings.getIncrement());
		assertFalse(settings.isCanAccumulate());
		assertEquals("40/9000", parser.toTag(settings));

		settings = parser.toClockSettings("40/9000:300+5");
		assertEquals(9000, settings.getInitialTime());
		assertEquals(0, settings.getIncrement());
		assertEquals(40, settings.getMovesNumberBeforeNext());
		ClockSettings nextSettings = settings.getNext();
		assertEquals(300, nextSettings.getInitialTime());
		assertEquals(5, nextSettings.getIncrement());
		assertEquals(1, nextSettings.getMovesNumberBeforeIncrement());
		assertNull(nextSettings.getNext());
		assertEquals("40/9000:300+5", parser.toTag(settings));
	}

	@Test
	void testExtensions() {
		final PGNTimeControlTagParser parser = new PGNTimeControlTagParser();
		ClockSettings settings = parser.toClockSettings("300+2/5");
		assertEquals(300, settings.getInitialTime());
		assertEquals(5, settings.getIncrement());
		assertEquals(2, settings.getMovesNumberBeforeIncrement());
		assertTrue(settings.isCanAccumulate());
		assertNull(settings.getNext());
		assertEquals("300+2/5", parser.toTag(settings));

		settings = parser.toClockSettings("20/3600+2/10:300+5");
		assertEquals(3600, settings.getInitialTime());
		assertEquals(10, settings.getIncrement());
		assertEquals(2, settings.getMovesNumberBeforeIncrement());
		assertEquals(20, settings.getMovesNumberBeforeNext());
		ClockSettings nextSettings = settings.getNext();
		assertEquals(300, nextSettings.getInitialTime());
		assertEquals(5, nextSettings.getIncrement());
		assertEquals(1, nextSettings.getMovesNumberBeforeIncrement());
		assertNull(nextSettings.getNext());
		assertEquals("20/3600+2/10:300+5", parser.toTag(settings));
	}
}
