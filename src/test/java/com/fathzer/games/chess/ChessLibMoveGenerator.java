package com.fathzer.games.chess;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.HashProvider;
import com.fathzer.games.Status;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;

public class ChessLibMoveGenerator implements MoveGenerator<Move>, HashProvider {
	private final Board board;
	private final Comparator<Move> comparator;
	private Function<Board, Comparator<Move>> moveComparatorBuilder;
	private boolean noQuiesce;
	
	@Override
	public ChessLibMoveGenerator fork() {
		final ChessLibMoveGenerator result = new ChessLibMoveGenerator(board.clone(), moveComparatorBuilder);
		if (noQuiesce) {
			result.noQuiesce = true;
		}
		return result;
	}
	
	public ChessLibMoveGenerator(Board board, Function<Board,Comparator<Move>> moveComparatorBuilder) {
		this.board = board;
		this.comparator = moveComparatorBuilder.apply(board);
		this.moveComparatorBuilder = moveComparatorBuilder;
	}
	
	public void setNoQuiesce(boolean noQuiesce) {
		this.noQuiesce = noQuiesce;
	}
	
	@Override
	public boolean isWhiteToMove() {
		return board.getSideToMove()==Side.WHITE;
	}

	@Override
	public boolean makeMove(Move move, MoveConfidence confidence) {
		try {
			return board.doMove(move, MoveConfidence.UNSAFE==confidence);
		} catch (RuntimeException e) {
			// Can throw an exception if no piece is at move from cell
			return false;
		}
	}
	
	@Override
	public void unmakeMove() {
		board.undoMove();
	}
	
	@Override
	public List<Move> getMoves(boolean quiesce) {
		final List<Move> moves;
		if (quiesce) {
			moves = noQuiesce ? Collections.emptyList() : board.pseudoLegalCaptures();
		} else {
			moves = board.pseudoLegalMoves();
		}
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

	@Override
	public Status getContextualStatus() {
		return board.getHalfMoveCounter()>50 || board.isInsufficientMaterial() || board.isRepetition() ? Status.DRAW : Status.PLAYING;
	}

	@Override
	public Status getEndGameStatus() {
		if (board.isKingAttacked()) {
			return board.getSideToMove()==Side.BLACK ? Status.WHITE_WON : Status.BLACK_WON;
		} else {
			return Status.DRAW;
		}
	}
}
