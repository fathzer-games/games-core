package com.fathzer.games.ai.iterativedeepening;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.fathzer.games.ai.AI;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningEngine.Mute;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningEngine.SearchEventLogger;
import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;

public class IterativeDeepeningSearch<M> {
	private final DeepeningPolicy deepeningPolicy;
	private final AI<M> ai;
	private List<EvaluatedMove<M>> orderedMoves;
	private List<SearchResult<M>> searchHistory;
	private SearchEventLogger<M> logger;
	
	IterativeDeepeningSearch(AI<M> ai, DeepeningPolicy deepeningPolicy) {
		this.ai = ai;
		this.logger = new Mute<>();
		this.deepeningPolicy = deepeningPolicy;
	}
	
	public void interrupt() {
		this.ai.interrupt();
	}
	
	public void setEventLogger(SearchEventLogger<M> logger) {
		this.logger = logger;
	}
	
	private List<EvaluatedMove<M>> buildBestMoves() {
		deepeningPolicy.start();
		this.searchHistory = new ArrayList<>();
		final SearchParameters currentParams = new SearchParameters(deepeningPolicy.getStartDepth(), deepeningPolicy.getSize(), deepeningPolicy.getAccuracy());
		SearchResult<M> bestMoves = ai.getBestMoves(currentParams);
		searchHistory.add(bestMoves);
		logger.logSearchAtDepth(currentParams.getDepth(), ai.getStatistics(), bestMoves);
		final long maxTime = deepeningPolicy.getMaxTime();
		final long remaining = maxTime-(deepeningPolicy.getSpent());
		if ((bestMoves.getList().size()==1 && !deepeningPolicy.isDeepenOnForced()) || remaining<=0) {
			return bestMoves.getCut();
		}
		final Timer timer = new Timer(true);
		if (maxTime!=Long.MAX_VALUE) {
			timer.schedule(new TimerTask(){
				@Override
				public void run() {
					ai.interrupt();
					logger.logTimeOut(currentParams.getDepth());
				}
			}, remaining);
		}
		final List<EvaluatedMove<M>> ended = new ArrayList<>(bestMoves.getList().size());
		while (currentParams.getDepth()<deepeningPolicy.getDepth()) {
			// Re-use best moves order to speedup next search
			final List<M> moves = deepeningPolicy.getMovesToDeepen(currentParams.getDepth(), bestMoves.getList(), ended);
			if (moves.isEmpty()) {
				logger.logEndedByPolicy(currentParams.getDepth());
			} else {
				final int previousDepth = currentParams.getDepth();
				currentParams.setDepth(deepeningPolicy.getNextDepth(currentParams.getDepth()));
				final SearchResult<M> deeper = ai.getBestMoves(moves, currentParams);
				searchHistory.add(deeper);
				logger.logSearchAtDepth(currentParams.getDepth(), ai.getStatistics(), deeper);
				if (!ai.isInterrupted()) {
					bestMoves = deeper;
				} else {
					deepeningPolicy.mergeInterrupted(bestMoves, previousDepth, deeper.getList(), currentParams.getDepth());
				}
			}
			if (ai.isInterrupted() || moves.isEmpty()) {
				break;
			}
		}
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
		return deepeningPolicy.getDepth();
	}
}
