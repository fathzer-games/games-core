package com.fathzer.games.ai.terativedeepening;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation;
import com.fathzer.games.ai.iterativedeepening.DeepeningPolicy;

public class DepeningPolicyTest {

	@Test
	void testMergeInterrupted() {
		final DeepeningPolicy policy = new DeepeningPolicy(4);
		final List<EvaluatedMove<String>> partialList = new ArrayList<>(); 
		List<EvaluatedMove<String>> cut;
		SearchResult<String> sr;

		sr = new SearchResult<>(2, 0);
		sr.add("x", Evaluation.score(50));
		sr.add("y", Evaluation.score(45));
		sr.add("z", Evaluation.score(44));
		sr.add("w", Evaluation.score(44));
		partialList.clear();
		// The easy case, scores of sr.getCut() are better than previously => merge (Replace scores)
		partialList.add(new EvaluatedMove<String>("y", Evaluation.score(52)));
		partialList.add(new EvaluatedMove<String>("x", Evaluation.score(50)));
		partialList.add(new EvaluatedMove<String>("w", Evaluation.score(49)));
		
		policy.mergeInterrupted(sr, 2, partialList, 4);
		cut = sr.getCut();
		assertEquals(2, cut.size());
		assertEquals("y", cut.get(0).getContent());
		assertEquals(52, cut.get(0).getEvaluation().getScore());
		assertEquals("x", cut.get(1).getContent());
		assertEquals(50, cut.get(1).getEvaluation().getScore());
		
		// Difficult part: previous sr.getCut() moves are not in partial list => Nothing should change (we have no way to know if x has a better score than y or not)
		sr = new SearchResult<>(2, 0);
		sr.add("x", Evaluation.score(50));
		sr.add("y", Evaluation.score(45));
		sr.add("z", Evaluation.score(44));
		sr.add("w", Evaluation.score(44));
		partialList.clear();
		partialList.add(new EvaluatedMove<String>("y", Evaluation.score(52)));
		
		policy.mergeInterrupted(sr, 2, partialList, 4);
		cut = sr.getCut();
		assertEquals("x", cut.get(0).getContent());
		assertEquals("y", cut.get(1).getContent());
		assertEquals(45, cut.get(1).getEvaluation().getScore());
		
		// Difficult part: some scores are lower than previous 'sr.getLow()'
		sr = new SearchResult<>(2, 0);
		sr.add("x", Evaluation.score(50));
		sr.add("y", Evaluation.score(45));
		sr.add("z", Evaluation.score(44));
		sr.add("w", Evaluation.score(44));
		partialList.clear();
		partialList.add(new EvaluatedMove<String>("y", Evaluation.score(42)));
		partialList.add(new EvaluatedMove<String>("x", Evaluation.score(40)));
		partialList.add(new EvaluatedMove<String>("w", Evaluation.score(39)));
		
		policy.mergeInterrupted(sr, 2, partialList, 4);
		partialList.clear();
		cut = sr.getCut();
		assertEquals(2, cut.size());
		assertEquals("y", cut.get(0).getContent());
		assertEquals(42, cut.get(0).getEvaluation().getScore());
		assertEquals("x", cut.get(1).getContent());
		assertEquals(40, cut.get(1).getEvaluation().getScore());
	}
}
