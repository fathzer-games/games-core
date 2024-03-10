package com.fathzer.games.ai.evaluation;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.SearchContext;

public interface QuiesceEvaluator<M, B extends MoveGenerator<M>> {
	int evaluate(SearchContext<M, B> ctx, int depth, int alpha, int beta);
}
