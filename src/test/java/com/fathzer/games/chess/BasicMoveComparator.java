package com.fathzer.games.chess;

import static com.fathzer.games.chess.BasicEvaluator.PIECE_VALUE;

import com.fathzer.games.util.SelectiveComparator;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.move.Move;

/** A move comparator that considers a catch is better than other moves and taking a high value piece with a small value piece is better than the opposite.
 */
public class BasicMoveComparator implements SelectiveComparator<Move> {
	private Board board;
	
	public BasicMoveComparator(Board board) {
		this.board = board;
	}

	@Override
	public int compare(Move m1, Move m2) {
		// Important sort from higher to lower scores
		return getValue(m2) - getValue(m1);
	}
	
	@Override
	public boolean test(Move m) {
		return getValue(m)!=0;
	}


	public int getValue(Move m) {
		final PieceType promotion = m.getPromotion().getPieceType();
		int value = promotion==null ? 0 : (PIECE_VALUE.get(promotion)-1)*16;
		final PieceType caught = board.getPiece(m.getTo()).getPieceType();
		if (caught==null) {
			return value;
		} else {
			value += PIECE_VALUE.get(caught)*16;
			final PieceType catching = board.getPiece(m.getFrom()).getPieceType();
			return value - (catching==PieceType.KING ? 10 : PIECE_VALUE.get(catching));
		}
	}
}
