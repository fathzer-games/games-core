package com.fathzer.games.perft;

import java.util.ArrayList;
import java.util.List;

/**
 * A test data for a <a href="https://www.chessprogramming.org/Perft">Perft</a> test on one start position.
 */
public class PerfTTestData {
	private String startPosition;
	private String name;
	private List<Long> counts;
	
	/**
	 * Creates a new test data.
	 * @param name The name of the test
	 * @param startPosition The start position of the test
	 */
	public PerfTTestData(String name, String startPosition) {
		if (startPosition==null) {
			throw new NullPointerException("startPosition can't be null");
		}
		this.startPosition = startPosition;
		this.counts = new ArrayList<>();
		this.name = name;
	}

	/**
	 * Adds an expected leave count to the test data.
	 * <br>The first call to this method is for depth 1, the second for depth 2, etc ...
	 * @param expectedLeaveCount The count to add
	 */
	public void add(long expectedLeaveCount) {
		this.counts.add(expectedLeaveCount);
	}
	
	/**
	 * Gets the name of the test.
	 * @return a String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the start position of the test.
	 * @return a String
	 */
	public String getStartPosition() {
		return startPosition;
	}
	
	/**
	 * Gets the number of expected leave counts.
	 * @return an int
	 */
	public int getSize() {
		return counts.size();
	}

	/**
	 * Gets the expected leave count for a given depth.
	 * @param depth The depth
	 * @return a long
	 */
	public long getExpectedLeaveCount(int depth) {
		return counts.get(depth-1);
	}
}