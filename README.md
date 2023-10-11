[![Maven Central](https://img.shields.io/maven-central/v/com.fathzer/games-core)](https://central.sonatype.com/artifact/com.fathzer/games-core)
[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](https://github.com/fathzer-games/games-core/blob/master/LICENSE)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=fathzer_games-core&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=fathzer-games_games-core)
[![javadoc](https://javadoc.io/badge2/com.fathzer/games-core/javadoc.svg)](https://javadoc.io/doc/com.fathzer/games-core)

# games-core
A core library to help implement two players game engines

It provides you with a ready to use and highly configurable [iterative deepening](https://www.chessprogramming.org/Iterative_Deepening) [Negamax](https://en.wikipedia.org/wiki/Negamax) implementation using configurable [transposition table](https://en.wikipedia.org/wiki/Transposition_table).

In order to have a working engine for your favorite game, you have to implement your own MoveGenerator and Evaluator for this game ... and use one of the provided ai (I recommend IterativeDeepeningEngine).  
You can implement your own transposition table and its policy (which positions to save or how to reuse them during the search). This library contains a basic implementation, try it to see if it is enough for your needs.

# Known bugs
- The iterative deepening search always stop deepening when it find a winning move. It prevents finding other deeper winning moves. It's not a problem when playing a game, but for analysis, it is one.
- Using non exact entries in transposition table to update the alpha/beta values leads to invalid results.  
For example: 4n2r/2k1Q2p/5B2/2N5/2B2R2/1P6/3PKPP1/6q1 b - - 2 46 in JChess engine at depth 7 returns a score of -1600 (PV: c7-c6, e7-d7, c6-b6, c5-a4, b6-a5, d7-b5) instead of M-3 (PV: c7-c6, f6-h8, g1-g2, e7-e8, c6-c5, e8-d8, g2-f2).

## TODO
- Maybe TTAi scoreToTT and ttToScore would be at a better place in TranspositionTablePolicy. Another, maybe better, approach is to compute fixed (mat) values in Negamax class and have a flag in the table (and in its store method) to explicitly set the stored value as a fixed value. It would allow those values to be used regardless of the depth at which they are recorded.
- EvaluatedMove.compareTo does not sort in natural order which can be confusing
- There's a strange behavior with transposition table. There's some situations where a never replace strategy leads to a faster resolution. It is totally counter intuitive.
- MoveGenerator:
  - It would be more efficient to have the moveGenerator generating directly an iterator of moves instead of a list of legal moves. It would allow to use incremental move generator that start by returning, what can be the best moves (for instance captures), and other moves when these first moves are consumed. Indeed, in alpha/beta ai, the first moves are often enough to trigger a cut, so generating all the moves is a waste of time.
  - Have a method to verify a move is pseudo-legal. This is useful when a "best" move is retrieved from transposition table. As transposition table's keys can have collision, the move retrieved can come from a totally different position and not be legal at all with current position. Currently, we verify that the move is contained in the whole moves list, which is very inefficient.
  - In order to implement quiesce evaluation, we need a method that returns the quiesce moves. Default implementation could return an empty iterator that leads to make no quiesce search.
- Negamax: 
  - Do not invoke MoveGenerator.getMoves before trying the best move from TT. It require the MoveGenerator to be able to test if move is valid.
  - Implement quiesce evaluation
- Write tests and Documentation ;-)
- Test with Sonar
- Publish artifact on Maven central
