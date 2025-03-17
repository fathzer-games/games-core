package com.fathzer.games.testutils;

import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation;

public interface Utils {
    public static <M> EvaluatedMove<M> eval(M move, int score) {
        return new EvaluatedMove<>(move, Evaluation.score(score));
    }

}
