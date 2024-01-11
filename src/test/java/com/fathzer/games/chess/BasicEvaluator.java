package com.fathzer.games.chess;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.fathzer.games.ai.evaluation.AbstractEvaluator;
import com.fathzer.games.ai.evaluation.StaticEvaluator;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;

public class BasicEvaluator extends AbstractEvaluator<Move, ChessLibMoveGenerator> implements StaticEvaluator<Move, ChessLibMoveGenerator>{
	public static final Map<PieceType, Integer> PIECE_VALUE;
	
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
	protected int evaluateAsWhite(ChessLibMoveGenerator board) {
		return 100*getPoints(board.getBoard());
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
}
