package com.fathzer.games.ai.recursive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.AI;
import com.fathzer.games.ai.Evaluator;
import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.recursive.AbstractRecursiveEngine.EventLogger;
import com.fathzer.games.ai.recursive.AbstractRecursiveEngine.Mute;
import com.fathzer.games.util.Evaluation;

class RecursiveSearch<M, B extends MoveGenerator<M>> {
	private final Evaluator<B> evaluator;
	private final SearchParameters params;
	private long maxTime = Long.MAX_VALUE;
	private final AI<M> ai;
	private List<Evaluation<M>> orderedMoves;
	private EventLogger<M> logger;
	private SearchParameters currentParams;
	
	RecursiveSearch(Evaluator<B> evaluator, AI<M> ai, SearchParameters params, long maxTimeMs) {
		this.evaluator = evaluator;
		this.params = params;
		this.maxTime = maxTimeMs;
		this.ai = ai;
		this.logger = new Mute<>();
	}
	
	public void interrupt() {
		this.ai.interrupt();
	}
	
	public void setEventLogger(EventLogger<M> logger) {
		this.logger = logger;
	}
	
	private List<Evaluation<M>> buildBestMoves() {
		final long start = System.currentTimeMillis();
		this.currentParams = new SearchParameters(getStartDepth(), params.getSize(), params.getAccuracy());
		SearchResult<M> bestMoves = ai.getBestMoves(currentParams);
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
		final List<Evaluation<M>> ended = new ArrayList<>(bestMoves.getList().size());
		do {
			// Re-use best moves order to speedup next search
			final List<M> moves = getMovesToDeepen(bestMoves.getList(), ended);
			currentParams.setDepth(getNextDepth(currentParams.getDepth()));
			if (!moves.isEmpty()) {
				final SearchResult<M> deeper = ai.getBestMoves(moves, currentParams);
				logger.logSearch(currentParams.getDepth(), ai.getStatistics(), deeper);
				if (!ai.isInterrupted()) {
					bestMoves = deeper;
				} else {
					for (Evaluation<M> ev:deeper.getList()) {
						bestMoves.update(ev.getContent(), ev.getValue());
					}
				}
			}
			if (ai.isInterrupted() || moves.isEmpty()) {
				break;
			}
		} while (currentParams.getDepth()<params.getDepth());
		timer.cancel();
		final List<Evaluation<M>> result = bestMoves.getCut();
		result.addAll(ended);
		return result;
	}

	protected int getStartDepth() {
		return 2;
	}

	protected int getNextDepth(int currentDepth) {
		return currentDepth+2;
	}

	public List<Evaluation<M>> getBestMoves() {
		if (orderedMoves==null) {
			orderedMoves = buildBestMoves();
		}
		return orderedMoves;
	}
	
	private List<M> getMovesToDeepen(List<Evaluation<M>> evaluations, List<Evaluation<M>> ended) {
		if (isEndOfGame(evaluations.get(0))) {
			// if best move is a win/loose, continuing analysis is useless
			logger.logEndDetected(currentParams.getDepth());
			return Collections.emptyList();
		}
		// Separate move that leads to loose (put in finished). These moves do not need to be deepened. Store others in toDeepen
		// We don't put 'finished' moves in ended directly to preserve the evaluation order 
		final List<M> toDeepen = new ArrayList<>(evaluations.size());
		final List<Evaluation<M>> finished = new ArrayList<>();
		evaluations.stream().forEach(e -> {
			if (isEndOfGame(e)) {
				finished.add(e);
			} else {
				toDeepen.add(e.getContent());
			}
		});
		ended.addAll(0, finished);
		return toDeepen;
	}
	
	private boolean isEndOfGame(Evaluation<M> mv) {
		return evaluator.isWinLooseScore(mv.getValue(), params.getDepth());
	}

	public int getMaxDepth() {
		return params.getDepth();
	}
}
