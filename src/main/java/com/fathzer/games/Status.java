package com.fathzer.games;

/** The status of a game (playing, draw, white or black won).
 */
public enum Status {
	/** The game is still playing */
	PLAYING, 
	/** The game ends with a draw */
	DRAW, 
	/** The white player has won */
	WHITE_WON, 
	/** The black player has won */
	BLACK_WON;
	
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
