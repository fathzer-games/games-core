package com.fathzer.games;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class UndoMoveManagerTest {
	
	private static class MyGame {
		private final UndoMoveManager<AtomicInteger> bckMgr;
		private int value;
		private int counter = 0;
		private int saveNumber = 0;
		private int backupContainerNumber = 0;
		
		MyGame() {
			this.bckMgr = new UndoMoveManager<>(this::buildBackup, this::save, i -> value=i.get());
		}
		
		int move() {
			bckMgr.beforeMove();
			value = ++counter;
			return value;
		}
		
		int undo() {
			bckMgr.undo();
			return value;
		}
		
		private void save(AtomicInteger backup) {
			backup.set(value);
			saveNumber++;
		}
		
		private AtomicInteger buildBackup() {
			backupContainerNumber++;
			return new AtomicInteger();
		}
	}

	@Test
	void test() {
		final MyGame game = new MyGame();
		assertThrows(IllegalStateException.class, ()-> game.undo());
		
		assertEquals(0, game.value); // Initial state will be save as index 0
		assertEquals(1, game.move());
		assertEquals(2, game.move());
		assertEquals(2,game.saveNumber);
		assertEquals(2,game.backupContainerNumber);
		assertEquals(1, game.undo());
		assertEquals(0, game.undo());
		assertThrows(IllegalStateException.class, ()-> game.undo());
		assertEquals(3, game.move());
		assertEquals(2,game.saveNumber,"A useless save was detected"); // Initial position does not need to be saved 
		assertEquals(4, game.move());
		assertEquals(5, game.move());
		assertEquals(4, game.undo());
		assertEquals(6, game.move());
		assertEquals(4, game.undo());
		assertEquals(4,game.saveNumber,"A useless save was detected");
		assertEquals(3, game.backupContainerNumber,"A useless backup container allocation was detected");
	}

}
