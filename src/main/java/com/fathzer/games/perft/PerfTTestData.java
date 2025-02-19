package com.fathzer.games.perft;

import java.util.ArrayList;
import java.util.List;

public class PerfTTestData {
	private String startPosition;
	private String name;
	private List<Long> counts;
	
	public PerfTTestData(String name, String startPosition) {
		if (startPosition==null) {
			throw new NullPointerException("startPosition can't be null");
		}
		this.startPosition = startPosition;
		this.counts = new ArrayList<>();
		this.name = name;
	}

	public void add(long count) {
		this.counts.add(count);
	}
	
	public String getStartPosition() {
		return startPosition;
	}
	
	public String getName() {
		return name;
	}

	public int getSize() {
		return counts.size();
	}

	public long getCount(int depth) {
		return counts.get(depth-1);
	}
}