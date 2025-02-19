package com.fathzer.games;

/** The status of a game (playing, draw, white or black won).
 */
public enum Status {
	PLAYING, DRAW, WHITE_WON, BLACK_WON;
	
	/** Gets the winner's color.
	 * @return A color or null if there's no winner
	 */
	public Color winner() {
		if (WHITE_WON==this) {
			return Color.WHITE;
		} else if (BLACK_WON==this) {
			return Color.BLACK;
		} else {
			return null;
		}
	}
}
