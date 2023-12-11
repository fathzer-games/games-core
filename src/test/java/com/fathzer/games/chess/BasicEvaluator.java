package com.fathzer.games.chess;

import static com.fathzer.games.Color.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.fathzer.games.Color;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;

public class BasicEvaluator implements Evaluator<Move, ChessLibMoveGenerator> {
	public static final Map<PieceType, Integer> PIECE_VALUE;
	private Color viewPoint;
	
	static {
		Map<PieceType, Integer> map = new EnumMap<>(PieceType.class);
		map.put(PieceType.QUEEN, 9);
		map.put(PieceType.ROOK, 5);
		map.put(PieceType.BISHOP, 3);
		map.put(PieceType.KNIGHT, 3);
		map.put(PieceType.PAWN, 1);
		map.put(PieceType.KING, 1000);
		PIECE_VALUE = Collections.unmodifiableMap(map);
	}
	
	@Override
	public void setViewPoint(Color viewPoint) {
		this.viewPoint = viewPoint;
	}

	@Override
	public int evaluate(ChessLibMoveGenerator mg) {
		int points = 100*getPoints(mg.getBoard());
		if (BLACK==viewPoint || (viewPoint==null && Side.BLACK==mg.getBoard().getSideToMove())) {
			points = -points;
		}
		return points;
	}

	public int getPoints(Board board) {
		int points = 0;
		for (Piece p : board.boardToArray()) {
			if (p!=Piece.NONE) {
				int inc = PIECE_VALUE.get(p.getPieceType());
				if (p.getPieceSide()==Side.WHITE) {
					points += inc;
				} else {
					points -= inc;
				}
			}
		}
		return points;
	}

	@Override
	public Evaluator<Move, ChessLibMoveGenerator> fork() {
		final BasicEvaluator evaluator = new BasicEvaluator();
		evaluator.setViewPoint(viewPoint);
		return evaluator;
	}
}
