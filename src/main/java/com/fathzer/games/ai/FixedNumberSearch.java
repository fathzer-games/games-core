package com.fathzer.games.ai;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.fathzer.games.util.Evaluation;

final class FixedNumberSearch<M> {
		private LinkedList<Evaluation<M>> result;
		private final int count;
		private final int delta;
		private volatile int currentLow = Integer.MIN_VALUE;
		
		FixedNumberSearch(int count, int delta) {
			this.count = count;
			this.delta = delta;
			this.result = new LinkedList<>();
		}
		synchronized int getLow() {
			return currentLow;
		}
		synchronized void add(M move, int value) {
			if (result.size()<count) {
				insert(this.result, new Evaluation<M>(move, value));
				if (result.size()==count) {
					currentLow = result.peekLast().getValue()-delta-1;
				}
			} else if (value>currentLow) {
				insert(this.result, new Evaluation<M>(move, value));
				currentLow = result.get(count-1).getValue() - delta -1;
			}
		}
		List<Evaluation<M>> getResult() {
			if (result.size()>count) {
				// Delete extra results
				final int limit = result.get(count-1).getValue()-delta;
				while (result.peekLast().getValue()<limit) {
					result.pollLast();
				}
			}
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