package com.fathzer.games.movelibrary;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.evaluation.EvaluatedMove;

/** An abstract move library based on searching records in a local or remote database
 * @param <R> The type of the database records. Those records contain the information about a move
 * @param <M> The type of moves
 * @param <B> The type of game board
 */
public abstract class AbstractMoveLibrary<R, M, B extends MoveGenerator<M>> implements MoveLibrary<M, B> {
	@SuppressWarnings("java:S2245") //Ignores Sonar security hot spot, here Random is safe 
	private static final Random RND = new Random();

	/** A function that always select the first element of a list.
	 * @param <T> The list element type
	 * @return A function that always select the first element of the list
	 */
	public static <T> Function<List<T>, T> firstMoveSelector() {
		return l -> l.get(0);
	}

	/** A function that randomly selects an element of a list.
	 * @param <T> The list element type
	 * @return A function that randomly selects an element of a list.
	 */
	public static <T> Function<List<T>,T> randomMoveSelector() {
		return l -> l.get(RND.nextInt(l.size()));
	}
	
	private Function<List<R>, R> moveSelector = randomMoveSelector();
	private MoveLibrary<M, MoveGenerator<M>> other;
	
	/** A function that randomly selects an element of a list with a probability proportional to its relative weight.
	 * @return A function.
	 * @see #getWeight(Object)
	 */
	public Function<List<R>, R> weightedMoveSelector() {
		return values -> {
			long count = values.stream().mapToLong(AbstractMoveLibrary.this::getWeight).sum();
			long value = RND.nextLong(count);
			count = 0;
			int index = values.size()-1;
			for (int i=0;i<values.size();i++) {
				count += getWeight(values.get(i));
				if (value<count) {
					index = i;
					break;
				}
			}
			return values.get(index);
		};
	}
	
	/** Gets the records related to a position.
	 * @param board The position
	 * @return A list of records, each of them describes a move. Or an empty optional if the position is not in the base
	 */
	protected abstract Optional<List<R>> getRecord(B board);

	/** Converts a database record that describes an evaluated move to a move instance.
	 * @param board The position
	 * @param move The move database record
	 * @return An evaluated move instance
	 */
	protected abstract EvaluatedMove<M> toEvaluatedMove(B board, R move);
	
	/** Sets the move selector (the function that selects a move in the list of move records returned by {@link #getRecord(MoveGenerator)}
	 * <br>By default, the move is randomly chosen in the list.
	 * @param moveSelector A move selector
	 */
	public void setMoveSelector(Function<List<R>, R> moveSelector) {
		this.moveSelector = moveSelector;
	}
	
	/** Sets another library to search when no moves are available in this one.
	 * @param next Another library or null to remove the current 'next' library.
	 */
	public void setNext(MoveLibrary<M, MoveGenerator<M>> next) {
		this.other = next;
	}

	/** {@inheritDoc}
	 * <br>If this library can't find a move for this position, and a 'next' library was linked with this using {@link #setNext(MoveLibrary)},
	 * its apply method's result is returned.
	 */
	@Override
	public Optional<EvaluatedMove<M>> apply(B board) {
		final Optional<List<R>> theRecordO = getRecord(board);
		if (theRecordO.isEmpty()) {
			return other==null ? Optional.empty() : other.apply(board);
		}
		final List<R> moves = theRecordO.get();
		final R selectedRecord = moveSelector.apply(moves);
		return Optional.of(toEvaluatedMove(board, selectedRecord));
	}
	
	/** Gets the weight of a move.
	 * @param move A database record that represents the move.
	 * @return a long, typically, the number of times best players played this move.
	 * <br>The default implementation returns 1, making all the moves equiprobable. 
	 */
	protected long getWeight(R move) {
		return 1;
	}

	@Override
	public void newGame() {
		if (other!=null) {
			other.newGame();
		}
	}
}
