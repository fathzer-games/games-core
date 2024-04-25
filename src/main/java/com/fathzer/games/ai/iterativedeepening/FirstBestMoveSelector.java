package com.fathzer.games.ai.iterativedeepening;

import java.util.List;

import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.moveselector.MoveSelector;

public class FirstBestMoveSelector<M> extends MoveSelector<M, SearchHistory<M>> {
	
	@Override
	public List<EvaluatedMove<M>> select(SearchHistory<M> history, List<EvaluatedMove<M>> result) {
		return super.select(history, filter(history, result));
	}

	protected List<EvaluatedMove<M>> filter(SearchHistory<M> history, List<EvaluatedMove<M>> bestMoves) {
		for (int i=history.getLength()-1;i>=0;i--) {
			final List<M> cut = history.getBestMoves(i).stream().map(EvaluatedMove::getContent).toList();
			bestMoves = getCandidates(bestMoves, cut);
			log(i, cut, bestMoves);
		}
		return bestMoves;
	}
	
	protected void log(int index, List<M> cut, List<EvaluatedMove<M>> result) {
		// Does nothing by default
	}
	
	private List<EvaluatedMove<M>> getCandidates(List<EvaluatedMove<M>> bestMoves, List<M> moves) {
		final List<EvaluatedMove<M>> alreadyBest = bestMoves.stream().filter(em -> moves.contains(em.getContent())).toList();
		return alreadyBest.isEmpty() ? bestMoves : alreadyBest;
	}
}
