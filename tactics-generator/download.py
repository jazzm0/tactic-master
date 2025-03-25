import hashlib
import os
import shutil

import requests
import zstandard as zstd


def download_and_unpack_csv(url, output_csv_path):
    # Download the compressed file
    response = requests.get(url, stream=True)
    compressed_file_path = output_csv_path + '.zst'

    with open(compressed_file_path, 'wb') as compressed_file:
        shutil.copyfileobj(response.raw, compressed_file)

    # Decompress the file
    with open(compressed_file_path, 'rb') as compressed_file, open(output_csv_path, 'wb') as output_file:
        dctx = zstd.ZstdDecompressor()
        dctx.copy_stream(compressed_file, output_file)

    # Remove the compressed file
    os.remove(compressed_file_path)


def sha256_checksum(file_path):
    sha256 = hashlib.sha256()
    with open(file_path, 'rb') as f:
        for chunk in iter(lambda: f.read(4096), b''):
            sha256.update(chunk)
    return sha256.hexdigest()


def validate_checksum(file_path):
    actual_checksum = sha256_checksum(file_path)
    return actual_checksum == "e20022cae7fed8645b68c1d817c4f5e531f761fd0f3a144cc1a99aed5f2c523e"


# Example usage
url = 'https://database.lichess.org/lichess_db_puzzle.csv.zst'
output_csv_path = 'lichess_db_puzzle.csv'

download_and_unpack_csv(url, output_csv_path)

if validate_checksum(output_csv_path):
    print("Checksum is valid.")
else:
    print("Checksum is invalid.")
