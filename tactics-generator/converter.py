import csv
import sqlite3

from path import csv_file_path


def csv_to_sqlite(csv_file_path, sqlite_db_path):
    conn = sqlite3.connect(sqlite_db_path)
    cursor = conn.cursor()
    count = 0
    with open(csv_file_path, newline='') as csvfile:
        reader = csv.reader(csvfile)
        headers = next(reader)

        column_types = {header: 'TEXT' for header in headers}
        for col in ['Rating', 'RatingDeviation', 'Popularity', 'NbPlays']:
            if col in column_types:
                column_types[col] = 'INTEGER'

        table_name = csv_file_path.split('/')[-1].split('.')[0]
        columns = ', '.join([f'"{header}" {column_types[header]}' for header in headers])
        cursor.execute(f'CREATE TABLE IF NOT EXISTS "{table_name}" ({columns})')
        max_rating = 0

        for row in reader:
            max_rating = max(max_rating, int(row[3]))
            count += 1
            placeholders = ', '.join(['?' for _ in headers])
            cursor.execute(f'INSERT INTO "{table_name}" VALUES ({placeholders})', row)
    conn.commit()
    conn.close()
    return count


total_count = csv_to_sqlite(csv_file_path, "all_puzzles.db")

print(total_count)
