package com.fathzer.games.perft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * A parser of text files containing <a href="https://www.chessprogramming.org/Perft">PerfT</a> tests data set.
 * <br>Here is the file format:<ul>
 * <li>Lines starting with # are comments.</li>
 * <li>Empty or blank lines before first test data should be ignored.</li>
 * <li>A perfT test starts with a name line and ends at the next name line (or the end of file).<ul>
 *   <li>A name line starts with 'name ' followed by an id of the test (This is configurable with {@link #withNamePrefix(String)}).</li>
 *   <li>The line immediately after the name line contains the position in FEN format variant where half move count and move number are missing.</li>
 *   <li>The position is prefixed by 'start ' (This is configurable with {@link #withStartPositionPrefix(String)}).</li>
 *   <li>The other lines are result lines. They start with 'perft ' (This is configurable with {@link #withResultPrefix(String)}), followed by a depth and the number of leaves at that depth in the tree obtained by playing all possible moves.</li>
 *   <li>The first result line is for depth 1. Second for depth 2, etc ... With no missing depth between 1 and the maximum depth for that test.
 *   The maximum depth can vary from one test to another (This is configurable with {@link #withResultPrefix(String)}).</li>
 * </ul></li>
 * </ul>
 */
public class PerfTParser {
	private String startPositionPrefix = "start";
	private String resultPrefix = "perft";
	private String namePrefix = "name";
	private UnaryOperator<String> startPositionCustomizer = s->s;
	
	/** Sets the prefix of name lines.
	 * @param namePrefix the prefix to set ("name" is the default value)
	 * @return this instance for fluent API
	 */
	public final PerfTParser withNamePrefix(String namePrefix) {
		this.namePrefix = namePrefix;
		return this;
	}

	/** Sets the prefix of start position lines.
	 * @param startPositionPrefix the prefix to set ("start" is the default value)
	 * @return this instance for fluent API
	 */
	public final PerfTParser withStartPositionPrefix(String startPositionPrefix) {
		this.startPositionPrefix = startPositionPrefix;
		return this;
	}

	/** Sets a customizer for start position lines.
	 * <br>The customizer is applied to the position string before putting it in the result.
	 * <br>Typically, in chess, start positions are often expressed in <a href="https://en.wikipedia.org/wiki/Forsyth-Edwards_Notation">FEN</a> format.
	 * <br>The move number and half move count are useless in perfT tests and can be removed from the text file and added to their default
	 * values on the fly by this customizer.
	 * @param startPositionCustomizer the customizer to set
	 * @return this instance for fluent API
	 */
	public final PerfTParser withStartPositionCustomizer(UnaryOperator<String> startPositionCustomizer) {
		this.startPositionCustomizer = startPositionCustomizer;
		return this;
	}

	/** Sets the prefix of result lines.
	 * @param resultPrefix the prefix to set ("perft" is the default value)
	 * @return this instance for fluent API
	 */
	public final PerfTParser withResultPrefix(String resultPrefix) {
		this.resultPrefix = resultPrefix;
		return this;
	}

	/** Reads a list of {@link PerfTTestData} from an input stream.
	 * @param stream The stream to read (warning, this method does not close the stream)
	 * @param cs The charset of the stream
	 * @return A list of {@link PerfTTestData}
	 * @throws IOException If an I/O error occurs
	 */
	public List<PerfTTestData> read(InputStream stream, Charset cs) throws IOException {
		final List<PerfTTestData> tests = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new UnclosedReader(stream, cs))) {
			PerfTTestData current = null;
			String name = null;
			String start = null;
			int lineNumber = 0;
			for (String line = reader.readLine(); line!=null; line = reader.readLine()) {
				lineNumber++;
				if (line.startsWith(namePrefix)) {
					name = line.substring(namePrefix.length()).trim();
					current = null;
				} else if (line.startsWith(startPositionPrefix)) {
					start = startPositionCustomizer.apply(line.substring(startPositionPrefix.length()).trim());
					current = null;
				} else if (line.startsWith(resultPrefix)) {
					if (current==null) {
						if (start==null) {
							throw new IllegalArgumentException("Found "+resultPrefix+" before any start position");
						} else {
							current = new PerfTTestData(name, start);
							name = null;
							start = null;
							tests.add(current);
						}
					}
					addResultLine(current, lineNumber, line);
				}
			}
		}
		return tests;
	}

	private void addResultLine(PerfTTestData current, int lineNumber, String line) throws IOException {
		final String[] fields = line.substring(resultPrefix.length()).trim().split(" ");
		try {
			final int depth = Integer.parseInt(fields[0]);
			if (depth!=current.getSize()+1) {
				throw new IllegalArgumentException("Seems depths are not sequential or don't start at 1");
			}
			final long count = Long.parseLong(fields[1]);
			current.add(count);
		} catch (RuntimeException e) {
			throw new IOException("Problem while parsing line "+lineNumber,e);
		}
	}
	
	private static class UnclosedReader extends InputStreamReader {
		public UnclosedReader(InputStream in, Charset set) {
			super(in, set);
		}

		@Override
		public void close() throws IOException {
			// Don't close underlying stream
		}
	}
}
