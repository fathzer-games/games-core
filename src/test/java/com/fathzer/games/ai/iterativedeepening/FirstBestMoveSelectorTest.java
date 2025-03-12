package com.fathzer.games.ai.iterativedeepening;

import static org.junit.jupiter.api.Assertions.*;

import static com.fathzer.games.testutils.Utils.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.evaluation.EvaluatedMove;


class FirstBestMoveSelectorTest {

    @Test
    void test() {
        SearchParameters params = new SearchParameters(3, 1);
        List<EvaluatedMove<String>> aBest = Arrays.asList(eval("A", 6), eval("B", 4), eval("C", 2));
        List<EvaluatedMove<String>> baBest = Arrays.asList(eval("B", 5), eval("A", 4), eval("C", 3));
        List<EvaluatedMove<String>> cBest = Arrays.asList(eval("C", 6), eval("A", 4), eval("B", 2));
        final FirstBestMoveSelector<String> selector = new FirstBestMoveSelector<>();

        SearchHistory<String> history;
        
        history = new SearchHistory<>(params);
        history.add(baBest, 1);
        history.add(cBest, 2);
		assertEquals("C", selector.get(history, cBest).get().getMove());
		
		history = new SearchHistory<String>(params);
		history.add(aBest, 1);
        history.add(baBest, 2);
        EvaluatedMove<String> selected = history.getBestMove(selector);
        assertEquals("A", selected.getMove());
    }

}
