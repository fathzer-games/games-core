package com.fathzer.games.perft;

import com.fathzer.games.MoveGenerator;

@FunctionalInterface
public interface TestableMoveGeneratorBuilder<M, B extends MoveGenerator<M>> {
	B fromFEN(String fen);
}
