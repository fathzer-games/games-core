package com.fathzer.games.util.exec;

import com.fathzer.games.MoveGenerator;

/** A forkable class can be partially cloned in order to be used in another thread that will changes the clone state but never try to access to
 * its state before the fork happened.
 * <br>Typically, a {@link MoveGenerator} has to be cloned when multiple threads are launched to explores the move tree.
 * The cloned instance can be <i>partial</i> because the threads that explore each move will never try to unmake moves done before the move generator was clone.
 * @param <T> The type of the forkable class.
 */
public interface Forkable<T> {
	/** Forks (partially clones) this instance.
	 * @return a partial clone of this instance. This clone should:<ul>
	 * <li>Be an instance of this class</li>
	 * <li>Have no side effects with this</li>
	 * </ul>
	 */
	T fork();
}
