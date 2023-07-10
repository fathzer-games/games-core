package com.fathzer.games;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** A class that manages the history of positions of a game to allow move to be undone.
 * <br>This class stores and retrieve positions in customizable backup containers.
 * It is optimized to minimize the number of backup containers instantiated and the number of backup performed.
 * <br>{@link #beforeMove()} method should be called to store the current position before performing a move.
 * <br>Then the {@link #undo()} method revert the last move. This method can be iterated until the start position is reached.
 * @param <T> The class of backup containers.
 */
public class UndoMoveManager<T> {
	private final Supplier<T> backupBuilder;
	private final Consumer<T> backup;
	private final Consumer<T> restore;
	private final List<T> backups;
	private int index;
	private boolean saved;
	
	/** Constructor.
	 * @param backupBuilder A supplier that can create a backup container for the game.
	 * @param backup A consumer that makes a game backup to its argument.
	 * @param restore A consumer that restores a game from its argument.
	 * <br>Please note, the backup/restore operations are not done by this class. It simply manage the containers used to perform these operations.
	 */
	public UndoMoveManager(Supplier<T> backupBuilder, Consumer<T> backup, Consumer<T> restore) {
		this.backupBuilder = backupBuilder;
		this.backup = backup;
		this.restore = restore;
		this.backups = new ArrayList<>();
		this.index = 0;
		this.saved = false;
	}
	
	/** This method should be called by the game to perform a backup before making a move.
	 * <br>All undone moves are forgotten after this method (you can't rewrite only one move in the middle of the game, there's no guarantee that remaining positions are possible).
	 */
	public void beforeMove() {
		if (!saved) {
			final T state;
			if (index>=backups.size()) {
				state = backupBuilder.get();
				backups.add(state);
			} else {
				state = backups.get(index);
			}
			backup.accept(state);
		}
		saved = false;
		index++;
	}

	/** Restores the backup of the game to achieve the undo move.
	 * @throws IllegalStateException if no backup is available for undoing a move.
	 */
	public void undo() {
		if (index==0) {
			throw new IllegalStateException("No move to undo");
		}
		index--;
		restore.accept(backups.get(index));
		saved = true;
	}
}
