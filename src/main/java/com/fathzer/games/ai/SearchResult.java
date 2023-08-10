package com.fathzer.games.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.fathzer.games.util.EvaluatedMove;

/** The result of a best move search.
 */
public final class SearchResult<M> {
	private final LinkedList<EvaluatedMove<M>> result;
	private final int count;
	private final int delta;
		
	SearchResult(int count, int delta) {
		this.count = count;
		this.delta = delta;
		this.result = new LinkedList<>();
	}
	
	synchronized int getLow() {
		return result.size()>=count ? result.get(count-1).getScore() - delta -1 : Integer.MIN_VALUE;
	}
	
	public synchronized void add(M move, Evaluation value) {
		insert(this.result, new EvaluatedMove<M>(move, value));
	}
	
	public synchronized void update(M move, Evaluation value) {
		final int index = getIndex(move);
		if (index>=0) {
			result.remove(index);
		}
		add(move, value);
	}
	
	synchronized int getIndex(M move) {
		int index = 0;
		for (EvaluatedMove<M> ev : result) {
			if (ev.getContent().equals(move)) {
				return index;
			} else {
				index++;
			}
		}
		return -1;
	}
	
	/** Gets the list of moves evaluation, truncated to the number of moves requested in this instance constructor.
	 * @return The sorted (best first) list of better moves
     * <br>Please note the list may have more than size elements in case of equivalent moves or almost equivalent moves.
     * It can also have less than size elements if there's less than size legal moves or search was interrupted before it finished. 
	 */
	public synchronized List<EvaluatedMove<M>> getCut() {
		final List<EvaluatedMove<M>> cut = new ArrayList<>(result.size());
		final int low = getLow();
		int currentCount = 0;
		for (EvaluatedMove<M> ev : result) {
			if (ev.getScore()>low || currentCount<count) {
				cut.add(ev);
				currentCount++;
			}
		}
		return cut;
	}
	
	/** Gets the list of moves evaluation.
	 * @return The list sorted (best first) of all valid moves
     * <br>Please note the list may contain upper bounded evaluation (moves we determine they are not good enough to be selected in {@link #getCut()}).
     * <br>Please note this list may not contains all valid moves if search wass interrupted before it finished.
	 */
	public List<EvaluatedMove<M>> getList() {
		return result;
	}

	private static <T extends Comparable<T>> void insert(List<T> list, T element) {
	    int index = Collections.binarySearch(list, element);
	    if (index < 0) {
	        index = -index - 1;
	    }
	    list.add(index, element);
	}
}