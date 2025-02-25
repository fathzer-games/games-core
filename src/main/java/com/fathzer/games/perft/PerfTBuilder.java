package com.fathzer.games.perft;

import static com.fathzer.games.MoveGenerator.MoveConfidence.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;

/** A <a href="https://www.chessprogramming.org/Perft">Perft</a> builder.
 * @see PerfT
 */
public class PerfTBuilder<M> {
	private boolean playLeaves = true;
	private ExecutorService exec = null;
	private MoveConfidence moveType = PSEUDO_LEGAL;
	
	/** Sets this PerfT to play the moves corresponding to tree leaves or not.
	 * <br>The default setting is to play the leave moves.
	 * @param playLeaves true to play the leave moves false to not play them.
	 * <br>If <i>playLeaves</i> is false, the tested move generator is requested for legal moves to prevent erroneous results.
	 * Indeed, every non legal leave move returned by the pseudo-legal move generator would not be tested and would be counted as a legal move.
	 */
	public void setPlayLeaves(boolean playLeaves) {
		this.playLeaves = playLeaves;
		if (!playLeaves) {
			moveType = LEGAL;
		}
	}
	
	/** Sets this PerfT to get legal or <a href="https://www.chessprogramming.org/Pseudo-Legal_Move">pseudo legal</a> moves from the move generator.
	 * <br>By default PerfT plays pseudo legal moves.
	 * @param legal true to use legal moves, false to use pseudo-legal moves.
	 * <br>When <i>legal</i> is false, <i>playLeaves</i> is automatically set to true (because not doing this would result in wrong leaves count)
	 */
	public void setLegalMoves(boolean legal) {
		moveType = legal ? LEGAL : PSEUDO_LEGAL;
		if (!legal) {
			playLeaves = true;
		}
	}
	
	/** Sets the executor service to use for running the PerfT test.
	 * <br>By default, no executor service is used and the perfT is single threaded and executed on the thread that invokes its {@link PerfT#get} method.
	 * @param exec the executor service to use, or null to run it on the thread calling {@link PerfT#get()} method.
	 * <br>The best performance is delivered by a {@link ForkJoinPool}, nevertheless, using a basic executor service is possible and faster than the default.
	 * Of course if the computer has a single core, using the default option is the best.
	 * <br><b>Warning</b>Please note that the executor service is not closed by the PerfT instance. It is the responsibility of the caller to shutdown it.
	 */
	public void setExecutor(ExecutorService exec) {
        this.exec = exec;
    }
	
	/** Builds a PerfT.
	 * @param generator the move generator initialized to the test's start position.
	 * @param depth the depth to reach.
	 * @return a PerfT instance.
	 * @throws IllegalArgumentException if the generator is null or depth &lt;= 0.
	 */
	public PerfT<M> build(final MoveGenerator<M> generator, final int depth) {
		if (exec == null) {
			return new PerfT<>(generator, depth, playLeaves, moveType);
		} else if (exec instanceof ForkJoinPool fjp) {
			return new ForkJoinPerfT<>(fjp, generator, depth, playLeaves, moveType);
		} else {
			return new MultiThreadedPerfT<>(exec, generator, depth, playLeaves, moveType);
		}
	}
}