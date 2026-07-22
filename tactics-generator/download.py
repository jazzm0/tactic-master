import os
import shutil

import requests
import zstandard as zstd


def download_and_unpack_csv(url, output_csv_path):
    # Download the compressed file
    response = requests.get(url, stream=True)
    response.raise_for_status()
    
    compressed_file_path = output_csv_path + '.zst'

    with open(compressed_file_path, 'wb') as compressed_file:
        shutil.copyfileobj(response.raw, compressed_file)

    # Guard against a truncated transfer. The zstd content checksum lives in the
    # final bytes of the frame, so a cut-off download would drop the checksum
    # too and still "decompress" cleanly into a partial file. Compare the bytes
    # we actually wrote against the advertised Content-Length to catch that.
    expected_size = response.headers.get('Content-Length')
    actual_size = os.path.getsize(compressed_file_path)
    if expected_size is not None and actual_size != int(expected_size):
        os.remove(compressed_file_path)
        raise IOError(
            f"Truncated download: got {actual_size} bytes, expected {expected_size}"
        )

    # Decompress the file. When the frame includes a content checksum (the low
    # 32 bits of an XXH64 digest, which the lichess dump ships with), the
    # decompressor verifies it and raises ZstdError on bit-corruption.
    try:
        with open(compressed_file_path, 'rb') as compressed_file, open(output_csv_path, 'wb') as output_file:
            dctx = zstd.ZstdDecompressor()
            dctx.copy_stream(compressed_file, output_file)
    except zstd.ZstdError:
        os.remove(output_csv_path)
        raise
    finally:
        os.remove(compressed_file_path)


# Example usage
url = 'https://database.lichess.org/lichess_db_puzzle.csv.zst'
output_csv_path = 'lichess_db_puzzle.csv'

download_and_unpack_csv(url, output_csv_path)
print("Download and decompression successful, checksum verified.")
