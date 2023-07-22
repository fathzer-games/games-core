package com.fathzer.games.ai.transposition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum EntryType {
	EXACT, LOWER_BOUND, UPPER_BOUND;
	
	public static final List<EntryType> ALL = Collections.unmodifiableList(Arrays.asList(EXACT, LOWER_BOUND, UPPER_BOUND)); 
}
