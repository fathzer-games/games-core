package com.fathzer.games.ai;

import com.fathzer.games.MoveGenerator;

public interface QuiescePolicy<M, B extends MoveGenerator<M>> {
	int quiesce(SearchContext<M, B> ctx, int alpha, int beta);
}
