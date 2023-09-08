package com.fathzer.games.ai;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.ai.experimental.Negamax3;
import com.fathzer.games.chess.BasicMoveComparator;
import com.fathzer.games.chess.ChessLibMoveGenerator;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

enum AiType {
	Negamax(Negamax::new), Negamax3(Negamax3::new), AlphaBeta(AlphaBeta::new), Minimax(Minimax::new);
	
	private final BiFunction<ExecutionContext<Move, ChessLibMoveGenerator>, Evaluator<ChessLibMoveGenerator>, AbstractAI<Move, ChessLibMoveGenerator>> aiBuilder;

	private AiType(BiFunction<ExecutionContext<Move, ChessLibMoveGenerator>, Evaluator<ChessLibMoveGenerator>, AbstractAI<Move, ChessLibMoveGenerator>> aiBuilder) {
		this.aiBuilder = aiBuilder;
	}

	public BiFunction<ExecutionContext<Move, ChessLibMoveGenerator>, Evaluator<ChessLibMoveGenerator>, AbstractAI<Move, ChessLibMoveGenerator>> getAiBuilder() {
		return aiBuilder;
	}
	
	public Supplier<ChessLibMoveGenerator> getMVGsupplier(Board board) {
		return () -> getMoveGenerator(board);
	}
	
	private ChessLibMoveGenerator getMoveGenerator(Board board) {
		ChessLibMoveGenerator mg = new ChessLibMoveGenerator(board);
		if (Minimax!=this) {
			mg.setMoveComparator(new BasicMoveComparator(mg));
		}
		return mg;
	}
}