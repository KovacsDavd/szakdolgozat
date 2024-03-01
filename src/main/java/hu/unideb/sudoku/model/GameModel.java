package hu.unideb.sudoku.model;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GameModel {
    private static GameDifficult difficult;
    private static final int SIZE = 9;
    private static final int EASY_MOD_REVOME_DIGITS = 36;
    private static final int MEDIUM_MOD_REVOME_DIGITS = 43;
    private static final int HARD_MOD_REVOME_DIGITS = 49;
    private CellPosition[][] sudokuBoard;
    private static final Random rand = new Random();

    public GameModel() {
        sudokuBoard = new CellPosition[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                sudokuBoard[i][j] = new CellPosition();
            }
        }
        generateSudoku();
    }

    private void generateSudoku() {
        sudokuBoard = new CellPosition[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                sudokuBoard[i][j] = new CellPosition();
            }
        }

        fillDiagonal();
        fillRemaining(0, 3);

        if (difficult == GameDifficult.EASY) {
            removeDigits(EASY_MOD_REVOME_DIGITS);
        } else if (difficult == GameDifficult.MEDIUM) {
            removeDigits(MEDIUM_MOD_REVOME_DIGITS);
        } else {
            removeDigits(HARD_MOD_REVOME_DIGITS);
        }
        storePossibleValues();
        printCurrentBoardState();
    }

    public void storePossibleValues() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (sudokuBoard[i][j].getValue() == 0) {
                    Set<Integer> possibleValues = getPossibleValues(i, j);
                    sudokuBoard[i][j].setPossibleValues(possibleValues);
                }
            }
        }
    }

    public void setPlayerValue(int row, int col, int playerValue) {
        sudokuBoard[row][col].setPlayerValue(playerValue);
    }

    public int getPlayerValueAt(int row, int col) {
        return sudokuBoard[row][col].getPlayerValue();
    }


    public Set<Integer> getPossibleValues(int row, int col) {
        boolean[] usedValues = new boolean[SIZE + 1];

        // Ellenőrizze a sorban lévő értékeket
        for (int j = 0; j < SIZE; j++) {
            int value = sudokuBoard[row][j].getValue();
            usedValues[value] = true;
        }

        // Ellenőrizze az oszlopban lévő értékeket
        for (int i = 0; i < SIZE; i++) {
            int value = sudokuBoard[i][col].getValue();
            usedValues[value] = true;
        }

        // Ellenőrizze a 3x3-as blokkban lévő értékeket
        int boxStartRow = row - row % 3;
        int boxStartCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int value = sudokuBoard[boxStartRow + i][boxStartCol + j].getValue();
                usedValues[value] = true;
            }
        }

        // Gyűjtsd össze a lehetséges értékeket
        Set<Integer> possibleValues = new HashSet<>();
        for (int num = 1; num <= SIZE; num++) {
            if (!usedValues[num]) {
                possibleValues.add(num);
            }
        }

        return possibleValues;
    }

    private void fillDiagonal() {
        for (int i = 0; i < SIZE; i = i + 3)
            fillBox(i, i);
    }

    private boolean unUsedInBox(int rowStart, int colStart, int num) {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (sudokuBoard[rowStart + i][colStart + j].getValue() == num)
                    return false;
        return true;
    }

    private void fillBox(int row, int col) {
        int num;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                do {
                    num = randomGenerator();
                } while (!unUsedInBox(row, col, num));

                sudokuBoard[row + i][col + j].setValue(num);
            }
    }

    private int randomGenerator() {
        return rand.nextInt(9) + 1;
    }

    private boolean unUsedInRow(CellPosition[][] board, int row, int num) {
        for (int j = 0; j < SIZE; j++)
            if (board[row][j].getValue() == num)
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

        if (sudokuBoard[i][j].getValue() != 0)
            return fillRemaining(i, j + 1);

        for (int num = 1; num <= SIZE; num++) {
            if (checkIfSafe(sudokuBoard, i, j, num)) {
                sudokuBoard[i][j].setValue(num);
                if (fillRemaining(i, j + 1))
                    return true;
                sudokuBoard[i][j].setValue(0);
            }
        }
        return false;
    }

    private void removeDigits(int count) {
        while (count != 0) {
            int i = randomGenerator() - 1;
            int j = randomGenerator() - 1;
            if (sudokuBoard[i][j].getValue() != 0) {
                count--;
                sudokuBoard[i][j].setValue(0);
            }
        }
    }

    public void solveSudoku() {
        CellPosition[][] copyBoard = new CellPosition[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                copyBoard[i][j] = new CellPosition();
            }
        }

        copyBoardValues(copyBoard, sudokuBoard);

        if (solve(copyBoard)) {
            copyBoardValues(sudokuBoard, copyBoard);
        }
    }

    private void copyBoardValues(CellPosition[][] destination, CellPosition[][] source) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                destination[i][j].setValue(source[i][j].getValue());
            }
        }
    }

    private boolean solve(CellPosition[][] board) {
        int[] nextEmpty = findEmptyLocation(board);

        if (nextEmpty.length == 0) {
            return true;
        }

        int row = nextEmpty[0];
        int col = nextEmpty[1];

        for (int num = 1; num <= SIZE; num++) {
            if (checkIfSafe(board, row, col, num)) {
                board[row][col].setValue(num);

                if (solve(board)) {
                    return true;
                }

                board[row][col].setValue(0);
            }
        }

        return false;
    }

    private int[] findEmptyLocation(CellPosition[][] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col].getValue() == 0) {
                    return new int[]{row, col};
                }
            }
        }
        return new int[0];
    }

    private boolean checkIfSafe(CellPosition[][] board, int row, int col, int num) {
        return (unUsedInRow(board, row, num) &&
                unUsedInCol(board, col, num) &&
                unUsedInBox(board, row - row % 3, col - col % 3, num));
    }

    private boolean unUsedInCol(CellPosition[][] board, int col, int num) {
        for (int row = 0; row < SIZE; row++) {
            if (board[row][col].getValue() == num) {
                return false;
            }
        }
        return true;
    }

    private boolean unUsedInBox(CellPosition[][] board, int rowStart, int colStart, int num) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[rowStart + i][colStart + j].getValue() == num) {
                    return false;
                }
            }
        }
        return true;
    }

    public CellPosition[][] getSudokuBoardValues() {
        CellPosition[][] sudokuValues = new CellPosition[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                sudokuValues[i][j] = sudokuBoard[i][j];
            }
        }

        return sudokuValues;
    }

    public CellPosition[][] getSudokuBoard() {
        return sudokuBoard;
    }

    public static GameDifficult getDifficult() {
        return difficult;
    }

    public static void setDifficult(GameDifficult dif) {
        difficult = dif;
    }

    public void printCurrentBoardState() {
        System.out.println("Current Sudoku board state:");
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int value = sudokuBoard[i][j].getValue();
                Set<Integer> possibleValues = sudokuBoard[i][j].getPossibleValues();

                if (value == 0) {
                    System.out.println("[" + i + "][" + j + "] = Empty");
                    System.out.println("Possible Values: " + possibleValues);
                } else {
                    System.out.println("[" + i + "][" + j + "] = " + value);
                }
            }
        }
        System.out.println("End of current Sudoku board state");
    }
}