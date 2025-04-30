package com.tacticmaster.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static java.lang.Character.toLowerCase;

import com.tacticmaster.puzzle.Puzzle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashSet;
import java.util.Set;

public class ChessboardTest {

    private Chessboard chessboard;

    @BeforeEach
    public void setUp() {
        String fen = "1rb2rk1/q5P1/4p2p/3p3p/3P1P2/2P5/2QK3P/3R2R1 b - - 0 29";
        String moves = "f8f7 c2h7 g8h7 g7g8q";
        Puzzle puzzle = new Puzzle("1", fen, moves, 1049);
        chessboard = new Chessboard(puzzle);
    }

    @Test
    public void testSetupBoard() {
        char[][] board = chessboard.getBoard();
        assertEquals(' ', board[0][0]);
        assertEquals('r', board[0][1]);
        assertEquals('b', board[0][2]);
        assertEquals(' ', board[0][3]);
        assertEquals(' ', board[0][4]);
        assertEquals('r', board[0][5]);
        assertEquals('k', board[0][6]);
        assertEquals(' ', board[0][7]);

        assertEquals('q', board[1][0]);
        assertEquals(' ', board[1][1]);
        assertEquals(' ', board[1][2]);
        assertEquals(' ', board[1][3]);
        assertEquals(' ', board[1][4]);
        assertEquals(' ', board[1][5]);
        assertEquals('P', board[1][6]);
        assertEquals(' ', board[1][7]);

        assertEquals(' ', board[2][0]);
        assertEquals(' ', board[2][1]);
        assertEquals(' ', board[2][2]);
        assertEquals(' ', board[2][3]);
        assertEquals('p', board[2][4]);
        assertEquals(' ', board[2][5]);
        assertEquals(' ', board[2][6]);
        assertEquals('p', board[2][7]);

        assertEquals(' ', board[3][0]);
        assertEquals(' ', board[3][1]);
        assertEquals(' ', board[3][2]);
        assertEquals('p', board[3][3]);
        assertEquals(' ', board[3][4]);
        assertEquals(' ', board[3][5]);
        assertEquals(' ', board[3][6]);
        assertEquals('p', board[3][7]);

        assertEquals(' ', board[4][0]);
        assertEquals(' ', board[4][1]);
        assertEquals(' ', board[4][2]);
        assertEquals('P', board[4][3]);
        assertEquals(' ', board[4][4]);
        assertEquals('P', board[4][5]);
        assertEquals(' ', board[4][6]);
        assertEquals(' ', board[4][7]);

        assertEquals(' ', board[5][0]);
        assertEquals(' ', board[5][1]);
        assertEquals('P', board[5][2]);
        assertEquals(' ', board[5][3]);
        assertEquals(' ', board[5][4]);
        assertEquals(' ', board[5][5]);
        assertEquals(' ', board[5][6]);
        assertEquals(' ', board[5][7]);

        assertEquals(' ', board[6][0]);
        assertEquals(' ', board[6][1]);
        assertEquals('Q', board[6][2]);
        assertEquals('K', board[6][3]);
        assertEquals(' ', board[6][4]);
        assertEquals(' ', board[6][5]);
        assertEquals(' ', board[6][6]);
        assertEquals('P', board[6][7]);

        assertEquals(' ', board[7][0]);
        assertEquals(' ', board[7][1]);
        assertEquals(' ', board[7][2]);
        assertEquals('R', board[7][3]);
        assertEquals(' ', board[7][4]);
        assertEquals(' ', board[7][5]);
        assertEquals('R', board[7][6]);
        assertEquals(' ', board[7][7]);
    }

    @Test
    public void testIsWhiteToMove() {
        assertTrue(chessboard.isWhiteToMove());
    }

    @Test
    public void testIsCorrectMove() {
        assertTrue(chessboard.isCorrectMove(0, 5, 1, 5));
        assertFalse(chessboard.isCorrectMove(0, 6, 1, 5));
    }

    @Test
    public void testMakeFirstMove() {
        chessboard.makeFirstMove();
        assertTrue(chessboard.isFirstMoveDone());
        assertEquals('r', chessboard.getBoard()[1][5]);
    }

    @Test
    public void testMakeNextMove() {
        chessboard.makeFirstMove();
        chessboard.makeNextMove();
        assertEquals('Q', chessboard.getBoard()[1][7]);
    }

    @Test
    public void testMovePiece() {
        assertTrue(chessboard.movePiece(0, 1, 1, 1));
        assertEquals('r', chessboard.getBoard()[1][1]);
        assertEquals(' ', chessboard.getBoard()[0][1]);
    }

    @Test
    public void testSolved() {
        chessboard.makeFirstMove();
        chessboard.makeNextMove();
        chessboard.makeNextMove();
        chessboard.makeNextMove();
        chessboard.makeNextMove();
        assertTrue(chessboard.solved());
    }

    @Test
    public void testPromotion() {
        chessboard.makeFirstMove();
        chessboard.makeNextMove();
        chessboard.makeNextMove();
        chessboard.makeNextMove();
        chessboard.makeNextMove();
        assertTrue(chessboard.solved());
    }

    @Test
    public void testMultiplePromotionsInOneBoard() {
        String fen = "8/P1P1P1P1/8/5k2/2K5/8/1p1p1p1p/8 w - - 0 1";
        String moves = "a7a8q b2b1q c7c8r d2d1r e7e8b f2f1b g7g8n h2h1n";
        Puzzle puzzle = new Puzzle("1", fen, moves, 1049);
        Chessboard chessboard = new Chessboard(puzzle);

        chessboard.makeFirstMove();
        assertEquals('Q', chessboard.getBoard()[7][0]);
        chessboard.makeNextMove();
        assertEquals('q', chessboard.getBoard()[0][1]);

        chessboard.makeNextMove();
        assertEquals('R', chessboard.getBoard()[7][2]);
        chessboard.makeNextMove();
        assertEquals('r', chessboard.getBoard()[0][3]);

        chessboard.makeNextMove();
        assertEquals('B', chessboard.getBoard()[7][4]);
        chessboard.makeNextMove();
        assertEquals('b', chessboard.getBoard()[0][5]);

        chessboard.makeNextMove();
        assertEquals('N', chessboard.getBoard()[7][6]);
        chessboard.makeNextMove();
        assertEquals('n', chessboard.getBoard()[0][7]);
    }

    @Test
    public void testEnPassant() {
        String fen = "4k3/2p5/8/3P4/8/8/8/4K3 w - - 0 1";
        String move = "c7c5 d5c6";
        Puzzle puzzle = new Puzzle("1", fen, move, 1049);
        Chessboard chessboard = new Chessboard(puzzle);
        chessboard.makeFirstMove();
        assertEquals('p', chessboard.getBoard()[4][2]);
        chessboard.makeNextMove();
        assertEquals(' ', chessboard.getBoard()[4][3]);
        assertEquals('P', chessboard.getBoard()[5][2]);

        fen = "1k6/p7/8/1P6/8/8/8/1K6 b - - 0 1";
        move = "a7a5 b5a6";
        puzzle = new Puzzle("1", fen, move, 1049);
        chessboard = new Chessboard(puzzle);
        chessboard.makeFirstMove();
        chessboard.makeNextMove();
        assertEquals(' ', chessboard.getBoard()[3][0]);
        assertEquals('P', chessboard.getBoard()[2][0]);

    }

    @ParameterizedTest
    @CsvSource({
            "'r3k1r1/p1pb1p2/1pn1q2p/1B2p3/4Pp2/P1P2N1P/2P1QPPK/1R3R2 w q - 0 18', 'b1d1 g8g2 h2g2 e6h3 g2g1 e8c8'",
            "'r3k2r/8/8/8/8/8/8/R3K2R b - - 0 1', 'e8g8 e1c1'",
            "'r4rk1/ppp2ppp/2n3b1/6q1/2B5/2Q2PP1/PP2N2P/R3K2R b KQ - 2 18', 'c6e5 f3f4 e5d3 c4d3 g5d5 e1c1'",
            "'r3k2r/ppp2ppp/2n4n/1B3b2/8/1PP1PN1P/P4PP1/RN1K3R w kq - 3 13', 'f3e5 e8c8 d1e1 c6e5'",
            "'r1bqk2r/pp3p2/2p1pn1p/4P1p1/1bB1P3/2N2N2/PP3PPP/R2QK2R b KQkq - 0 11', 'f6e4 d1d8 e8d8 e1c1 d8c7 c3e4'",
            "'r3kb1r/pp3ppp/2n2n2/2p1P3/8/2P2b2/PP2BPPP/RNBR2K1 w kq - 0 10', 'e5f6 f3e2 f6g7 f8g7 d1e1 e8c8'",
            "'rn1qk2r/pp3ppp/5pb1/2b4P/3p4/6N1/PPP1PPP1/R2QKBNR b KQkq - 0 9', 'c5b4 c2c3 d4c3 d1d8 e8d8 e1c1'",
            "'Qnb1k2r/p1p1bppp/1p3n2/4p3/2Bq4/8/PPP2PPP/RNB1K2R w KQk - 0 9', 'e1g1 d4c4 a8b8 e8g8'",
            "'r3nrk1/b2b2pp/p1p2q2/1pPN1p2/1P1Qp2P/P3P1P1/1BP2PB1/2R1K2R b K - 0 19', 'c6d5 d4d5 d7e6 d5a8 f6b2 e1g1'",
            "'r3k1nr/pp2qp1p/2B4Q/4b3/8/8/PPb2PPP/RNB1K2R b KQkq - 0 12', 'b7c6 h6c6 e8f8 c6a8 f8g7 e1g1 c2e4 a8c8'",
            "'rnb1k1nr/1pp1bppp/p7/3P4/3P1B1N/2N5/PPP1Q2P/R3KB1q b Qkq - 1 10', 'c8h3 e1c1 h3f1 d1f1 h1f1 e2f1'",
            "'r2qkbnr/pp3ppp/2n1b3/1B2p3/4p3/2P2N2/PP3PPP/RNBQK2R w KQkq - 0 8', 'f3e5 d8d1 e1d1 e8c8'",
            "'1r3rk1/2p2pp1/2p2p1p/3q4/1R1P4/P1Q1PN2/1p3PPP/4K2R b K - 1 22', 'b8b4 c3b4 d5a2 e1g1'",
            "'r1b1k2r/ppp2pp1/2p5/2P4p/4q1B1/2P3P1/PP1K1P1P/RNBQ4 w kq - 0 13', 'g4c8 e8g8 d1e2 f8d8 d2e1 e4h1'",
            "'rnbq2kr/ppp3pp/1b3n2/4P3/3PP3/2P5/PP4PP/RNBQK2R b KQ - 0 9', 'f6e4 d1b3 g8f8 e1g1'",
            "'r3kb1r/ppp1pppp/2n2n2/8/Q7/5NPq/PP1PPP1P/RNB2RK1 w kq - 0 10', 'f3e5 e8c8 e5c6 f6g4 a4g4 h3g4'",
            "'r2qk2r/pp1n1pp1/4p2p/1Qb5/7P/2B2N2/PPP2PP1/R3K2R b KQkq - 3 16', 'e8g8 e1c1 d8b6 b5d7'",
            "'r1b1r1k1/pp1nnppp/4p3/3pP3/5P2/1NPBQ2P/Pq4P1/RN2K2R b KQ - 0 15', 'a7a5 e1g1 a5a4 f1f2'",
            "'r1b1k2r/ppp2ppp/2p2q2/8/4n3/3P1K1P/PPP1Q1P1/RNB2B1R w kq - 4 10', 'f3e4 e8g8 e4e3 f8e8 e3d2 e8e2'",
            "'2r1k2r/p3bppp/2nB4/1p1p1q2/Q7/7P/PPPN1PP1/2KRR3 w k - 0 18', 'a4b5 e8g8 d2c4 e7d6 c4d6 f5f4 c1b1 f4d6'",
            "'r2qk2r/pR2ppbp/2b3p1/2P5/4pB2/4P3/P1PQ1PPP/4KB1R w Kkq - 1 13', 'f1b5 d8d2 e1d2 e8c8'",
            "'rnb2rk1/pp3p1B/4pp2/8/1b1N4/2N1P3/PP3PqP/R2QK2R b KQ - 0 11', 'g8h7 d1h5 h7g7 e1c1 f8h8 h5b5'",
            "'r3k2r/pp1n1pp1/2pbpn1p/7q/4N1b1/P2PBN1P/1PPQBPP1/R3K2R b KQkq - 1 12', 'f6e4 d3e4 d6c5 e1c1 c5e3 f2e3 e8c8 h3g4'",
            "'r3k1nr/pp3ppp/8/8/Qb2q1b1/2N1B3/PP3PPP/R3KB1R b KQkq - 4 12', 'g4d7 a4d7 e8d7 e1c1 d7c7 c3e4'",
            "'r1bqk2r/ppp2pp1/5n1p/3N4/4P2B/8/PPB1NPPP/R3K2R b KQkq - 0 13', 'f6d5 h4d8 d5b4 e1c1'",
            "'r1b4r/1p1p1p1p/p3qkp1/2p1P3/P1P5/1PQ5/2P1N1PP/4K2R b K - 0 19', 'e6e5 e1g1 f6e6 e2f4 e5f4 f1f4'",
            "'r3k2r/p1p2ppp/2p5/4qb2/1b6/2B5/PP2NPPP/R2QK2R b KQkq - 3 15', 'a8d8 d1d8 e8d8 e1c1 d8c8 c3e5'",
            "'r2qk1nr/pp3ppp/2n1p3/3B4/8/2P2P2/P1P2P1P/R1BQR1K1 w kq - 1 13', 'c1a3 d8g5 g1f1 e8c8 d5c6 d8d1'",
            "'r3k2r/5pp1/2p1p3/4qPQ1/1R2PnPp/7P/PpP3B1/1K1R4 w kq - 0 26', 'b4b7 e8g8 g2f1 e5a5 f1c4 f4e2'",
            "'r3k2r/pp2bppp/3pbn2/q1p5/N4B2/5B2/PPP2PPP/R3RQK1 w kq - 4 16', 'f3b7 a5a4 b7a8 e8g8 a8e4 f6e4'",
            "'4k2r/ppQ2ppp/8/1q2P3/4P3/5P2/PP2K2P/6NR w k - 1 23', 'e2d1 b5f1 d1d2 e8g8 e5e6 f7e6'",
            "'Qn2k2r/p1pb1ppp/1q3n2/3P4/2B1p3/2N5/PP2N1PP/R3K2R w KQk - 4 15', 'd5d6 d7c6 c4b5 e8g8 b5c6 b8c6'",
            "'2r2b1r/1BNk1ppp/pP2p3/2p1n3/2P5/P7/6PP/R3K2R b KQ - 0 22', 'c8c7 e1c1 f8d6 d1d6'",
            "'r3k2r/1Rp3pp/p1b2p2/q3p3/8/B5Q1/P1P2PPP/5RK1 w kq - 1 18', 'g3g7 e8c8 a3b4 h8g8 b4a5 g8g7 b7c7 g7c7'",
            "'Q1b1k2r/p2pnpbp/4p1p1/4P3/2B2B2/8/PqP2PPP/R4RK1 w k - 1 14', 'a1b1 b2d4 c4a6 d4f4 a6c8 e8g8'",
            "'r1bq1rk1/ppp2n2/7p/2b3p1/2B5/3PB3/PPP1Q1PP/RN2K2R b KQ - 0 15', 'c5e3 e2e3 f8e8 c4f7 g8f7 e1g1'",
            "'r3k2r/ppqn1ppp/2pbpn2/4N3/2BP1BP1/8/PPP1QP1P/2KR3R w kq - 6 14', 'e5f7 d6f4 c1b1 e8g8'",
            "'r1bq1rk1/p1pn1ppp/2p5/3pP1b1/8/2NB2BP/PPP2PP1/R2QK2R b KQ - 4 12', 'd7e5 g3e5 f8e8 e1g1 e8e5 f2f4'",
            "'r1b1k2r/p4ppp/2p1p3/4P3/4q3/2Q3P1/PPP4P/3RKB1R w Kkq - 1 18', 'e1d2 e4h1 f1e2 h1g2 d2c1 e8g8'",
            "'r1bqk2r/pppp1pbp/2n2n2/3N4/2B1Pp2/1P3Q2/PBPP2PP/R3K2R b KQkq - 4 9', 'd7d6 d5f6 g7f6 f3f4 c6e5 e1g1'",
            "'r3k2r/pp1qbppp/2n2n2/3p4/3Q4/2N2N2/PPP2PPP/R1B1R1K1 w kq - 4 11', 'd4e3 d5d4 e3g5 d4c3 g5g7 e8c8'",
            "'r1bq1r1k/pp2p1bp/6p1/6B1/2BPp3/8/PPPQ1PP1/R3K2R b KQ - 0 14', 'g7d4 e1c1 d4b2 c1b2'",
            "'r3k2r/4pp1p/1p4p1/1q1Pp1P1/2p1P2P/P1Q5/K1P1B3/R6R w kq - 0 26', 'c3e5 a8a3 a2a3 e8g8 e5e7 f8a8 e7a7 a8a7'",
            "'rn1qr1k1/ppp2ppp/1b6/8/1PbP4/P1N3P1/3QNPBP/R3K2R b KQ - 2 13', 'b6d4 e1c1 c4e2 c3e2'",
            "'r3k2r/1b1p2p1/p4p2/1pq1p3/4P1p1/1B1Q2P1/PPP1N2P/R2R2K1 w kq - 0 18', 'g1h1 h8h2 h1h2 c5f2 h2h1 e8c8 d3d7 d8d7'",
            "'r2qk2r/ppp1bppp/2n1bn2/1B2p3/8/2N1Q3/PPPB1PPP/R3K1NR w KQkq - 0 9', 'e3e5 d8d2 e1d2 e8c8 d2c1 c6e5'",
            "'r6r/2p3pp/p2b4/n5B1/5Pk1/2P4P/P1P3P1/R3K2R b KQ - 0 22', 'g4g3 e1g1 d6c5 g1h1'",
            "'r3k2r/pp2b1pp/2pq4/2p3B1/Q3p1n1/2PP4/PP3PPP/RN2R1K1 w kq - 3 15', 'a4e4 d6h2 g1f1 e8g8 e4g4 e7g5'",
            "'1r1qk2r/3b1pb1/p1nPp1pn/3B2Np/1p3B2/N1P4P/PP2QPP1/2KR3R w k - 0 16', 'g5e6 f7e6 d5c6 e8g8 c6d7 d8d7 a3c4 f8f4'",
            "'r1b1k1nr/ppp1b2p/5p2/3p4/2BqN2B/3P4/PP2QPPP/R4RK1 w kq - 0 15', 'e4f6 g8f6 f1e1 e8g8 e2e7 d5c4'",
            "'r1b4r/pp2kp2/4p3/4q1pp/8/P4QPP/2P1B1P1/3RK2R b K - 5 20', 'c8d7 e1g1 f7f5 d1d7 e7d7 f3b7'",
            "'r3k2r/pRpq2pp/8/Q1b3B1/4n3/5N2/P4PPP/5RK1 w kq - 0 18', 'f1e1 c5f2 g1h1 e8g8'",
            "'r3k3/pbp2p2/3b3p/4pp1q/8/2NPQN1P/PPP2P2/4R1RK w q - 5 20', 'g1g3 e8c8 h1g2 f5f4 e3e2 f4g3'",
            "'r1b2rk1/pp4b1/1q1p1nQ1/4p1P1/3P4/2P1P3/PP1N2P1/R3K2R b KQ - 0 17', 'f6d5 g6h7 g8f7 e1g1'",
            "'r1b1k1r1/pp2Pp1p/2n3p1/4N2n/P1q5/2B5/2PQ1PPP/R3K2R b KQq - 4 18', 'c4e6 e1c1 e6e7 e5c6 b7c6 c3b4 e7c7 b4a5 c7f4 d2f4 h5f4 d1d8 e8e7 d8g8'",
            "'r3k2r/ppp2ppp/5n2/3qn3/3P2b1/2P5/P1P1Q1PP/R1B1KBNR w KQkq - 1 10', 'e2e3 e8c8 d4e5 h8e8 g1f3 g4f3'",
            "'r3kbQ1/pp1b1p2/1qn1p3/3pP3/3P4/P1N1PN2/1P5P/R3KB2 w Qq - 1 20', 'f3g5 e8c8 g5f7 b6b2 f7d8 b2c3'",
            "'r4rk1/pppb1p2/2nq4/3N4/3PP3/7P/PP3QP1/R3K2R b KQ - 0 18', 'c6b4 d5f6 g8g7 e1g1'",
            "'rn1qr1k1/pp3p2/2pb2pp/3p1n2/3P4/5N1Q/PPP1NPPP/R1B1K2R b KQ - 3 13', 'g8g7 g2g4 d8e7 e1g1 h6h5 g4f5'",
            "'Q1b1k2r/p2q1ppp/8/2p5/5n2/P3P3/1P3PPP/R3K2R w KQk - 0 17', 'e3f4 e8g8 e1g1 c8b7 a8a7 d7c6 f2f3 f8a8 a7a8 b7a8'",
            "'r3kb1r/pp4pp/2n5/q2BpbN1/8/8/PPP2PPP/R1BQK2R w KQkq - 1 12', 'c2c3 e8c8 d1f3 c6d4 f3f5 d4f5'",
            "'r1b1k2r/2ppbppp/p7/1p6/4Q1nq/1B6/PPPP1PPP/RNB1R1K1 w kq - 1 11', 'h2h3 h4f2 g1h1 e8g8'",
            "'r3kb1r/p2b1p1p/2p5/q3p2Q/1pB1P3/P1P5/1P4PP/R3K1NR b KQkq - 1 15', 'b4c3 h5f7 e8d8 e1c1'",
            "'r3k2r/ppp3pp/2p5/3b1p2/3qN3/3P1K1P/PPP1B1P1/R1BQ3R w kq - 0 12', 'c1e3 f5e4 f3f2 e8g8 e2f3 e4f3'",
            "'r3kb1r/pp2pppp/4b3/q1P5/3P4/1Rn2QB1/3N1PPP/4KB1R b Kkq - 2 15', 'a5a1 b3b1 c3b1 f1b5 e8d8 e1g1'",
            "'r3k2r/pp1q2pp/2n2n2/1B3p2/1b2p3/2NP3P/PPP2PP1/R1BQK2R w KQkq - 1 11', 'd3e4 d7d1 e1d1 e8c8 c1d2 b4c3 b2c3 f6e4 b5d3 e4f2'",
            "'r4rk1/2p1q3/p3p1pP/1p2B1p1/4Q3/2P5/PP3P2/R3K3 b Q - 3 24', 'e7f7 h6h7 f7h7 e1c1 a8d8 d1h1'",
            "'r2qk2r/ppp2pp1/2p5/2b4p/4n1b1/2PP1P1P/PP4P1/RNBQKB1R w KQkq - 0 9', 'd3e4 d8h4 e1d2 e8c8 d2c2 d8d1'",
            "'r2k2nr/1p3ppp/1pnQ4/5q2/8/4B1P1/PPP2P1P/R3K2R b KQ - 0 16', 'f5d7 d6f8 d7e8 e1c1'",
            "'4k2r/5pp1/p1b1p1p1/PpPp4/7q/1R3PN1/5PKP/R3Q3 w k - 7 29', 'g2f1 h4c4 f1g2 c4b3 e1e5 e8g8'",
            "'2rrn1k1/p3bppb/1q2p2B/2ppN2Q/3N4/2P1P2P/PP3P2/R3K1R1 b Q - 3 20', 'g7g6 e5g6 f7g6 g1g6 h7g6 h5g6 g8h8 e1c1'",
            "'r1b1k2r/pppp1ppp/2n5/4P3/1qB3Q1/4P3/PPPN2PP/R3K2R b KQkq - 2 10', 'b4b2 c4f7 e8f7 e1g1 f7e8 d2c4 c6e5 g4e4 d7d5 e4d5'",
            "'rnb2b1r/pp1p2pp/2p1k3/3nP1BQ/2qP1P2/8/PPP3PP/RN2K2R b KQ - 0 11', 'c4c2 e1g1 g7g6 f4f5 c2f5 h5h4'",
            "'r3kb1r/pp2pp1p/1P2b1p1/8/4Q3/8/P1B2PPP/qNB1K2R b Kkq - 0 15', 'a7b6 c2a4 e8d8 e1g1'",
            "'r2qk2r/pbp3bp/1pn1Bpp1/8/3P4/5N1P/PPPBQ1P1/R3K2R b KQkq - 0 15', 'c6d4 f3d4 d8d4 e1c1'",
            "'r4b1r/pp1kp1pp/1qp1p3/8/2B5/2N2Q2/PP3PPP/4K2R b K - 1 15', 'b6b2 e1g1 a8d8 f1b1 b2b1 c3b1'",
            "'1k1r1b1r/pp3pp1/2p5/1PPp4/Q2Pp1n1/2N1PqPp/P2B1P1P/R3KR2 b Q - 2 19', 'g4h2 b5c6 f3g2 e1c1'",
            "'rn2kb1r/3qpp1p/1p1P1np1/p7/8/B3PQ2/PP3PPP/2R1K2R b Kkq - 0 16', 'f8g7 f3a8 e8g8 d6e7 f8e8 e1g1'",
            "'r1bk1b1r/ppp1n3/2p1qBQ1/8/4P3/2N5/PPP2PPP/R3K2R b KQ - 2 13', 'h8g8 e1c1 c8d7 d1d7 d8d7 h1d1 e7d5 g6h7 d7c8 e4d5'",
            "'r1bqk2r/p4ppp/1p1bp2n/2pP4/2B1P3/5Q2/PPP3PP/RNB1R1K1 w kq - 0 12', 'd5e6 d6h2 g1f1 e8g8 e6e7 d8e7'",
            "'rn1qk2r/pQ3ppp/2b2n2/1B1p4/1b1P4/8/PP3PPP/RNB1K1NR w KQkq - 3 10', 'c1d2 e8g8 b5c6 b8c6'",
            "'r1b1k2r/pp1n1ppp/2p1p3/8/1P1Pn3/2N1PN2/BPQ2PPP/q1B1K2R b Kkq - 1 11', 'e4c3 b2c3 a7a5 b4b5 c6b5 e1g1 b5b4 c1d2'",
            "'r3r2k/1bqn2bp/pp4n1/5pP1/P2B4/1QP2N2/1P1NB3/R3K2R b KQ - 0 20', 'g6f4 d4g7 h8g7 h1h7 g7h7 b3f7 h7h8 e1c1'",
            "'r2qk1nr/1p3ppp/p7/5b2/2Bn4/P7/P4PPP/1RBQK1NR w Kkq - 2 14', 'b1b7 d4c2 e1e2 d8d1 e2d1 e8c8'",
            "'rnb1k2r/pp4pp/8/3QP3/4p3/6q1/PPP1BR2/R3K3 b Qkq - 0 17', 'h8f8 e2b5 b8d7 d5e6 e8d8 e1c1'",
            "'rn2k1r1/pp2p2p/2p3p1/3qP3/5Q2/4P2P/PPP2PP1/R3K2R b KQq - 0 18', 'd5g2 e1c1 b8d7 h1h2'",
            "'r1bqk2r/p1p2ppp/1p2p3/8/Qb1n1P2/2N2N2/PP3PPP/R3KB1R b KQkq - 1 10', 'c7c6 e1c1 b4c3 b2c3'",
            "'r1b1kb1r/pp3ppp/2q2n2/4N3/8/PQ2B3/1P3PPP/RN2K2R b KQkq - 0 14', 'c6g2 b3f7 e8d8 b1d2 f8d6 e1c1'",
            "'r3k2r/pp2nppp/3bq3/3N4/2Q5/4B3/PPP2PPP/R4RK1 w kq - 1 15', 'f1e1 e6d5 c4d5 e7d5 a1d1 e8c8 d1d5 d6h2 g1h2 d8d5'",
            "'rn2k2r/ppp2pp1/2q1b3/4QP2/4p1Pp/2P4P/PP6/RNB1K2R b KQkq - 0 15', 'e4e3 e1g1 b8d7 e5e3 e8g8 f5e6'",
            "'r1b1k2r/ppppqppp/2n5/3P4/8/5N2/P2BBPPP/Q3K2R b Kkq - 2 12', 'c6b4 a1g7 h8f8 e1g1'",
            "'3q1r2/3bppk1/5np1/r1pPQ3/2P5/2N4P/6B1/1R2K2R b K - 0 25', 'e7e6 e1g1 e6d5 c3d5'",
            "'r3k2r/pp4qp/2p3p1/2Pp4/4p1b1/P1N2N2/1PPQ1P2/2KRR3 w kq - 0 22', 'c3e4 e8g8 e4g5 g4f3 g5f3 f8f3'",
            "'r2qk2r/1p2b1p1/p2p4/2pPnp2/4B1bP/2N3P1/PPPB1Q2/2K1R2R w kq - 0 20', 'e4f5 e8g8 f5e6 g4e6'",
            "'r3k2r/pbqn1pp1/2N1p2p/1Bp5/P7/2P2Q2/2P2PPP/3R1RK1 w kq - 3 19', 'c3c4 a7a6 d1d7 c7d7 f1d1 d7c7 c6d8 a6b5 d8b7 e8g8'",
            "'r1bqkb1r/pppp1ppp/8/8/2BQ4/2P5/P1P2PPP/R1B1K2R b KQkq - 0 8', 'c7c5 c4f7 e8f7 d4d5 f7e8 e1g1 f8e7 f1e1 a7a5 c1g5'",
            "'r1bqk2r/ppp4p/6p1/3p4/6pb/2PBPPR1/PP4N1/RN1QK3 w Qkq - 1 17', 'g2h4 d8h4 d1a4 c8d7 a4f4 e8c8'",
            "'r3k2r/1b1R1ppp/p1pbp3/2p1N3/N3n3/8/PPP2PPP/R1B3K1 w kq - 4 13', 'd7f7 d6e5 f7b7 e8c8'",
            "'r4b2/pp1npkp1/2pq4/3p2P1/3PB3/8/PPP4r/RNBQK2R b KQ - 0 14', 'd5e4 e1g1 f7g8 c1f4'",
            "'r3k2r/p4ppp/1qp2n2/8/Q5b1/2N2N2/PP2KbPP/R1B2B1R w kq - 4 12', 'h2h3 e8c8 h3g4 h8e8'",
            "'r3k1nr/ppp3pp/2n2q2/5b2/1bPp4/1N3N2/PP1BPPPP/R2QKB1R w KQkq - 4 9', 'b3d4 c6d4 f3d4 b4d2 d1d2 e8c8 e2e3 c7c5'"
    })
    public void testCastling(String fen, String moves) {
        Puzzle puzzle = new Puzzle("1", fen, moves, 1049);
        Chessboard chessboard = new Chessboard(puzzle);

        char[] firstRowBefore = chessboard.getBoard()[0].clone();
        char[] lastRowBefore = chessboard.getBoard()[7].clone();

        chessboard.makeFirstMove();
        char[] firstRowAfter = chessboard.getBoard()[0];
        char[] lastRowAfter = chessboard.getBoard()[7];

        if (wasCastlingMove(firstRowBefore, firstRowAfter) || wasCastlingMove(lastRowBefore, lastRowAfter)) {
            assertTrue(validCastling(firstRowAfter) || validCastling(lastRowAfter));
            return;
        }

        for (int i = 0; i < moves.split(" ").length; i++) {
            firstRowBefore = chessboard.getBoard()[0].clone();
            lastRowBefore = chessboard.getBoard()[7].clone();
            chessboard.makeNextMove();
            firstRowAfter = chessboard.getBoard()[0];
            lastRowAfter = chessboard.getBoard()[7];

            if (wasCastlingMove(firstRowBefore, firstRowAfter) || wasCastlingMove(lastRowBefore, lastRowAfter)) {
                assertTrue(validCastling(firstRowAfter) || validCastling(lastRowAfter));
                break;
            }
        }
    }

    public boolean validCastling(char[] row) {
        return (toLowerCase(row[6]) == 'k' && toLowerCase(row[5]) == 'r') || (toLowerCase(row[2]) == 'k' && toLowerCase(row[3]) == 'r');
    }

    public boolean wasCastlingMove(char[] rowBefore, char[] rowAfter) {
        int changes = 0;
        Set<Character> usedPieces = new HashSet<>();
        for (int i = 0; i < rowBefore.length; i++) {
            if (rowBefore[i] != rowAfter[i]) {
                if (rowBefore[i] != ' ') {
                    usedPieces.add(toLowerCase(rowBefore[i]));
                }
                changes++;
            }
        }

        return changes == 4 && usedPieces.size() == 2 && usedPieces.contains('k') && usedPieces.contains('r');
    }
}