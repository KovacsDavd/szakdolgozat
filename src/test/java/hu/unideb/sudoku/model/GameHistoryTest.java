package hu.unideb.sudoku.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GameHistoryTest {

    private final CellPosition[][] originalBoard = new CellPosition[1][1];
    private final CellPosition[][] solvedBoard = new CellPosition[1][1];
    private final long elapsedTimeSeconds = 123;
    private final String difficulty = "EASY";

    private GameHistory getGameHistoryEntity() {
        return new GameHistory(originalBoard, solvedBoard, elapsedTimeSeconds, difficulty);
    }

    @Test
    void testGgetOriginalBoard() {
        GameHistory gameHistory = getGameHistoryEntity();
        assertArrayEquals(originalBoard, gameHistory.getOriginalBoard());
    }

    @Test
    void testGetSolvedBoard() {
        GameHistory gameHistory = getGameHistoryEntity();
        assertEquals(solvedBoard, gameHistory.getSolvedBoard());
    }

    @Test
    void testGetElapsedTimeFormatted() {
        long minutes = (elapsedTimeSeconds % 3600) / 60;
        long seconds = elapsedTimeSeconds % 60;
        String formattedTime = String.format("%d:%02d", minutes, seconds);
        GameHistory gameHistory = getGameHistoryEntity();
        assertEquals(formattedTime, gameHistory.getElapsedTimeFormatted());
    }

    @Test
    void testGetDifficulty() {
        GameHistory gameHistory = getGameHistoryEntity();
        assertEquals(difficulty, gameHistory.getDifficulty());
    }
}