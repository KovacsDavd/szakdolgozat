package hu.unideb.sudoku.model;

import javafx.util.Pair;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GameModel {
    private static GameDifficult difficult;
    private static final int SIZE = 9;
    private static final int EASY_MOD_REVOME_DIGITS = 2;
    private static final int MEDIUM_MOD_REVOME_DIGITS = 43;
    private static final int HARD_MOD_REVOME_DIGITS = 49;
    private CellPosition[][] sudokuBoard;
    private final CellPosition[][] solvedBoard;
    private final CellPosition[][] originalBoard;
    private static final Random rand = new Random();
    private static boolean needHistoryLoad = false;

    public GameModel() {
        sudokuBoard = new CellPosition[SIZE][SIZE];
        solvedBoard = new CellPosition[SIZE][SIZE];
        originalBoard = new CellPosition[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                sudokuBoard[i][j] = new CellPosition();
            }
        }
        if (!needHistoryLoad) {
            generateSudoku();
        }
    }

    public void loadGameFromHistory(GameHistory history) {
        deepCopy(history.getOriginalBoard(), originalBoard);
        deepCopy(history.getSudokuBoard(), sudokuBoard);
        deepCopy(history.getSolvedBoard(), solvedBoard);
    }

    public static void setNeedHistoryLoad(boolean needHistoryLoad) {
        GameModel.needHistoryLoad = needHistoryLoad;
    }

    public static boolean isNeedHistoryLoad() {
        return needHistoryLoad;
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

        deepCopy(sudokuBoard, solvedBoard);

        if (difficult == GameDifficult.EASY) {
            removeDigits(EASY_MOD_REVOME_DIGITS);
        } else if (difficult == GameDifficult.MEDIUM) {
            removeDigits(MEDIUM_MOD_REVOME_DIGITS);
        } else {
            removeDigits(HARD_MOD_REVOME_DIGITS);
        }
        storePossibleValues();

        deepCopy(sudokuBoard, originalBoard);
    }

    public void deepCopy(CellPosition[][] source, CellPosition[][] destination) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                destination[i][j] = new CellPosition(source[i][j].getValue(), new HashSet<>(source[i][j].getPossibleValues()));
            }
        }
    }

    public void storePossibleValues() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (sudokuBoard[i][j].getValue() == 0) {
                    Set<Integer> possibleValues = getNewPossibleValues(i, j);
                    sudokuBoard[i][j].setPossibleValues(possibleValues);
                }
            }
        }
    }


    public void setValueAt(int row, int col, int value) {
        sudokuBoard[row][col].setValue(value);
    }

    public int getValueAt(int row, int col) {
        return sudokuBoard[row][col].getValue();
    }

    public void setPossibleValuesAt(int row, int col, Set<Integer> values) {
        sudokuBoard[row][col].setPossibleValues(values);
    }

    public Set<Integer> getPossibleValuesAt(int row, int col) {
        return sudokuBoard[row][col].getPossibleValues();
    }

    private Set<Integer> getNewPossibleValues(int row, int col) {
        boolean[] usedValues = new boolean[SIZE + 1];

        // Ellenőrizzi a sorban lévő értékeket
        for (int j = 0; j < SIZE; j++) {
            int value = sudokuBoard[row][j].getValue();
            usedValues[value] = true;
        }

        // Ellenőrizzi az oszlopban lévő értékeket
        for (int i = 0; i < SIZE; i++) {
            int value = sudokuBoard[i][col].getValue();
            usedValues[value] = true;
        }

        // Ellenőrizzi a 3x3-as blokkban lévő értékeket
        int boxStartRow = row - row % 3;
        int boxStartCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int value = sudokuBoard[boxStartRow + i][boxStartCol + j].getValue();
                usedValues[value] = true;
            }
        }

        Set<Integer> possibleValues = new HashSet<>();
        for (int num = 1; num <= SIZE; num++) {
            if (!usedValues[num]) {
                possibleValues.add(num);
            }
        }

        return possibleValues;
    }

    public boolean isValueValid(int row, int col, int value) {
        // Ellenőrizzük a sort
        for (int j = 0; j < 9; j++) {
            if (j != col && sudokuBoard[row][j].getValue() == value) {
                return false;
            }
        }
        // Ellenőrizzük az oszlopot
        for (int i = 0; i < 9; i++) {
            if (i != row && sudokuBoard[i][col].getValue() == value) {
                return false;
            }
        }
        // Ellenőrizzük a 3x3-as dobozt
        int boxRowStart = row - row % 3;
        int boxColStart = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (boxRowStart + i != row || boxColStart + j != col) {
                    if (sudokuBoard[boxRowStart + i][boxColStart + j].getValue() == value) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void fillDiagonal() {
        for (int i = 0; i < SIZE; i = i + 3)
            fillBox(i, i);
    }

    private void fillBox(int row, int col) {
        int num;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                do {
                    num = randomGenerator();
                } while (!unUsedInBox(sudokuBoard, row, col, num));

                sudokuBoard[row + i][col + j].setValue(num);
            }
    }

    private int randomGenerator() {
        return rand.nextInt(9) + 1;
    }

    private boolean unUsedInRow(CellPosition[][] board, int row, int num) {
        for (int j = 0; j < SIZE; j++)
            if (board[row][j].getValue() == num) return false;
        return true;
    }

    private boolean fillRemaining(int i, int j) {
        if (i == SIZE - 1 && j == SIZE) return true;

        if (j == SIZE) {
            i++;
            j = 0;
        }

        if (sudokuBoard[i][j].getValue() != 0) return fillRemaining(i, j + 1);

        for (int num = 1; num <= SIZE; num++) {
            if (checkIfSafe(sudokuBoard, i, j, num)) {
                sudokuBoard[i][j].setValue(num);
                if (fillRemaining(i, j + 1)) return true;
                sudokuBoard[i][j].setValue(0);
            }
        }
        return false;
    }

    private void removeDigits(int count) {
        Set<Pair<Integer, Integer>> cellsToRemove = new HashSet<>();
        while (cellsToRemove.size() < count) {
            int i = rand.nextInt(SIZE);
            int j = rand.nextInt(SIZE);
            if (sudokuBoard[i][j].getValue() != 0) {
                int backup = sudokuBoard[i][j].getValue();
                sudokuBoard[i][j].setValue(0);

                // Ellenőrizze az egyediséget
                if (!hasUniqueSolution()) {
                    // Ha nincs egyedi megoldás, visszaállítja a számot
                    sudokuBoard[i][j].setValue(backup);
                } else {
                    // Sikeres eltávolítás, hozzáadjuk a sethez
                    cellsToRemove.add(new Pair<>(i, j));
                }
            }
        }
    }

    public boolean hasUniqueSolution() {
        int[] numberOfSolutions = new int[1]; // Egy tömb, hogy referencia típusként viselkedjen
        solveSudoku(0, 0, numberOfSolutions);
        return numberOfSolutions[0] == 1;
    }

    private boolean solveSudoku(int row, int col, int[] numberOfSolutions) {
        if (row == SIZE) {
            numberOfSolutions[0]++; // Egy lehetséges megoldás megtalálva
            return numberOfSolutions[0] == 1; // Csak akkor tér vissza igaz értékkel, ha ez az első megoldás
        }

        int nextRow = (col == SIZE - 1) ? row + 1 : row;
        int nextCol = (col == SIZE - 1) ? 0 : col + 1;

        if (sudokuBoard[row][col].getValue() != 0) {
            // Ugrás a következő cellára, ha ez már ki van töltve
            return solveSudoku(nextRow, nextCol, numberOfSolutions);
        } else {
            for (int num = 1; num <= SIZE; num++) {
                if (isValueValid(row, col, num)) {
                    sudokuBoard[row][col].setValue(num);
                    if (solveSudoku(nextRow, nextCol, numberOfSolutions)) {
                        if (numberOfSolutions[0] > 1) {
                            return false; // Már több mint egy megoldás van, nem kell többet keresni
                        }
                    }
                    sudokuBoard[row][col].setValue(0); // Visszavonás (backtrack)
                }
            }
        }
        return numberOfSolutions[0] == 1;
    }

    private boolean checkIfSafe(CellPosition[][] board, int row, int col, int num) {
        return (unUsedInRow(board, row, num) && unUsedInCol(board, col, num) && unUsedInBox(board, row - row % 3, col - col % 3, num));
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

    public void solve() {
        deepCopy(solvedBoard, sudokuBoard);
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

    public CellPosition[][] getSolvedBoard() {
        return solvedBoard;
    }

    public CellPosition[][] getOriginalBoard() {
        return originalBoard;
    }

    public void resetBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                sudokuBoard[i][j] = new CellPosition(originalBoard[i][j].getValue(), new HashSet<>(originalBoard[i][j].getPossibleValues()));
            }
        }
    }

    public boolean isComplete() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int value = sudokuBoard[i][j].getValue();
                if (value == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isCorrect() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int value = sudokuBoard[i][j].getValue();
                int correctValue = solvedBoard[i][j].getValue();
                if (value != correctValue) {
                    return false;
                }
            }
        }
        return true;
    }

    public Set<Pair<Integer, Pair<Integer, Integer>>> checkFullHouse() {
        Set<Pair<Integer, Pair<Integer, Integer>>> results = new HashSet<>();

        // Sorok vizsgálata
        results.addAll(checkFullHouseByRowORCol(results, true));

        // Oszlopok vizsgálata
        results.addAll(checkFullHouseByRowORCol(results, false));

        // 3x3-as blokkok vizsgálata
        results.addAll(checkFullHouseByBoxes(results));

        System.out.println("FULL HOUSE: " + results.size());
        return results;
    }

    private Set<Pair<Integer, Pair<Integer, Integer>>> checkFullHouseByRowORCol(Set<Pair<Integer, Pair<Integer, Integer>>> results, boolean isRowCheck) {
        for (int i = 0; i < SIZE; i++) {
            int emptyCount = 0;
            int lastEmptyIndex = -1;
            for (int j = 0; j < SIZE; j++) {
                int row = isRowCheck ? i : j;
                int col = isRowCheck ? j : i;
                if (sudokuBoard[row][col].getValue() == 0) {
                    emptyCount++;
                    lastEmptyIndex = j;
                }
            }
            if (emptyCount == 1) {
                int targetRow = isRowCheck ? i : lastEmptyIndex;
                int targetCol = isRowCheck ? lastEmptyIndex : i;
                Set<Integer> possibleValues = getPossibleValuesAt(targetRow, targetCol);
                if (possibleValues.size() == 1) {
                    int value = possibleValues.iterator().next();
                    results.add(new Pair<>(value, new Pair<>(targetRow, targetCol)));
                }
            }
        }
        return results;
    }

    private Set<Pair<Integer, Pair<Integer, Integer>>> checkFullHouseByBoxes(Set<Pair<Integer, Pair<Integer, Integer>>> results) {
        for (int boxRow = 0; boxRow < SIZE; boxRow += 3) {
            for (int boxCol = 0; boxCol < SIZE; boxCol += 3) {
                int emptyCount = 0;
                Pair<Integer, Integer> lastEmptyCell = null;
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (sudokuBoard[boxRow + i][boxCol + j].getValue() == 0) {
                            emptyCount++;
                            lastEmptyCell = new Pair<>(boxRow + i, boxCol + j);
                        }
                    }
                }
                if (emptyCount == 1 && lastEmptyCell != null) {
                    Set<Integer> possibleValues = getPossibleValuesAt(lastEmptyCell.getKey(), lastEmptyCell.getValue());
                    if (possibleValues.size() == 1) {
                        int value = possibleValues.iterator().next();
                        results.add(new Pair<>(value, lastEmptyCell));
                    }
                }
            }
        }
        return results;
    }

    public Set<Pair<Integer, Pair<Integer, Integer>>> checkNakedSingles() {
        Set<Pair<Integer, Pair<Integer, Integer>>> results = new HashSet<>();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (sudokuBoard[row][col].getValue() == 0) {
                    Set<Integer> possibleValues = sudokuBoard[row][col].getPossibleValues();

                    if (possibleValues.size() == 1) {
                        int value = possibleValues.iterator().next();
                        results.add(new Pair<>(value, new Pair<>(row, col)));
                    }
                }
            }
        }
        System.out.println("\n NAKED SINGLE: " + results.size());
        return results;
    }

    public Set<Pair<Integer, Pair<Integer, Integer>>> checkHiddenSingles() {
        Set<Pair<Integer, Pair<Integer, Integer>>> results = new HashSet<>();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                CellPosition cell = sudokuBoard[row][col];
                if (cell.getValue() == 0) {
                    for (int value : cell.getPossibleValues()) {
                        if (isHiddenSingleCell(row, col, value)) {
                            results.add(new Pair<>(value, new Pair<>(row, col)));
                        }
                    }
                }
            }
        }
        System.out.println("\n HIDDEN SINGLE: " + results.size());
        return results;
    }

    private boolean isHiddenSingleCell(int row, int col, int value) {
        return !isValueInRow(row, col, value) && !isValueInCol(row, col, value) && !isValueInBox(row, col, value);
    }

    private boolean isValueInRow(int row, int col, int value) {
        for (int c = 0; c < SIZE; c++) {
            if (c != col && sudokuBoard[row][c].getPossibleValues().contains(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValueInCol(int row, int col, int value) {
        for (int r = 0; r < SIZE; r++) {
            if (r != row && sudokuBoard[r][col].getPossibleValues().contains(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValueInBox(int row, int col, int value) {
        int boxRow = row - row % 3;
        int boxCol = col - col % 3;

        for (int r = boxRow; r < boxRow + 3; r++) {
            for (int c = boxCol; c < boxCol + 3; c++) {
                if ((r != row || c != col) && sudokuBoard[r][c].getPossibleValues().contains(value)) {
                    return true;
                }
            }
        }
        return false;
    }

}