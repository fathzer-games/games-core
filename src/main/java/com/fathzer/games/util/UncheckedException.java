package com.fathzer.games.util;

/** A generic runtime exception to encapsulate checked exceptions
 */
public class UncheckedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/** Constructor
	 * @param cause The exception's cause
	 */
	public UncheckedException(Throwable cause) {
		super(cause);
	}
}