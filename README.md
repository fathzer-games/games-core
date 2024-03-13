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
- The AI always supposes a player that can't move loosed or make a draw ... which is not always the case (typically in [Reversi](https://en.wikipedia.org/wiki/Reversi)).
- The iterative deepening search always stops deepening when it finds a winning move. It prevents finding other deeper winning moves. It's not a problem when playing a game, but for analysis, it is one.

## TODO
- Filter the library moves with candidates moves in IterativeDeepengingEngine.
- The transposition table *newPosition* method should have the board has an argument in order to, for instance, compute a generation number based on board characteristics.
- There's probably a bug or something very misleading in ClockSettings: withNext has a number of plies arguments, but it seems that the usage is to deal with number of moves (not plies which are half moves).
- Maybe TTAi scoreToTT and ttToScore would be at a better place in TranspositionTablePolicy. Another, maybe better, approach is to compute fixed (mat) values in Negamax class and have a flag in the table (and in its store method) to explicitly set the stored value as a fixed value. It would allow those values to be used regardless of the depth at which they are recorded.
- EvaluatedMove.compareTo does not sort in natural order which can be confusing
- There's a strange behavior with transposition table. There's some situations where a never replace strategy leads to a faster resolution. It is totally counter intuitive.
- MoveGenerator:
  - It would be more efficient to have the moveGenerator generating directly an iterator of moves instead of a list of moves. It would allow to use incremental move generators that start by returning, what can hope to be the best moves (for instance captures), and other moves if these first moves are consumed. Indeed, in alpha/beta ai, the first moves are often enough to trigger a cut, so generating all the moves is a waste of time.
- Write tests and Documentation ;-)
- Test with Sonar
- Publish artifact on Maven central
