[![Maven Central](https://img.shields.io/maven-central/v/com.fathzer/games-core)](https://central.sonatype.com/artifact/com.fathzer/games-core)
[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](https://github.com/fathzer-games/games-core/blob/master/LICENSE)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=fathzer_games-core&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=fathzer-games_games-core)
[![javadoc](https://javadoc.io/badge2/com.fathzer/games-core/javadoc.svg)](https://javadoc.io/doc/com.fathzer/games-core)

# games-core
A core library to help implement two players game engines.

It provides you with a ready to use and highly configurable [iterative deepening](https://www.chessprogramming.org/Iterative_Deepening) [Negamax](https://en.wikipedia.org/wiki/Negamax) implementation using configurable [transposition table](https://en.wikipedia.org/wiki/Transposition_table).

In order to have a working engine for your favorite game, you have to implement your own MoveGenerator and Evaluator for this game ... and use one of the provided ai (I recommend IterativeDeepeningEngine).  
You can implement your own transposition table and its policy (which positions to save or how to reuse them during the search). This library contains a basic implementation, try it to see if it is enough for your needs.

It also provides you with other useful building blocks like a clock, PerfT tests (for testing your move generator), or abstract implementations of move library (typically openings book for chess).

# Known limitations
- The AI always supposes that when a player can't move the game is ended ... which is not always the case (typically in [Reversi](https://en.wikipedia.org/wiki/Reversi)).  
This doesn't means that the AI can't be used to play Reversi. Simply, the reversi move generator should have a special '*can't play*' move returned when a player can't move but the game is not finished.
- The Negamax is quite basic, it implements a highly configurable transposition and quiesce move search, but none other advanced algorithm (no PV search, futility pruning, killer or null move, etc ...).

## TODO (probable breaking changes)
- com.fathzer.games.ai.AbstractAI requires an ExecutionContext in its constructor. I think this is not a good approach (even it was mine ;-)) to have this non standard implementation detail exposed outside this public class. Moreover, this disallow building different multi-threading models in AI implementation. Typically it is currently impossible to use a fork/join pool to perform the search.
As it seems, in real life, a new context is created at each AI invocation, a better approach would be to let the AI manage its own threading scheme, and simply pass a SearchContext to the constructor.

## TODO (maybe)
- Make MoveLibrary implement AI?
- There's probably a bug or something very misleading in ClockSettings: withNext has a number of plies arguments, but it seems that the usage is to deal with number of moves (not plies which are half moves).
- Maybe TTAi scoreToTT and ttToScore would be at a better place in TranspositionTablePolicy. Another, maybe better, approach is to compute fixed (mat) values in Negamax class and have a flag in the table (and in its store method) to explicitly set the stored value as a fixed value. It would allow those values to be used regardless of the depth at which they are recorded.
- There's a strange behavior with transposition table. There's some situations where a never replace strategy leads to a faster resolution. It is totally counter intuitive.
- MoveGenerator:
  - It would be more efficient to have the moveGenerator generating directly an iterator of moves instead of a list of moves. It would allow to use incremental move generators that starts by returning, what can hope to be the best moves (for instance captures), and other moves if these first moves are consumed. Indeed, in alpha/beta ai, the first moves are often enough to trigger a cut, so generating all the moves is a waste of time.
- Write more tests ;-)
