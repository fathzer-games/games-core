package com.fathzer.games.ai.experimental;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import com.fathzer.games.MoveGenerator;

/**
 * A spy that can be used to monitor the search process of {@link Negamax3}.
 * <br>It is called by the search process at various points.
 * @param <M> The type of the moves
 * @param <B> The type of the {@link MoveGenerator} to use
 */
public abstract class KeyBasedNegaMaxSpyFilter<M,B extends MoveGenerator<M>> {
	private int enteringDepth=-1;
	private final long searchedKey;
	private int atMaxDepth;
	
	protected KeyBasedNegaMaxSpyFilter(long searched) {
		this.searchedKey = searched;
		this.atMaxDepth = -1;
	}
	
	/** Tests whether the filter is switched on (the event should not be ignored).
	 * @return true if the event should not be ignored
	 */
	public boolean isOn() {
		return enteringDepth>=0;
	}
	
	public void setAtMaxDepth(int atMaxDepth) {
		this.atMaxDepth = atMaxDepth;
	}

	/** Should be called when entering a position.
	 * @param state The current search stack
	 * @return true if filter switches to on
	 */
	public boolean enter(TreeSearchStateStack<M, B> state) {
		if (getKey(state)==searchedKey && enteringDepth<0 && (atMaxDepth<0 || atMaxDepth==state.maxDepth)) {
			enteringDepth = state.getCurrentDepth();
			return true;
		}
		return false;
	}
	
	public abstract long getKey(TreeSearchStateStack<M, B> state);
	
	/** Should be called when leaving a position.
	 * @param state The current search stack
	 * @return true if filter switches to off
	 */
	public boolean exit(TreeSearchStateStack<M, B> state) {
		if (isOn() && getKey(state)==searchedKey && state.getCurrentDepth()==enteringDepth) {
			enteringDepth=-1;
			return true;
		}
		return false;
	}

	public static CharSequence getTab(TreeSearchStateStack<?, ?> state) {
		final StringBuilder builder = new StringBuilder();
		IntStream.range(state.getCurrentDepth(),state.maxDepth).forEach(i -> builder.append(' '));
		return builder;
	}
	
	public static <M, B extends MoveGenerator<M>> List<String> getMoves(TreeSearchStateStack<M, B> state, Function<M,String> toString) {
		return IntStream.rangeClosed(state.getCurrentDepth()+1, state.maxDepth).map(i -> state.getCurrentDepth()+1 + state.maxDepth - i)
			.mapToObj(i->{
				final M mv = state.get(i).lastMove;
				return mv==null ? "?"+i: toString.apply(mv);
			})
			.toList();
	}
}