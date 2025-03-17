package com.fathzer.games;

/** A player's color.
 * <br>Ok, there's a lot of games with two players and no "white" and "black".
 * <br>For instance, the <a href="https://en.wikipedia.org/wiki/Tic-tac-toe">Tic Tac Toe</a>, has "X" and "O" players.
 * <br>Ok, ok, so let say "white" means "X" and "black" means "O". The important thing is to identify both players, isn't it?.
 */
public enum Color {
	/** White player */
	WHITE, 
	/** Black player */
	BLACK;

	static {
		WHITE.opposite = BLACK;
		BLACK.opposite = WHITE;
	}
	
	private Color opposite;

	/** Gets the opposite color of this color.
	 * @return a color.
	 */
	public Color opposite() {
		return opposite;
	}
}
