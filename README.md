[<img src="https://img.shields.io/github/v/release/jazzm0/tactic-master.svg?logo=github" alt="Version" height="40">]
(https://f-droid.org/packages/com.daemon.ssh/)

# Tactic Master

An offline chess tactic trainer for Android based on a subset of
the [lichess tactics database](https://database.lichess.org/lichess_db_puzzle.csv.zst).

## Privacy policy

**WE DO NOT STORE ANY DATA OR MESSAGES YOU PROCESS WITH THE APPLICATION. PERIOD.**

## Compilation

Simply clone the repository and import it to Android Studio.

## Tactics Trainer Module

The `tactics-trainer` module contains several Python scripts that help in processing and managing
the chess tactics data:

- **`download.py`**: Downloads the latest puzzles from the Lichess database.
- **`chess_viewer.py`**: Displays a chess board with the given FEN position.
- **`converter.py`**: Processes the downloaded puzzles, convert the from csv to sqlite format.
- **`analyzer.py`**: Generates statistics from the processed puzzles, such as the number
  of puzzles per rating distribution.
- **`validator.py`**: Processes the downloaded puzzles to filter and validate them according to certain rules.

## Icons

I used the piece icons from chess.com

## License

This project is licensed under the GNU GENERAL PUBLIC LICENSE Version 2 or any later version.


#TODO: Add the following badges
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
alt="Get it on F-Droid"
height="80">](https://f-droid.org/en/packages/com.tacticmaster/)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
alt="Get it on Google Play"
height="80">](https://play.google.com/store/apps/details?id=com.tacticmaster)