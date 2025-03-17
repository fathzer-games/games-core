/** Classes to perform <a href="https://www.chessprogramming.org/Perft">PerfT</a> tests.
 * <br>The idea of ​​checking that the number of leaves of the tree of possible moves, for a given starting
 * position and at a given depth, is the expected one can be used in any other game as long as the number
 * of moves is not infinite.
 * <br><br>
 * When performed on a significant number of starting positions with sufficient depth (it all depends on the game),
 * these tests constitute a fairly reliable proof-by-nine of the proper functioning of a move generator.
 * <br>
 * <br>{@link com.fathzer.games.perft.PerfTBuilder} is the main class to use to create these tests.
 * <br>{@link PerfTParser} can be used to read a set of tests data ({@link com.fathzer.games.perft.PerfTTestData})
 * from a text file.
*/
package com.fathzer.games.perft;
