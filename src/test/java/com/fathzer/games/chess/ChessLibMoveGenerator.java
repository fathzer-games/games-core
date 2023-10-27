package com.fathzer.games.chess;

import java.util.Comparator;
import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.HashProvider;
import com.fathzer.games.Status;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;

public class ChessLibMoveGenerator implements MoveGenerator<Move>, HashProvider {
	private final Board board;
	private Comparator<Move> comparator;
	
	public ChessLibMoveGenerator(Board board) {
		this.board = board.clone();
	}
	
	@Override
	public boolean makeMove(Move move) {
		return board.doMove(move);
	}
	
	@Override
	public void unmakeMove() {
		board.undoMove();
	}
	
	@Override
	public List<Move> getMoves(boolean quiesce) {
		final List<Move> moves = quiesce ? board.pseudoLegalCaptures() : board.pseudoLegalMoves();
		if (comparator!=null) {
			moves.sort(comparator);
		}
		return moves;
	}
	
	@Override
	public long getHashKey() {
		return board.getZobristKey();
	}
	
	public Board getBoard() {
		return this.board; 
	}

	public void setMoveComparator(Comparator<Move> comparator) {
		this.comparator = comparator;
	}
	

	@Override
	public Status isRepetition() {
		return board.getHalfMoveCounter()>50 || board.isInsufficientMaterial() || board.isRepetition() ? Status.DRAW : Status.PLAYING;
	}

	@Override
	public Status onNoValidMove() {
		if (board.isKingAttacked()) {
			return board.getSideToMove()==Side.BLACK ? Status.WHITE_WON : Status.BLACK_WON;
		} else {
			return Status.DRAW;
		}
	}
}
