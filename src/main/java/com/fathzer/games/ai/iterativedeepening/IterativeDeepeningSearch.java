package com.fathzer.games.ai.iterativedeepening;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import com.fathzer.games.ai.DepthFirstAI;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningEngine.Mute;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningEngine.SearchEventLogger;
import com.fathzer.games.util.SortedUtils;
import com.fathzer.games.ai.DepthFirstSearchParameters;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;

public class IterativeDeepeningSearch<M> {
	private final DeepeningPolicy deepeningPolicy;
	private final DepthFirstAI<M, DepthFirstSearchParameters> ai;
	private SearchHistory<M> searchHistory;
	private List<M> searchedMoves;
	private SearchEventLogger<M> logger;
	private int depth;
	
	IterativeDeepeningSearch(DepthFirstAI<M, DepthFirstSearchParameters> ai, DeepeningPolicy deepeningPolicy) {
		this.ai = ai;
		this.logger = new Mute<>();
		this.deepeningPolicy = deepeningPolicy;
	}
	
	public void setSearchedMoves(List<M> searchedMoves) {
		this.searchedMoves = searchedMoves;
	}

	public void interrupt() {
		this.ai.interrupt();
	}
	
	public void setEventLogger(SearchEventLogger<M> logger) {
		this.logger = logger;
	}
	
	private SearchHistory<M> buildBestMoves() {
		deepeningPolicy.start();
		this.searchHistory = new SearchHistory<>(deepeningPolicy);
		final DepthFirstSearchParameters currentParams = new DepthFirstSearchParameters(deepeningPolicy.getStartDepth(), deepeningPolicy.getSize(), deepeningPolicy.getAccuracy());
		SearchResult<M> bestMoves = searchedMoves==null ? ai.getBestMoves(currentParams) : ai.getBestMoves(searchedMoves, currentParams);
		searchHistory.add(bestMoves.getList(), deepeningPolicy.getStartDepth());
		logger.logSearchAtDepth(currentParams.getDepth(), ai.getStatistics(), bestMoves);
		final long maxTime = deepeningPolicy.getMaxTime();
		final long remaining = maxTime-(deepeningPolicy.getSpent());
		if (!deepeningPolicy.isEnoughTimeToDeepen(currentParams.getDepth())) {
			// If there is not enough time to deepen => do not deepen the search
			return searchHistory;
		}
		final Timer timer = new Timer(true);
		if (maxTime!=Long.MAX_VALUE) {
			// Schedule a task to stop the deepening when maximum thinking time has run out
			timer.schedule(new TimerTask(){
				@Override
				public void run() {
					ai.interrupt();
					logger.logTimeOut(currentParams.getDepth());
				}
			}, remaining);
		}
		List<EvaluatedMove<M>> evaluatedMoves = bestMoves.getList();
		final List<EvaluatedMove<M>> ended = new ArrayList<>(evaluatedMoves.size());
		while (currentParams.getDepth()<deepeningPolicy.getDepth()) {
			final List<M> moves = deepeningPolicy.isEnoughTimeToDeepen(depth) ? deepeningPolicy.getMovesToDeepen(currentParams, searchHistory, evaluatedMoves) : Collections.emptyList();
			if (moves.isEmpty()) {
				logger.logEndedByPolicy(currentParams.getDepth());
			} else {
				if (moves.size()!=evaluatedMoves.size()) {
					// Some moves does not need deepening => Add them to ended
					evaluatedMoves.stream().filter(em-> !moves.contains(em.getMove())).forEach(ended::add);
				}
				currentParams.setDepth(deepeningPolicy.getNextDepth(currentParams.getDepth()));
				final SearchResult<M> deeper = ai.getBestMoves(moves, currentParams);
				evaluatedMoves = deeper.getList();
				depth = currentParams.getDepth();
				logger.logSearchAtDepth(depth, ai.getStatistics(), deeper);
				final Optional<SearchResult<M>> stepResult = ai.isInterrupted() ? deepeningPolicy.mergeInterrupted(searchHistory, deeper, depth) : Optional.of(deeper);
				stepResult.ifPresent(r -> searchHistory.add(complete(r, ended), depth));
			}
			if (ai.isInterrupted() || moves.isEmpty()) {
				break;
			}
		}
		timer.cancel();
		return searchHistory;
	}
	
	private static <M> List<EvaluatedMove<M>> complete(SearchResult<M> result, List<EvaluatedMove<M>> ended) {
		if (ended.isEmpty()) {
			return result.getList();
		}
		final var moves = new ArrayList<>(result.getList());
		ended.stream().forEach(em -> SortedUtils.insert(moves, em));
		return moves;
	}
	
	public SearchHistory<M> getSearchHistory() {
		if (searchHistory==null) {
			searchHistory = buildBestMoves();
		}
		return this.searchHistory;
	}
}
