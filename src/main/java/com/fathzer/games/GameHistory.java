package com.fathzer.games;

import java.util.LinkedList;
import java.util.List;

import com.fathzer.games.MoveGenerator.MoveConfidence;

/** A game history with its start board, the list of its moves and its termination cause.
 * @param <M> The type of the moves
 * @param <B> The type of the {@link MoveGenerator} used to represent the board
 */
public class GameHistory<M,B extends MoveGenerator<M>> {
	/** The termination cause of a game.
	 */
	public enum TerminationCause {
		/** A player resigns */
		ABANDONED,
		/** Result determined by third-party adjudication */
		ADJUDICATION,
		/** A player died (hope he was a computer!) */
		DEATH,
		/** The game was ended because of an emergency (fire, etc...) */
		EMERGENCY,
		/** The game ended simply because a player won */
		NORMAL,
		/** A player was disqualified */
		RULES_INFRACTION,
		/** A player ran out of time */
		TIME_FORFEIT,
		/** The game is not terminated */
		UNTERMINATED;
	}
	
	private final B startBoard;
	private final B board;
	private final List<M> moves;
	private Status extraStatus;
	private TerminationCause terminationCause;

	@SuppressWarnings("unchecked")
	public GameHistory(B board) {
		this.startBoard = (B) board.fork();
		this.board = (B) board.fork();
		this.moves = new LinkedList<>();
		this.terminationCause = TerminationCause.UNTERMINATED;
	}

	/** Adds a move to this history.
	 * @param move The move to add.
	 * @return true if the move is legal. False if it is not. In such a case the move is also added to the list of moves.
	 * <br>Please that if the move is illegal, the player is not automatically disqualified. The developer is free to implement
	 * the punishment he wants.
	 * <br>If the move is legal and changes the {@link Status} of the game to anything except {@link Status#PLAYING}, the termination
	 * cause is set to NORMAL 
	 * @throws IllegalStateException if game is already ended.
	 */
	public boolean add(M move) {
		if (Status.PLAYING!=getStatus()) {
			throw new IllegalStateException();
		}
		this.moves.add(move);
		final boolean result = board.makeMove(move, MoveConfidence.UNSAFE);
		if (result && Status.PLAYING!=getStatus()) {
			this.terminationCause = TerminationCause.NORMAL;
		}
		return result;
	}

	public B getStartBoard() {
		return startBoard;
	}
	
	public B getBoard() {
		return board;
	}

	public List<M> getMoves() {
		return moves;
	}
	
	/** Declares an early termination for the game.
	 * <br>For instance, a player resigns or players agree to a draw.
	 * <br>Any subsequent call to this method or {@link #add(Move)} will result in an IllegalStateException  
	 * @param status The end status
	 * @param terminationCause The termination cause
	 * @throws IllegalStateException if game is already ended.
	 * @throws IllegalArgumentException if termination is null or if status is {@link Status#PLAYING} or null.
	 */
	public void earlyEnd(Status status, TerminationCause terminationCause) {
		if (Status.PLAYING==status || status==null || terminationCause==null || terminationCause==TerminationCause.UNTERMINATED) {
			throw new IllegalArgumentException();
		}
		if (Status.PLAYING!=getStatus()) {
			throw new IllegalStateException("Status is "+getStatus());
		}
		this.extraStatus = status;
		this.terminationCause = terminationCause;
	}
	
	public Status getStatus() {
		if (extraStatus!=null) {
			return extraStatus;
		}
		return getBoardStatus(board);
	}
	
	public TerminationCause getTerminationCause() {
		return this.terminationCause;
	}
	
	protected Status getBoardStatus(B board) {
		Status status = board.getContextualStatus();
		if (status==Status.PLAYING && board.getLegalMoves().isEmpty()) {
			status = board.getEndGameStatus();
		}
		return status;
	}
}