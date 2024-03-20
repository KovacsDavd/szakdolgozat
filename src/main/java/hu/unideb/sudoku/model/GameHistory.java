package hu.unideb.sudoku.model;

public class GameHistory {
    private final CellPosition[][] originalBoard;
    private final CellPosition[][] solvedBoard;
    private final CellPosition[][] sudokuBoard;
    private final long elapsedTimeSeconds;

    private final String difficulty;

    public GameHistory(CellPosition[][] originalBoard, CellPosition[][] solvedBoard, CellPosition[][] sudokuBoard, long elapsedTimeSeconds, String difficulty) {
        this.originalBoard = originalBoard;
        this.solvedBoard = solvedBoard;
        this.elapsedTimeSeconds = elapsedTimeSeconds;
        this.sudokuBoard = sudokuBoard;
        this.difficulty = difficulty;
    }


    public CellPosition[][] getOriginalBoard() {
        return originalBoard;
    }


    public CellPosition[][] getSolvedBoard() {
        return solvedBoard;
    }

    public CellPosition[][] getSudokuBoard() {
        return sudokuBoard;
    }

    public String getElapsedTimeFormatted() {
        long minutes = (elapsedTimeSeconds % 3600) / 60;
        long seconds = elapsedTimeSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public String getDifficulty() {
        return difficulty;
    }
}

