import sqlite3

from matplotlib import pyplot as plt


def plot_histogram(sqlite_input_db_path):
    ratings = []
    popularity = []
    plays = []

    conn_input = sqlite3.connect(sqlite_input_db_path)
    cursor_input = conn_input.cursor()
    cursor_input.execute("SELECT * FROM lichess_db_puzzle ORDER BY Popularity DESC, Rating DESC, NbPlays DESC")
    headers = [description[0] for description in cursor_input.description]

    for row in cursor_input:
        row_dict = dict(zip(headers, row))
        if 'castling' in row_dict['Themes']:
            fen = row_dict["FEN"]
            moves = ' '.join(row_dict["Moves"].split())
            print(f"\"'{fen}', '{moves}'\",")
        ratings.append(int(row_dict['Rating']))
        popularity.append(int(row_dict['Popularity']))
        plays.append(int(row_dict['NbPlays']))

    plt.figure(figsize=(10, 6))
    plt.hist(ratings, bins=50, alpha=0.75, edgecolor='black')
    plt.title('Rating Distribution of Chess Puzzles')
    plt.xlabel('Rating')
    plt.ylabel('Frequency')
    plt.show()
    #
    # plt.figure(figsize=(10, 6))
    # plt.hist(popularity, bins=50, alpha=0.75, edgecolor='black')
    # plt.title('Popularity Distribution of Chess Puzzles')
    # plt.xlabel('Popularity')
    # plt.ylabel('Frequency')
    # plt.show()

    # plt.figure(figsize=(10, 6))
    # plt.hist(plays, bins=50, alpha=0.75, edgecolor='black')
    # plt.title('NbPlays Distribution of Chess Puzzles')
    # plt.xlabel('NbPlays')
    # plt.ylabel('Frequency')
    # plt.show()


plot_histogram("validated_puzzles.db")
