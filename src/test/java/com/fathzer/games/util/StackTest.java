package com.fathzer.games.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class StackTest {
	
	private static class MyGame {
		private final Stack<AtomicInteger> bckMgr;
		private int value;
		private int counter = 0;
		private int backupContainerNumber = 0;
		
		MyGame() {
			this.bckMgr = new Stack<>(this::buildBackup);
		}
				
		int move() {
			save(bckMgr.get());
			bckMgr.next();
			value = ++counter;
			return value;
		}
		
		int undo() {
			bckMgr.previous();
			value=bckMgr.get().get();
			return value;
		}
		
		private void save(AtomicInteger backup) {
			backup.set(value);
		}
		
		private AtomicInteger buildBackup() {
			backupContainerNumber++;
			return new AtomicInteger();
		}
	}

	@Test
	void test() {
		final MyGame game = new MyGame();
		assertThrows(NoSuchElementException.class, game::undo);
		
		assertEquals(0, game.value); // Initial state will be save as index 0
		assertEquals(1, game.move());
		assertEquals(2, game.move());
		assertEquals(2,game.backupContainerNumber);
		assertEquals(1, game.undo());
		assertEquals(0, game.undo());
		assertThrows(NoSuchElementException.class, game::undo);
		assertEquals(3, game.move());
		assertEquals(4, game.move());
		assertEquals(5, game.move());
		assertEquals(4, game.undo());
		assertEquals(6, game.move());
		assertEquals(4, game.undo());
		assertEquals(3, game.backupContainerNumber,"A useless backup container allocation was detected");
	}

}
