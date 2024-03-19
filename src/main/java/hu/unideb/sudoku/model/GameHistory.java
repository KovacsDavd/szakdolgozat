package hu.unideb.sudoku.model;

public class GameHistory {
    private CellPosition[][] originalBoard;
    private CellPosition[][] solvedBoard;
    private CellPosition[][] sudokuBoard;
    private long  elapsedTimeSeconds;

    public GameHistory(CellPosition[][] originalBoard, CellPosition[][] solvedBoard, CellPosition[][] sudokuBoard, long elapsedTimeSeconds) {
        this.originalBoard = originalBoard;
        this.solvedBoard = solvedBoard;
        this.elapsedTimeSeconds = elapsedTimeSeconds;
        this.sudokuBoard = sudokuBoard;
    }

    public GameHistory() {
    }

    public CellPosition[][] getOriginalBoard() {
        return originalBoard;
    }

    public void setOriginalBoard(CellPosition[][] originalBoard) {
        this.originalBoard = originalBoard;
    }

    public CellPosition[][] getSolvedBoard() {
        return solvedBoard;
    }

    public void setSolvedBoard(CellPosition[][] solvedBoard) {
        this.solvedBoard = solvedBoard;
    }

    public CellPosition[][] getSudokuBoard() {
        return sudokuBoard;
    }

    public void setSudokuBoard(CellPosition[][] sudokuBoard) {
        this.sudokuBoard = sudokuBoard;
    }

    public long getElapsedTimeSeconds() {
        return elapsedTimeSeconds;
    }

    public void setElapsedTimeSeconds(long elapsedTimeSeconds) {
        this.elapsedTimeSeconds = elapsedTimeSeconds;
    }

    public String getElapsedTimeFormatted() {
        long minutes = (elapsedTimeSeconds % 3600) / 60;
        long seconds = elapsedTimeSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}

