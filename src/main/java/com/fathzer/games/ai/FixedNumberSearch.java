package com.fathzer.games.ai;

import java.util.ArrayList;
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
			insert(this.result, new Evaluation<M>(move, value));
			if (result.size()>=count) {
				currentLow = result.get(count-1).getValue() - delta -1;
			}
		}
		synchronized List<Evaluation<M>> getCut() {
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
		List<Evaluation<M>> getList() {
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