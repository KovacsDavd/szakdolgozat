package hu.unideb.sudoku.model;

import java.util.Random;

public class GameModel {
    private static GameDifficult difficult;
    private static final int SIZE = 9;
    private int[][] sudokuBoard;
    private static final Random rand = new Random();

    public GameModel() {
        generateSudoku();
    }

    public void generateSudoku() {
        sudokuBoard = new int[SIZE][SIZE];
        fillDiagonal();
        fillRemaining(0, 3);
        removeDigits();
    }

    private void fillDiagonal() {
        for (int i = 0; i < SIZE; i = i + 3)
            fillBox(i, i);
    }

    private boolean unUsedInBox(int rowStart, int colStart, int num) {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (sudokuBoard[rowStart + i][colStart + j] == num)
                    return false;
        return true;
    }

    private void fillBox(int row, int col) {
        int num;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                do {
                    num = randomGenerator(9);
                } while (!unUsedInBox(row, col, num));
                sudokuBoard[row + i][col + j] = num;
            }
    }

    private int randomGenerator(int num) {
        return rand.nextInt(num) + 1;
    }


    private boolean unUsedInRow(int[][] board, int row, int num) {
        for (int j = 0; j < SIZE; j++)
            if (board[row][j] == num)
                return false;
        return true;
    }


    private boolean fillRemaining(int i, int j) {
        if (i == SIZE - 1 && j == SIZE)
            return true;

        if (j == SIZE) {
            i++;
            j = 0;
        }

        if (sudokuBoard[i][j] != 0)
            return fillRemaining(i, j + 1);

        for (int num = 1; num <= SIZE; num++) {
            if (checkIfSafe(sudokuBoard, i, j, num)) {
                sudokuBoard[i][j] = num;
                if (fillRemaining(i, j + 1))
                    return true;
                sudokuBoard[i][j] = 0;
            }
        }
        return false;
    }

    private void removeDigits() {
        int count = 36;

        while (count != 0) {
            int i = randomGenerator(9) - 1;
            int j = randomGenerator(9) - 1;
            if (sudokuBoard[i][j] != 0) {
                count--;
                sudokuBoard[i][j] = 0;
            }
        }
    }

    public void solveSudoku() {
        int[][] copyBoard = new int[SIZE][SIZE];
        copyBoardValues(copyBoard, sudokuBoard);

        if (solve(copyBoard)) {
            copyBoardValues(sudokuBoard, copyBoard);
        }
    }

    private void copyBoardValues(int[][] destination, int[][] source) {
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(source[i], 0, destination[i], 0, SIZE);
        }
    }

    private boolean solve(int[][] board) {
        int[] nextEmpty = findEmptyLocation(board);

        if (nextEmpty.length == 0) {
            return true;
        }

        int row = nextEmpty[0];
        int col = nextEmpty[1];

        for (int num = 1; num <= SIZE; num++) {
            if (checkIfSafe(board, row, col, num)) {
                board[row][col] = num;

                if (solve(board)) {
                    return true;
                }

                board[row][col] = 0;
            }
        }

        return false;
    }

    private int[] findEmptyLocation(int[][] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {
                    return new int[]{row, col};
                }
            }
        }
        return new int[0];
    }

    private boolean checkIfSafe(int[][] board, int row, int col, int num) {
        return (unUsedInRow(board, row, num) &&
                unUsedInCol(board, col, num) &&
                unUsedInBox(board, row - row % 3, col - col % 3, num));
    }

    private boolean unUsedInCol(int[][] board, int col, int num) {
        for (int row = 0; row < SIZE; row++) {
            if (board[row][col] == num) {
                return false;
            }
        }
        return true;
    }

    private boolean unUsedInBox(int[][] board, int rowStart, int colStart, int num) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[rowStart + i][colStart + j] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    public int[][] getSudokuBoard() {
        return sudokuBoard;
    }

    public static GameDifficult getDifficult() {
        return difficult;
    }

    public static void setDifficult(GameDifficult dif) {
        difficult = dif;
    }
}
