package com.fathzer.games.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.fathzer.games.util.Evaluation;

public final class SearchResult<M> {
		private final LinkedList<Evaluation<M>> result;
		private final int count;
		private final int delta;
		private volatile int currentLow = Integer.MIN_VALUE;
		
		SearchResult(int count, int delta) {
			this.count = count;
			this.delta = delta;
			this.result = new LinkedList<>();
		}
		
		synchronized int getLow() {
			return currentLow;
		}
		
		synchronized void add(M move, int value) {
			insert(this.result, new Evaluation<M>(move, value));
			if (result.size()>=count) {
				currentLow = result.get(count-1).getValue() - delta -1;
			}
		}
		
		/** Gets the list of moves evaluation, truncated to the number of moves requested in this instance constructor.
		 * @return The list of evaluated moves
	     * <br>Please note the list may have more than size elements in case of equivalent moves or almost equivalent moves.
	     * It can also have less than size elements if there's less than size legal moves. 
		 */
		public synchronized List<Evaluation<M>> getCut() {
			final List<Evaluation<M>> cut = new ArrayList<>(result.size());
			final int low = getLow();
			int currentCount = 0;
			for (Evaluation<M> ev : result) {
				if (ev.getValue()>low || currentCount<count) {
					cut.add(ev);
					currentCount++;
				}
			}
			return cut;
		}
		
		/** Gets the list of moves evaluation.
		 * @return The list of evaluation of all valid moves
	     * <br>Please note the list may contain upper bounded evaluation (moves we determine they are not good enough to be selected in {@link #getCut()}).
		 */
		public List<Evaluation<M>> getList() {
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