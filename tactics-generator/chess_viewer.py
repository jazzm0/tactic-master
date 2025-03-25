import csv
import io
import os

from path import csv_file_path

os.environ['DYLD_LIBRARY_PATH'] = '/opt/homebrew/Cellar/cairo/1.18.4/lib:' + os.environ.get('DYLD_LIBRARY_PATH', '')
import tkinter as tk
from tkinter import ttk

import cairosvg
import chess
import chess.svg
from PIL import Image, ImageTk

engine_path = "/opt/homebrew/Cellar/stockfish/17/bin/stockfish"


class TacticsApp:
    def __init__(self, root, file_path):
        self.root = root
        self.root.title("Tactics Viewer")
        self.file_path = file_path
        self.current_index = 0
        self.problems = self.load_problems()

        self.board_label = ttk.Label(root)
        self.board_label.pack()

        self.info_label = ttk.Label(root, text="", wraplength=400)
        self.info_label.pack()

        self.prev_button = ttk.Button(root, text="Previous", command=self.prev_problem)
        self.prev_button.pack(side=tk.LEFT)

        self.next_button = ttk.Button(root, text="Next", command=self.next_problem)
        self.next_button.pack(side=tk.RIGHT)

        self.display_problem(self.current_index)

    def load_problems(self):
        problems = []
        with open(self.file_path, newline='') as csvfile:
            reader = csv.DictReader(csvfile)
            for row in reader:
                problems.append(row)
        return problems

    def display_problem(self, index):
        problem = self.problems[index]
        fen = problem['FEN']
        board = chess.Board(fen)

        # Convert the board to an image
        svg_data = chess.svg.board(board=board)
        png_data = cairosvg.svg2png(bytestring=svg_data)
        image = Image.open(io.BytesIO(png_data))
        photo = ImageTk.PhotoImage(image)

        self.board_label.config(image=photo)
        self.board_label.image = photo

        info_text = f"Puzzle ID: {problem['PuzzleId']}\nRating: {problem['Rating']}\nThemes: {problem['Themes']}\nGame URL: {problem['GameUrl']}"
        self.info_label.config(text=info_text)

    def prev_problem(self):
        if self.current_index > 0:
            self.current_index -= 1
            self.display_problem(self.current_index)

    def next_problem(self):
        if self.current_index < len(self.problems) - 1:
            self.current_index += 1
            self.display_problem(self.current_index)

root = tk.Tk()
app = TacticsApp(root, csv_file_path)
root.mainloop()
