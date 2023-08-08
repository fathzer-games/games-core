[![Maven Central](https://img.shields.io/maven-central/v/com.fathzer/games-core)](https://central.sonatype.com/artifact/com.fathzer/games-core)
[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](https://github.com/fathzer-games/games-core/blob/master/LICENSE)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=fathzer_games-core&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=fathzer-games_games-core)
[![javadoc](https://javadoc.io/badge2/com.fathzer/games-core/javadoc.svg)](https://javadoc.io/doc/com.fathzer/games-core)

# games-core
A core library to help implement two players games

## TODO
- Negamax: Store cut moves in transposition table, see if we can speed up sort in case a best move is registered for position (currently, there's a lot of list content manipulation). Try to understand why Wikipedia tt implementation seems not to work :-(
- AlphaBeta: Verify it's still working and implement move sorting
- Write tests and Documentation ;-)
- Test with Sonar
- Publish artifact on Maven central
