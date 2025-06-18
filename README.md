[<img src="https://img.shields.io/github/v/release/jazzm0/tactic-master.svg?logo=github" alt="Version" height="40">](https://f-droid.org/en/packages/com.tacticmaster/)

# Tactic Master

An offline chess tactic trainer for Android based on a subset of
the [lichess tactics database](https://database.lichess.org/lichess_db_puzzle.csv.zst).

## Privacy policy

**WE DO NOT STORE ANY DATA OR MESSAGES YOU PROCESS WITH THE APPLICATION. PERIOD.**

## Compilation

Simply clone the repository and import it to Android Studio.

## Bugs

Please report any bugs or issues you find in the [issue tracker](https://github.com/jazzm0/tactic-master/issues). 
Make sure you use the latest version of the app before reporting an issue. F-Droid takes some time to update the app,
so you may want to check the [GitHub releases](https://github.com/jazzm0/tactic-master/releases) Page for the latest version.

When reporting an issue, please include the following information:
- The puzzle ID with which the issue can be replicated.
- The exact steps to reproduce the issue.
- The expected behavior and the actual behavior you observed.

## Tactics Trainer Module

The `tactics-trainer` module contains several Python scripts that help in processing and managing
the chess tactics data:

- **`download.py`**: Downloads the latest puzzles from the Lichess database.
- **`chess_viewer.py`**: Displays a chess board with the given FEN position.
- **`converter.py`**: Processes the downloaded puzzles, convert the from csv to sqlite format.
- **`analyzer.py`**: Generates statistics from the processed puzzles, such as the number
  of puzzles per rating distribution.
- **`validator.py`**: Processes the downloaded puzzles to filter and validate them according to
  certain rules.

## Icons

I used the piece icons from chess.com

## License

This project is licensed under the GNU GENERAL PUBLIC LICENSE Version 3.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
alt="Get it on F-Droid"
height="80">](https://f-droid.org/en/packages/com.tacticmaster/)

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
alt="Get it on Google Play"
height="80">](https://play.google.com/store/apps/details?id=com.tacticmaster)
