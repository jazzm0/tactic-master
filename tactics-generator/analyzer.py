import sqlite3

from matplotlib import pyplot as plt


def plot_histogram(sqlite_input_db_path):
    ratings = []

    conn_input = sqlite3.connect(sqlite_input_db_path)
    cursor_input = conn_input.cursor()
    cursor_input.execute("SELECT * FROM lichess_db_puzzle ORDER BY Rating DESC")
    headers = [description[0] for description in cursor_input.description]

    for row in cursor_input:
        row_dict = dict(zip(headers, row))
        ratings.append(int(row_dict['Rating']))


    plt.figure(figsize=(10, 6))
    plt.hist(ratings, bins=50, alpha=0.75, edgecolor='black')
    plt.title('Rating Distribution of Chess Puzzles')
    plt.xlabel('Rating')
    plt.ylabel('Frequency')
    plt.show()


plot_histogram("validated_puzzles.db")
