package com.fathzer.games.nim;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;

/** A <a href="https://en.wikipedia.org/wiki/Nim_game">Nim game</a> move generator. */
public class NimGameMoveGenerator implements MoveGenerator<Integer> {
	private boolean whiteToMove;
	private int currentCount;
	private final LinkedList<Integer> previous = new LinkedList<>();

	public NimGameMoveGenerator(int currentCount,boolean whiteToMove) {
		if (currentCount<0) {
			throw new IllegalArgumentException();
		}
		this.whiteToMove = whiteToMove;
		this.currentCount = currentCount;
	}

	@Override
	public Status getEndGameStatus() {
		return whiteToMove? Status.WHITE_WON:Status.BLACK_WON;
	}

	@Override
	public List<Integer> getMoves() {
		if (currentCount == 0) {
			return Collections.emptyList();
		}
		int max = Math.min(3, currentCount);
		return IntStream.range(1, max+1).boxed().toList();
	}

	@Override
	public boolean isWhiteToMove() {
		return whiteToMove;
	}

	@Override
	public boolean makeMove(Integer move, MoveConfidence confidence) {
		if (currentCount<move || move<=0 ) {
			return false;
		}
		whiteToMove = !whiteToMove;
		previous.add(currentCount);
		currentCount -= move;
		return true;
	}

	@Override
	public void unmakeMove() {
		whiteToMove = !whiteToMove;
		currentCount += previous.removeLast();
	}

	@Override
	public MoveGenerator<Integer> fork() {
		return new NimGameMoveGenerator(currentCount, whiteToMove);
	}
}