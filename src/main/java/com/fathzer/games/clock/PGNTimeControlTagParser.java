package com.fathzer.games.clock;

/** A <a href="https://github.com/mliebelt/pgn-spec-commented/blob/main/pgn-specification.md#961-tag-timecontrol">PGN time control tag</a> parser.
 */
public class PGNTimeControlTagParser {
	/** Builds clock settings based on a <a href="https://github.com/mliebelt/pgn-spec-commented/blob/main/pgn-specification.md#961-tag-timecontrol">PGN time control tag</a>.
	 * @param pgnTag A pgn time control tag
	 * @return a ClockSettings instance or null if time control is "-" (that means no time control).
	 * @throws IllegalArgumentException if time control is malformed or is "?" (which means the time control is unknown). 
	 */
	public ClockSettings toClockSettings(String pgnTag) {
		if (pgnTag==null) {
			throw new IllegalArgumentException("Argument is null");
		}
		pgnTag = pgnTag.trim();
		if ("-".equals(pgnTag)) {
			return null;
		} else if ("?".equals(pgnTag)) {
			throw new IllegalArgumentException("Unknown time control");
		}
		ClockSettings result = null;
		final String[] fields = pgnTag.split(":");
		for (int i=fields.length-1; i>=0; i--) {
			result = addField(result, fields[i].trim());
		}
		return result;
	}

	private ClockSettings addField(ClockSettings nextSettings, String field) {
		if (field.startsWith("*")) {
			throw new IllegalArgumentException("Sorry sand clock is not yet supported");
		}
		final String[] timeAndIncrement = field.split("\\+");
		if (timeAndIncrement.length>2) {
			throw new IllegalArgumentException("Control field "+field+" has more than one increment");
		}
		int[] time = parseCadency(timeAndIncrement[0]);
		final ClockSettings current = new ClockSettings(time[0]);
		if (timeAndIncrement.length==2) {
			int[] incParsed = parseCadency(timeAndIncrement[1]);
			current.withIncrement(incParsed[0], incParsed[1]==0?1:incParsed[1], true);
		}
		if (nextSettings!=null) {
			if (time[1]==0) {
				throw new IllegalArgumentException(field + " can't be followed by another control time period");
			}
			current.withNext(time[1], 0, nextSettings);
		} else if (time[1]>0) {
			current.withIncrement(time[0], time[1], false);
		}
		return current;
	}
	
	private int[] parseCadency(String field) {
		int index = field.indexOf('/');
		if (index<0) {
			return new int[] {Integer.parseInt(field),0};
		} else {
			return new int[] {
				Integer.parseInt(field.substring(index+1)),
				Integer.parseInt(field.substring(0, index))
			};
		}
	}

	/** Converts a clock settings to a PGN tag.
	 * @param settings The clock settings to convert.
	 * @return a non null String
	 */
	public String toTag(ClockSettings settings) {
		if (settings==null) {
			return "-";
		}
		final StringBuilder tag = new StringBuilder();
		do {
			if (!tag.isEmpty()) {
				tag.append(':');
			}
			tag.append(parseSingle(settings));
			settings = settings.getNext();
		} while (settings!=null);
		return tag.toString();
	}
	
	private String parseSingle(ClockSettings settings) {
		final StringBuilder result = new StringBuilder();
		final boolean hasNext = settings.getNext()!=null;
		if (hasNext) {
			result.append(settings.getMovesNumberBeforeNext());
			result.append('/');
		} else if (settings.getInitialTime()==settings.getIncrement()) {
			// Special case of 40/9000 like cadency
			return Integer.toString(settings.getMovesNumberBeforeIncrement())+"/"+Integer.toString(settings.getInitialTime());
		}
		result.append(settings.getInitialTime());
		if (settings.getIncrement()>0) {
			result.append('+');
			if (settings.getMovesNumberBeforeIncrement()!=1) {
				result.append(settings.getMovesNumberBeforeIncrement());
				result.append('/');
			}
			result.append(settings.getIncrement());
		}
		return result.toString();
	}
}
