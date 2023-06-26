package com.fathzer.games.perft;

import java.util.function.Supplier;

import com.fathzer.games.MoveGenerator;

public interface TestableMoveGeneratorSupplier<M> extends Supplier<MoveGenerator<M>> {
	void setStartPosition(String position);
}
