package com.fathzer.games.perft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class PerfTParser {
	private String startPositionPrefix = "start";
	private String resultPrefix = "perft";
	private String namePrefix = "name";
	
	public final PerfTParser withStartPositionPrefix(String startPositionPrefix) {
		this.startPositionPrefix = startPositionPrefix;
		return this;
	}

	public final PerfTParser withResultPrefix(String resultPrefix) {
		this.resultPrefix = resultPrefix;
		return this;
	}

	public final PerfTParser withNamePrefix(String idPrefix) {
		this.namePrefix = idPrefix;
		return this;
	}

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
					start = line.substring(startPositionPrefix.length()).trim();
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
