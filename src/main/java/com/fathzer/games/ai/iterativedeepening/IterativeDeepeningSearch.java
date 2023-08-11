package com.fathzer.games.ai.iterativedeepening;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.fathzer.games.ai.AI;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningEngine.EventLogger;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningEngine.Mute;
import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation.Type;

class IterativeDeepeningSearch<M> {
	private final SearchParameters params;
	private final DeepeningPolicy deepeningPolicy;
	private final AI<M> ai;
	private long maxTime = Long.MAX_VALUE;
	private SearchParameters currentParams;
	private List<EvaluatedMove<M>> orderedMoves;
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
		final long start = System.currentTimeMillis();
		this.currentParams = new SearchParameters(deepeningPolicy.getStartDepth(), params.getSize(), params.getAccuracy());
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
		final List<EvaluatedMove<M>> ended = new ArrayList<>(bestMoves.getList().size());
		do {
			// Re-use best moves order to speedup next search
			final List<M> moves = getMovesToDeepen(bestMoves.getList(), ended);
			final int nextDepth = deepeningPolicy.getNextDepth(currentParams.getDepth());
			if (!moves.isEmpty() && nextDepth>0) {
				currentParams.setDepth(nextDepth);
				final SearchResult<M> deeper = ai.getBestMoves(moves, currentParams);
				deepeningPolicy.addSearchStage(nextDepth, deeper);
				logger.logSearch(currentParams.getDepth(), ai.getStatistics(), deeper);
				if (!ai.isInterrupted()) {
					bestMoves = deeper;
				} else {
					for (EvaluatedMove<M> ev:deeper.getList()) {
						bestMoves.update(ev.getContent(), ev.getEvaluation());
					}
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
	
	private List<M> getMovesToDeepen(List<EvaluatedMove<M>> evaluations, List<EvaluatedMove<M>> ended) {
		if (isEndOfGame(evaluations.get(0))) {
			// if best move is a win/loose, continuing analysis is useless
			logger.logEndDetected(currentParams.getDepth());
			return Collections.emptyList();
		}
		// Separate move that leads to loose (put in finished). These moves do not need to be deepened. Store others in toDeepen
		// We don't put 'finished' moves in ended directly to preserve the evaluation order 
		final List<M> toDeepen = new ArrayList<>(evaluations.size());
		final List<EvaluatedMove<M>> finished = new ArrayList<>();
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
	
	private boolean isEndOfGame(EvaluatedMove<M> mv) {
		return mv.getEvaluation().getType()!=Type.EVAL;
	}

	public int getMaxDepth() {
		return params.getDepth();
	}
}
