package com.fathzer.games.ai.iterativedeepening;

import java.util.ArrayList;
import java.util.List;

import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.moveselector.MoveSelector;

public class SearchHistory<M> {
	private final int size;
	private final int accuracy;
	private final List<List<EvaluatedMove<M>>> results;
	private final List<Integer> depths;
	
	public SearchHistory(int size, int accuracy) {
		this.size = size;
		this.accuracy = accuracy;
		this.results = new ArrayList<>();
		this.depths = new ArrayList<>();
	}
	
	public void add(List<EvaluatedMove<M>> result, int depth) {
		if (!isEmpty() && depth<depths.get(depths.size()-1)) {
			throw new IllegalArgumentException();
		}
		results.add(result);
		depths.add(depth);
	}
	
	public int getDepth(int index) {
		return depths.get(index);
	}

	public List<EvaluatedMove<M>> getList(int index) {
		return results.get(index);
	}

	public List<EvaluatedMove<M>> getBestMoves(int index) {
		return SearchResult.getBestMoves(results.get(index), size, accuracy);
	}

	public List<EvaluatedMove<M>> getList() {
		return results.isEmpty() ? null : results.get(results.size()-1);
	}
	public List<EvaluatedMove<M>> getBestMoves() {
		return results.isEmpty() ? null : SearchResult.getBestMoves(results.get(results.size()-1), size, accuracy);
	}
	
	public EvaluatedMove<M> getBestMove(MoveSelector<M, SearchHistory<M>> selector) {
		return selector.select(this, getBestMoves()).get(0);
	}

	public EvaluatedMove<M> getBestMove() {
		return getBestMoves().get(0);
	}
	
	public M getBest() {
		return isEmpty() ? null : getBestMoves().get(0).getContent();
	}
	
	public int length() {
		return results.size();
	}
	
	public boolean isEmpty() {
		return results.isEmpty();
	}
	
	public int getDepth() {
		return results.isEmpty() ? 0 : depths.get(results.size()-1);
	}
}