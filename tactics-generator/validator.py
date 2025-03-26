import os
import sqlite3
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed

import chess
import chess.engine
import numpy as np
from intervaltree import IntervalTree, Interval
from tqdm import tqdm

lock = threading.Lock()


def create_interval_tree_with_distribution(lowest_rating, highest_rating, mean_rating, std_dev, max_puzzles, step=10):
    # Generate a normal distribution of ratings
    normal_ratings = np.random.normal(mean_rating, std_dev, max_puzzles).astype(int)

    # Create an IntervalTree
    tree = IntervalTree()
    for start in range(lowest_rating, highest_rating, step):
        end = start + step
        count = ((normal_ratings >= start) & (normal_ratings < end)).sum()
        tree[start:end] = count

    return tree


def view_db_content(db_path):
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
    tables = cursor.fetchall()

    for table in tables:
        print(f"Table: {table[0]}")
        cursor.execute(f"SELECT * FROM {table[0]};")
        rows = cursor.fetchall()
        for row in rows:
            print(row)
        print("\n")

    conn.close()


def validate_move(engine_path, fen, moves):
    engine = chess.engine.SimpleEngine.popen_uci(engine_path)
    engine.configure({"Skill Level": 20})

    board = chess.Board(fen)
    board.push_uci(moves[0])
    result = engine.play(board, chess.engine.Limit(time=2.0))
    stockfish_move = result.move
    info = engine.analyse(board, chess.engine.Limit(time=2.0))
    score = info['score'].relative.score(mate_score=10000)

    if score is None:
        winning_chance = None
    elif score > 0:
        winning_chance = 1 / (1 + 10 ** (-score / 400))
    else:
        winning_chance = 1 - 1 / (1 + 10 ** (score / 400))

    engine.quit()
    return stockfish_move.uci() == moves[1] and winning_chance > 0.9


def create_table(cursor, table_name, headers):
    column_types = {header: 'TEXT' for header in headers}
    for col in ['Rating', 'RatingDeviation', 'Popularity', 'NbPlays']:
        if col in column_types:
            column_types[col] = 'INTEGER'
    columns = ', '.join([f'"{header}" {column_types[header]}' for header in headers])
    cursor.execute(f'CREATE TABLE IF NOT EXISTS "{table_name}" ({columns}, UNIQUE("PuzzleId"))')


def insert_puzzle(cursor, table_name, headers, row, written_puzzle_ids, pbar: tqdm):
    with lock:
        if row['PuzzleId'] in written_puzzle_ids or len(written_puzzle_ids) >= max_puzzles:
            return
        written_puzzle_ids.add(row['PuzzleId'])
        pbar.update(1)
        placeholders = ', '.join(['?' for _ in headers])
        try:
            cursor.execute(f'INSERT INTO "{table_name}" VALUES ({placeholders})', [row[header] for header in headers])
        except sqlite3.IntegrityError:
            pass


def process_tasks(tasks, cursor_output, table_name, headers, written_puzzle_ids, pbar, max_puzzles,
                  incorrect_puzzle_ids, interval_tree):
    for task in as_completed(tasks):
        row_dict = task.row_dict
        if len(written_puzzle_ids) >= max_puzzles:
            break
        if task.result():
            rating = int(row_dict['Rating'])
            intervals = interval_tree[rating]
            if intervals:
                interval = intervals.pop()
                interval_tree.remove(interval)
                new_interval = Interval(interval.begin, interval.end, interval.data - 1)
                if new_interval.data > 0:
                    interval_tree.add(new_interval)
                    insert_puzzle(cursor_output, table_name, headers, row_dict, written_puzzle_ids, pbar)
        else:
            incorrect_puzzle_ids.add(row_dict['PuzzleId'])


def validate_and_store_moves(sqlite_input_db_path, engine_path, sqlite_output_db_path, lowest_rating, highest_rating,
                             mean_rating, std_dev, max_puzzles, step=10):
    if os.path.exists(sqlite_output_db_path):
        os.remove(sqlite_output_db_path)

    written_puzzle_ids, incorrect_puzzle_ids = set(), set()
    conn_input = sqlite3.connect(sqlite_input_db_path)
    cursor_input = conn_input.cursor()
    conn_output = sqlite3.connect(sqlite_output_db_path)
    cursor_output = conn_output.cursor()

    cursor_input.execute("SELECT * FROM lichess_db_puzzle ORDER BY Popularity DESC, Rating DESC, NbPlays DESC")
    headers = [description[0] for description in cursor_input.description]
    table_name = "lichess_db_puzzle"
    create_table(cursor_output, table_name, headers)

    interval_tree = create_interval_tree_with_distribution(lowest_rating, highest_rating, mean_rating, std_dev,
                                                           max_puzzles, step)

    tasks = []
    with ThreadPoolExecutor() as executor, tqdm(total=max_puzzles) as pbar:
        for row in cursor_input:
            if len(written_puzzle_ids) >= max_puzzles:
                break
            row_dict = dict(zip(headers, row))
            rating = int(row_dict['Rating'])
            popularity = int(row_dict['Popularity'])
            nb_plays = int(row_dict['NbPlays'])
            rating_deviation = int(row_dict['RatingDeviation'])

            if not (lowest_rating <= rating <= highest_rating and
                    popularity >= 90 and
                    nb_plays >= 1000 and
                    rating_deviation < 100):
                continue

            fen = row_dict['FEN']
            moves = row_dict['Moves'].split()
            if len(moves) < 2:
                print(f"Puzzle ID {row_dict['PuzzleId']}: Not enough moves to validate")
                continue

            if row_dict['PuzzleId'] in written_puzzle_ids:
                continue

            future = executor.submit(validate_move, engine_path, fen, moves)
            future.row_dict = row_dict
            tasks.append(future)

            if len(tasks) >= 10:
                process_tasks(tasks, cursor_output, table_name, headers, written_puzzle_ids, pbar, max_puzzles,
                              incorrect_puzzle_ids, interval_tree)
                tasks = []

        process_tasks(tasks, cursor_output, table_name, headers, written_puzzle_ids, pbar, max_puzzles,
                      incorrect_puzzle_ids, interval_tree)

    conn_output.commit()
    conn_output.close()
    conn_input.close()


engine_path = '/opt/homebrew/Cellar/stockfish/17/bin/stockfish'
sqlite_db_path = 'validated_puzzles.db'
sqlite_input_db_path = 'all_puzzles.db'
max_puzzles = 100000
validate_and_store_moves(sqlite_input_db_path, engine_path, sqlite_db_path, 1600, 3500, 2200, 300, max_puzzles)
# view_db_content(sqlite_db_path)
