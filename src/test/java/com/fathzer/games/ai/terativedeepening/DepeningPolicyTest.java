package com.fathzer.games.ai.terativedeepening;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.DummyEvaluator;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.iterativedeepening.DeepeningPolicy;
import com.fathzer.games.ai.iterativedeepening.SearchHistory;
import com.fathzer.games.util.OrderedUtils;

class DepeningPolicyTest {
	@Test
	void testMovesToDeepen() {
		final DeepeningPolicy policy = new DeepeningPolicy(8);
		policy.setSize(3);
		policy.start();
		assertTrue(policy.isEnoughTimeToDeepen(0));
		int depth = policy.getStartDepth();
		final SearchHistory<String> history = new SearchHistory<>(policy.getSize(), policy.getAccuracy());
		final List<EvaluatedMove<String>> evaluations = new ArrayList<>();
		evaluations.add(new EvaluatedMove<>("a",Evaluation.score(0)));
		evaluations.add(new EvaluatedMove<>("b",Evaluation.score(-1)));
		evaluations.add(new EvaluatedMove<>("c",Evaluation.score(-2)));
		evaluations.add(new EvaluatedMove<>("d",Evaluation.score(-3)));
		fillHistory(history, evaluations, 1);
		List<String> toDeepen = policy.getMovesToDeepen(depth, evaluations, history);
		assertEquals(4, toDeepen.size());
		
		final Evaluator<String, Void> evaluator = new DummyEvaluator<String, Void>();
		evaluations.clear();
		evaluations.add(new EvaluatedMove<>("a",Evaluation.win(depth, evaluator.getWinScore(depth))));
		evaluations.add(new EvaluatedMove<>("b",Evaluation.score(-1)));
		evaluations.add(new EvaluatedMove<>("c",Evaluation.score(-2)));
		evaluations.add(new EvaluatedMove<>("d",Evaluation.loose(depth, -evaluator.getWinScore(depth-1))));
		fillHistory(history, evaluations, 2);
		toDeepen = policy.getMovesToDeepen(depth, evaluations, history);
		assertEquals(2, toDeepen.size());

		evaluations.clear();
		evaluations.add(new EvaluatedMove<>("b",Evaluation.win(depth+2, evaluator.getWinScore(depth+2))));
		evaluations.add(new EvaluatedMove<>("c",Evaluation.score(0)));
		fillHistory(history, evaluations, 3);
		toDeepen = policy.getMovesToDeepen(depth, evaluations, history);
		assertEquals(0, toDeepen.size());
	}

	private void fillHistory(SearchHistory<String> history, List<EvaluatedMove<String>> evaluations, int depth) {
		final LinkedList<EvaluatedMove<String>> added = new LinkedList<>(evaluations);
		if (history.isEmpty()) {
			history.add(added, depth);
		} else {
			final List<EvaluatedMove<String>> current = history.getList();
			if (added.size()!=current.size()) {
				final List<EvaluatedMove<String>> missing = current.stream().filter(em -> !added.stream().anyMatch(am-> em.getContent().equals(am.getContent()))).toList();
				missing.forEach(em -> OrderedUtils.insert(added, em));
			}
			history.add(added, depth);
		}
//		System.out.println("---------");
//		System.out.println(evaluations);
//		System.out.println(" ----->");
//		history.results().stream().forEach(System.out::println);
	}

	@Test
	void testMergeInterrupted() {
		final DeepeningPolicy policy = new DeepeningPolicy(4);
		policy.setSize(2);
		final List<EvaluatedMove<String>> partialList = new ArrayList<>(); 
		List<EvaluatedMove<String>> cut;
		SearchResult<String> sr;

		sr = new SearchResult<>(policy.getSize(), policy.getAccuracy());
		sr.add("x", Evaluation.score(50));
		sr.add("y", Evaluation.score(45));
		sr.add("z", Evaluation.score(44));
		sr.add("w", Evaluation.score(44));
		partialList.clear();
		// The easy case, scores of sr.getCut() are better than previously => merge (Replace scores)
		partialList.add(new EvaluatedMove<String>("y", Evaluation.score(52)));
		partialList.add(new EvaluatedMove<String>("x", Evaluation.score(50)));
		partialList.add(new EvaluatedMove<String>("w", Evaluation.score(49)));
		
		sr = merge(policy, sr, partialList).get();
		cut = sr.getCut();
		assertEquals(2, cut.size());
		assertEquals("y", cut.get(0).getContent());
		assertEquals(52, cut.get(0).getEvaluation().getScore());
		assertEquals("x", cut.get(1).getContent());
		assertEquals(50, cut.get(1).getEvaluation().getScore());
		
		// Difficult part: previous sr.getCut() moves are not in partial list => Nothing should change (we have no way to know if x has a better score than y or not)
		sr = new SearchResult<>(policy.getSize(), policy.getAccuracy());
		sr.add("x", Evaluation.score(50));
		sr.add("y", Evaluation.score(45));
		sr.add("z", Evaluation.score(44));
		sr.add("w", Evaluation.score(44));
		partialList.clear();
		partialList.add(new EvaluatedMove<String>("y", Evaluation.score(52)));
		
		assertTrue(merge(policy, sr, partialList).isEmpty());
		
		// Difficult part: some scores are lower than previous 'sr.getLow()'
		sr = new SearchResult<>(policy.getSize(), policy.getAccuracy());
		sr.add("x", Evaluation.score(50));
		sr.add("y", Evaluation.score(45));
		sr.add("z", Evaluation.score(44));
		sr.add("w", Evaluation.score(44));
		partialList.clear();
		partialList.add(new EvaluatedMove<String>("y", Evaluation.score(42)));
		partialList.add(new EvaluatedMove<String>("x", Evaluation.score(40)));
		partialList.add(new EvaluatedMove<String>("w", Evaluation.score(39)));
		
		sr = merge(policy, sr, partialList).get();
		partialList.clear();
		cut = sr.getCut();
		assertEquals(2, cut.size());
		assertEquals("y", cut.get(0).getContent());
		assertEquals(42, cut.get(0).getEvaluation().getScore());
		assertEquals("x", cut.get(1).getContent());
		assertEquals(40, cut.get(1).getEvaluation().getScore());
	}
	
	private <M> Optional<SearchResult<M>> merge(DeepeningPolicy policy, SearchResult<M> history, List<EvaluatedMove<M>> partialList) {
		final SearchResult<M> interrupted = new SearchResult<M>(policy.getSize(), policy.getAccuracy());
		final SearchHistory<M> theHistory = new SearchHistory<M>(policy.getSize(), policy.getAccuracy());
		theHistory.add(history.getList(), 1);
		partialList.forEach(ev -> interrupted.add(ev.getContent(), ev.getEvaluation()));
		return policy.mergeInterrupted(theHistory, interrupted, 2);
	}
}
