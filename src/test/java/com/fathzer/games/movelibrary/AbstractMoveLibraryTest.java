package com.fathzer.games.movelibrary;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation;

class AbstractMoveLibraryTest {
	private static Random mockedRnd;
	
	@BeforeAll
	static void setUp() {
		mockedRnd = mock(Random.class);
	}
	
	private static class MyMoveLibrary extends AbstractMoveLibrary<String, String, String> {
		private final String expected;
		private final List<String> results;
		private boolean newGameCalled;
		
		public MyMoveLibrary(String expected, List<String> results) {
			this.expected = expected;
			this.results = results;
		}
		
		@Override
		protected List<String> getRecords(String board) {
			return expected.equals(board)?results:Collections.emptyList();
		}

		@Override
		protected EvaluatedMove<String> toEvaluatedMove(String board, String move) {
			return new EvaluatedMove<>(move, Evaluation.UNKNOWN);
		}

		@Override
		protected long getWeight(String move) {
			try {
				return Long.parseLong(move)+1;
			} catch (NumberFormatException e) {
				return super.getWeight(move);
			}
		}

		@Override
		public void newGame() {
			newGameCalled = true;
			super.newGame();
		}
	}

	@Test
	void test() throws Exception {
		assertEquals("0",AbstractMoveLibrary.firstMoveSelector().apply(Arrays.asList("0","1","2")));

		Field field = AbstractMoveLibrary.class.getDeclaredField("rnd");
	    field.setAccessible(true);
	    field.set(null, mockedRnd);

	    when(mockedRnd.nextInt(anyInt())).thenReturn(1);
		assertEquals("1",AbstractMoveLibrary.randomMoveSelector().apply(Arrays.asList("0","1","2")));
	    
	    MyMoveLibrary lib = new MyMoveLibrary("ok", Arrays.asList("0","1","2","3","4"));
	    final MyMoveLibrary next = new MyMoveLibrary("ko", Arrays.asList("x"));
		lib.setNext(next);
	    
		assertTrue(lib.apply("unknown").isEmpty());
		
	    when(mockedRnd.nextInt(1)).thenReturn(0);
		assertEquals("x", lib.apply("ko").get().getMove());
	    
	    lib.setMoveSelector(lib.weightedMoveSelector());
	    when(mockedRnd.nextLong(15)).thenReturn(4L);
	    assertEquals("2", lib.apply("ok").get().getMove());
	    assertEquals(5, lib.getMoves("ok").size());
	    assertEquals(1, lib.getMoves("ko").size());
	    assertTrue(lib.getMoves("unknown").isEmpty());
	    
	    lib.newGame();
	    assertTrue(lib.newGameCalled);
	    assertTrue(next.newGameCalled);
	}
}
