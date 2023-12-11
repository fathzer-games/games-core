package com.fathzer.games.ai;

import java.util.function.Function;

import com.fathzer.games.ai.experimental.Negamax3;
import com.fathzer.games.chess.ChessLibMoveGenerator;
import com.fathzer.games.util.exec.ExecutionContext;
import com.github.bhlangonijr.chesslib.move.Move;

public enum AiType {
	Negamax(Negamax::new), Negamax3(Negamax3::new), AlphaBeta(AlphaBeta::new), Minimax(Minimax::new);
	
	private final Function<ExecutionContext<SearchContext<Move, ChessLibMoveGenerator>>, AbstractAI<Move, ChessLibMoveGenerator>> aiBuilder;

	private AiType(Function<ExecutionContext<SearchContext<Move, ChessLibMoveGenerator>>, AbstractAI<Move, ChessLibMoveGenerator>> aiBuilder) {
		this.aiBuilder = aiBuilder;
	}

	public Function<ExecutionContext<SearchContext<Move, ChessLibMoveGenerator>>, AbstractAI<Move, ChessLibMoveGenerator>> getAiBuilder() {
		return aiBuilder;
	}
}