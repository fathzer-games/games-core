package com.fathzer.games.ai.terativedeepening;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation;
import com.fathzer.games.ai.iterativedeepening.DeepeningPolicy;
import com.fathzer.games.ai.iterativedeepening.SearchHistory;

class DeepeningPolicyTest {
	@Test
	void test() {
		final DeepeningPolicy policy = new DeepeningPolicy(10);
		assertEquals(10, policy.getDepth());
		assertEquals(2, policy.getStartDepth());
		assertEquals(3, policy.getNextDepth(2));
		assertEquals(4, policy.getNextDepth(3));
		assertEquals(Long.MAX_VALUE, policy.getMaxTime());
		assertThrows(IllegalStateException.class, () -> policy.getSpent());
		policy.start();
		policy.getSpent(); //Should not fail
		
		// Reset the policy
		policy.setMaxTime(1);
		assertThrows(IllegalStateException.class, () -> policy.getSpent());
		policy.start();
		await().atMost(110, TimeUnit.MILLISECONDS).until(() -> policy.getSpent()>10);
		assertFalse(policy.isEnoughTimeToDeepen(4));
		
		policy.setMaxTime(1000);
		policy.start();
		await().atLeast(10, TimeUnit.MILLISECONDS);
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
