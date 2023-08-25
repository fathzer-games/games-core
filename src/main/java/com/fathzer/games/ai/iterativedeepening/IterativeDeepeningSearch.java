package com.fathzer.games.ai.iterativedeepening;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.fathzer.games.ai.AI;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningEngine.EventLogger;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningEngine.Mute;
import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;

public class IterativeDeepeningSearch<M> {
	private final SearchParameters params;
	private final DeepeningPolicy deepeningPolicy;
	private final AI<M> ai;
	private long maxTime = Long.MAX_VALUE;
	private SearchParameters currentParams;
	private List<EvaluatedMove<M>> orderedMoves;
	private List<SearchResult<M>> searchHistory;
	private EventLogger<M> logger;
	
	IterativeDeepeningSearch(AI<M> ai, SearchParameters params, DeepeningPolicy deepeningPolicy, long maxTimeMs) {
		this.params = params;
		this.maxTime = maxTimeMs;
		this.ai = ai;
		this.logger = new Mute<>();
		this.deepeningPolicy = deepeningPolicy;
	}
	
	public void interrupt() {
		this.ai.interrupt();
	}
	
	public void setEventLogger(EventLogger<M> logger) {
		this.logger = logger;
	}
	
	private List<EvaluatedMove<M>> buildBestMoves() {
		this.searchHistory = new ArrayList<>();
		final long start = System.currentTimeMillis();
		this.currentParams = new SearchParameters(deepeningPolicy.getStartDepth(), params.getSize(), params.getAccuracy());
		SearchResult<M> bestMoves = ai.getBestMoves(currentParams);
		searchHistory.add(bestMoves);
		logger.logSearch(currentParams.getDepth(), ai.getStatistics(), bestMoves);
		final Timer timer = new Timer(true);
		if (maxTime!=Long.MAX_VALUE) {
			final long remaining = maxTime-(System.currentTimeMillis()-start);
			if (remaining>0) {
				timer.schedule(new TimerTask(){
					@Override
					public void run() {
						ai.interrupt();
						logger.logTimeOut(currentParams.getDepth());
					}
				}, remaining);
			} else {
				return bestMoves.getCut();
			}
		}
		final List<EvaluatedMove<M>> ended = new ArrayList<>(bestMoves.getList().size());
		do {
			// Re-use best moves order to speedup next search
			final List<M> moves = deepeningPolicy.getMovesToDeepen(currentParams.getDepth(), bestMoves.getList(), ended);
			if (moves.isEmpty()) {
				logger.logEndedByPolicy(currentParams.getDepth());
			} else {
				final int previousDepth = currentParams.getDepth();
				currentParams.setDepth(deepeningPolicy.getNextDepth(currentParams.getDepth()));
				final SearchResult<M> deeper = ai.getBestMoves(moves, currentParams);
				searchHistory.add(deeper);
				logger.logSearch(currentParams.getDepth(), ai.getStatistics(), deeper);
				if (!ai.isInterrupted()) {
					bestMoves = deeper;
				} else {
					deepeningPolicy.mergeInterrupted(bestMoves, previousDepth, deeper.getList(), currentParams.getDepth());
				}
			}
			if (ai.isInterrupted() || moves.isEmpty()) {
				break;
			}
		} while (currentParams.getDepth()<params.getDepth());
		timer.cancel();
		// Warning, ended should be added carefully or loosing position will be returned in the best positions
		// For example, if we simple add the ended moves to bestMove.getCut().
		for (EvaluatedMove<M> em:ended) {
			bestMoves.add(em.getContent(), em.getEvaluation());
		}
		return bestMoves.getCut();
	}

	public List<EvaluatedMove<M>> getBestMoves() {
		if (orderedMoves==null) {
			orderedMoves = buildBestMoves();
		}
		return orderedMoves;
	}
	
	public List<SearchResult<M>> getSearchHistory() {
		if (orderedMoves==null) {
			orderedMoves = buildBestMoves();
		}
		return this.searchHistory;
	}

	public int getMaxDepth() {
		return params.getDepth();
	}
}
