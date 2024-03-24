package hu.unideb.sudoku.model;

import javafx.util.Pair;
import org.tinylog.Logger;

import java.util.*;

public class GameModel {
    private static GameDifficult difficult;
    private static final int SIZE = 9;
    private static final int EASY_MOD_REVOME_DIGITS = 40;
    private static final int MEDIUM_MOD_REVOME_DIGITS = 48;
    private static final int HARD_MOD_REVOME_DIGITS = 52;
    private static final String LOG_FORMAT = "[{}][{}] = {}";
    private CellPosition[][] sudokuBoard;
    private final CellPosition[][] solvedBoard;
    private final CellPosition[][] originalBoard;
    private static final Random rand = new Random();
    private static boolean needHistoryLoad = false;
    private final Set<Pair<Pair<Integer, Integer>, Set<Integer>>> checkedPairSet = new HashSet<>();

    public void addCheckedPairSet(Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet) {
        for (Pair<Pair<Integer, Integer>, Set<Integer>> removeEntry : removeSet) {
            Pair<Integer, Integer> position = removeEntry.getKey();
            Set<Integer> valuesToRemove = removeEntry.getValue();

            Pair<Pair<Integer, Integer>, Set<Integer>> checkedEntry = new Pair<>(position, valuesToRemove);
            checkedPairSet.add(checkedEntry);
        }
    }

    public void setEmptyCheckedPairSet() {
        checkedPairSet.clear();
    }

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
        difficult = GameDifficult.valueOf(history.getDifficulty());
        deepCopy(history.getOriginalBoard(), originalBoard);
        deepCopy(history.getOriginalBoard(), sudokuBoard);
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

    private void storePossibleValues() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (sudokuBoard[i][j].getValue() == 0) {
                    Set<Integer> possibleValues = getNewPossibleValues(i, j);
                    sudokuBoard[i][j].setPossibleValues(possibleValues);
                }
            }
        }
    }

    public void storeActualPossibleValues() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (sudokuBoard[i][j].getValue() == 0) {
                    Set<Integer> newPossibleValues = getNewPossibleValues(i, j);
                    Set<Integer> currentPossibleValues = sudokuBoard[i][j].getPossibleValues();
                    currentPossibleValues.retainAll(newPossibleValues);
                    sudokuBoard[i][j].setPossibleValues(currentPossibleValues);
                }
            }
        }
    }

    //TODO: Segítségnél, ujraszámolásnál csak elvegyen
    // Lehetséges értékek reset (de ez elveszi a nakedpair-t is): letárolni honnan mit vett el, ezt ne rakja vissza
    public void resetPossibleValues() {
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

    public void removePossibleValuesAt(int row, int col, Set<Integer> valuesToRemove) {
        if (sudokuBoard[row][col] != null) {
            Set<Integer> possibleValues = new HashSet<>(sudokuBoard[row][col].getPossibleValues());
            possibleValues.removeAll(valuesToRemove);
            sudokuBoard[row][col].setPossibleValues(possibleValues);
        }
    }

    public void setPossibleValuesAt(int row, int col, Set<Integer> values) {
        sudokuBoard[row][col].setPossibleValues(values);
    }

    public Set<Integer> getPossibleValuesAt(int row, int col) {
        return sudokuBoard[row][col].getPossibleValues();
    }

    public Set<Integer> getNewPossibleValues(int row, int col) {
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

        Pair<Integer, Integer> currentPosition = new Pair<>(row, col);
        for (Pair<Pair<Integer, Integer>, Set<Integer>> checkedEntry : checkedPairSet) {
            if (checkedEntry.getKey().equals(currentPosition)) {
                possibleValues.removeAll(checkedEntry.getValue());
            }
        }

        return possibleValues;
    }

    public boolean isValueValid(int row, int col, int value) {
        return isValueUnusedInRow(row, col, value) && isValueUnusedInCol(row, col, value) && isValueUnusedInBox(row, col, value);
    }

    private boolean isValueUnusedInRow(int row, int col, int value) {
        for (int j = 0; j < 9; j++) {
            if (j != col && sudokuBoard[row][j].getValue() == value) {
                return false;
            }
        }
        return true;
    }

    private boolean isValueUnusedInCol(int row, int col, int value) {
        for (int i = 0; i < 9; i++) {
            if (i != row && sudokuBoard[i][col].getValue() == value) {
                return false;
            }
        }
        return true;
    }

    private boolean isValueUnusedInBox(int row, int col, int value) {
        int boxRowStart = row - row % 3;
        int boxColStart = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if ((boxRowStart + i != row || boxColStart + j != col) && sudokuBoard[boxRowStart + i][boxColStart + j].getValue() == value) {
                    return false;
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
                    if (solveSudoku(nextRow, nextCol, numberOfSolutions) && (numberOfSolutions[0] > 1)) {
                        return false; // Már több mint egy megoldás van, nem kell többet keresni

                    }
                    sudokuBoard[row][col].setValue(0); // BackTrack
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

        return results;
    }

    private Set<Pair<Integer, Pair<Integer, Integer>>> checkFullHouseByRowORCol(Set<Pair<Integer, Pair<Integer, Integer>>> results, boolean isRowCheck) {
        for (int i = 0; i < SIZE; i++) {
            Pair<Integer, Integer> emptyInfo = getEmptyInfo(i, isRowCheck);
            int emptyCount = emptyInfo.getKey();
            int lastEmptyIndex = emptyInfo.getValue();

            if (emptyCount == 1) {
                addResultIfValid(results, i, lastEmptyIndex, isRowCheck);
            }
        }
        return results;
    }

    private Pair<Integer, Integer> getEmptyInfo(int index, boolean isRowCheck) {
        int emptyCount = 0;
        int lastEmptyIndex = -1;
        for (int j = 0; j < SIZE; j++) {
            int row = isRowCheck ? index : j;
            int col = isRowCheck ? j : index;
            if (sudokuBoard[row][col].getValue() == 0) {
                emptyCount++;
                lastEmptyIndex = j;
            }
        }
        return new Pair<>(emptyCount, lastEmptyIndex);
    }

    private void addResultIfValid(Set<Pair<Integer, Pair<Integer, Integer>>> results, int index, int lastEmptyIndex, boolean isRowCheck) {
        int targetRow = isRowCheck ? index : lastEmptyIndex;
        int targetCol = isRowCheck ? lastEmptyIndex : index;
        Set<Integer> possibleValues = getPossibleValuesAt(targetRow, targetCol);

        if (possibleValues.size() == 1) {
            int value = possibleValues.iterator().next();
            results.add(new Pair<>(value, new Pair<>(targetRow, targetCol)));
            Logger.debug("FULL HOUSE: " + LOG_FORMAT, targetRow, targetCol, value);
        }
    }

    private Set<Pair<Integer, Pair<Integer, Integer>>> checkFullHouseByBoxes(Set<Pair<Integer, Pair<Integer, Integer>>> results) {
        for (int boxRow = 0; boxRow < SIZE; boxRow += 3) {
            for (int boxCol = 0; boxCol < SIZE; boxCol += 3) {
                Pair<Integer, Pair<Integer, Integer>> emptyCellInfo = findSingleEmptyCellInBox(boxRow, boxCol);
                int emptyCount = emptyCellInfo.getKey();
                Pair<Integer, Integer> lastEmptyCell = emptyCellInfo.getValue();

                if (emptyCount == 1) {
                    addResultForBoxIfValid(results, lastEmptyCell);
                }
            }
        }
        return results;
    }

    private Pair<Integer, Pair<Integer, Integer>> findSingleEmptyCellInBox(int boxRow, int boxCol) {
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
        return new Pair<>(emptyCount, lastEmptyCell);
    }

    private void addResultForBoxIfValid(Set<Pair<Integer, Pair<Integer, Integer>>> results, Pair<Integer, Integer> lastEmptyCell) {
        Set<Integer> possibleValues = getPossibleValuesAt(lastEmptyCell.getKey(), lastEmptyCell.getValue());
        if (possibleValues.size() == 1) {
            int value = possibleValues.iterator().next();
            results.add(new Pair<>(value, lastEmptyCell));
            Logger.debug("FULL HOUSE: " + LOG_FORMAT, lastEmptyCell.getKey(), lastEmptyCell.getValue(), value);
        }
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
                        Logger.debug("NAKED SINGLE: " + LOG_FORMAT, row, col, value);
                    }
                }
            }
        }
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
                            Logger.debug("HIDDEN SINGLE: " + LOG_FORMAT, row, col, value);
                        }
                    }
                }
            }
        }
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

    public NakedPairsType checkNakedPairs() {
        NakedPairsType nakedPairsType = new NakedPairsType();

        Set<Pair<Integer, Integer>> nakedPairsPositionSet = new HashSet<>();
        Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet = new HashSet<>();
        // Iterálás a táblán
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                CellPosition cell = sudokuBoard[row][col];
                // Csak a két lehetséges értékkel rendelkező cellák érdekelnek
                if (cell.getPossibleValues().size() == 2) {
                    Set<Integer> pairValues = cell.getPossibleValues();
                    // Sorban keresés
                    for (int j = 0; j < SIZE; j++) {
                        if (j != col && sudokuBoard[row][j].getPossibleValues().equals(pairValues)) {
                            if (!nakedPairsPositionSet.contains(new Pair<>(row, col)) && !nakedPairsPositionSet.contains(new Pair<>(row, j))) {
                                Logger.debug("NAKED PAIR: ({}, {}) and ({}, {})", row, col, row, j);
                            }
                            nakedPairsPositionSet.add(new Pair<>(row, col));
                            nakedPairsPositionSet.add(new Pair<>(row, j));
                            addRemovePostionAndValuesRow(row, col, j, pairValues, removeSet);
                        }
                    }

                    // Oszlopban keresés
                    for (int i = 0; i < SIZE; i++) {
                        if (i != row && sudokuBoard[i][col].getPossibleValues().equals(pairValues)) {
                            if (!nakedPairsPositionSet.contains(new Pair<>(row, col)) && !nakedPairsPositionSet.contains(new Pair<>(i, col))) {
                                Logger.debug("NAKED PAIR: ({}, {}) and ({}, {})", row, col, i, col);
                            }
                            nakedPairsPositionSet.add(new Pair<>(row, col));
                            nakedPairsPositionSet.add(new Pair<>(i, col));
                            addRemovePostionAndValuesCol(row, col, i, pairValues, removeSet);
                        }
                    }
                    // 3x3-as blokkban keresés
                    int startRow = row - row % 3;
                    int startCol = col - col % 3;
                    for (int i = startRow; i < startRow + 3; i++) {
                        for (int j = startCol; j < startCol + 3; j++) {
                            if ((i != row || j != col) && sudokuBoard[i][j].getPossibleValues().equals(pairValues)) {
                                if (!nakedPairsPositionSet.contains(new Pair<>(row, col)) && !nakedPairsPositionSet.contains(new Pair<>(i, j))) {
                                    Logger.debug("NAKED PAIR: ({}, {}) and ({}, {})", row, col, i, j);
                                }
                                nakedPairsPositionSet.add(new Pair<>(row, col));
                                nakedPairsPositionSet.add(new Pair<>(i, j));
                                addRemovePositionAndValuesBox(startRow, startCol, pairValues, removeSet);
                            }
                        }
                    }
                }
            }
        }
        if (nakedPairsPositionSet.isEmpty()) {
            return null;
        }
        nakedPairsType.setNakedPairsPositionSet(nakedPairsPositionSet);
        nakedPairsType.setRemoveSet(removeSet);

        return nakedPairsType;
    }

    private void addRemovePostionAndValuesRow(int row, int col, int j, Set<Integer> pairValues, Set<Pair<Pair<Integer, Integer>, Set<Integer>>> results) {
        for (int i = 0; i < SIZE; i++) {
            if (i != col && i != j) {
                Set<Integer> removeValueSet = new HashSet<>();
                for (int value : pairValues) {
                    if (sudokuBoard[row][i].getPossibleValues().contains(value)) {
                        removeValueSet.add(value);
                    }
                }
                if (!removeValueSet.isEmpty()) {
                    results.add(new Pair<>(new Pair<>(row, i), removeValueSet));
                }
            }
        }
    }

    private void addRemovePostionAndValuesCol(int row, int col, int j, Set<Integer> pairValues, Set<Pair<Pair<Integer, Integer>, Set<Integer>>> results) {
        for (int i = 0; i < SIZE; i++) {
            if (i != row && i != j) {
                Set<Integer> removeValueSet = new HashSet<>();
                for (int value : pairValues) {
                    if (sudokuBoard[i][col].getPossibleValues().contains(value)) {
                        removeValueSet.add(value);
                    }
                }
                if (!removeValueSet.isEmpty()) {
                    results.add(new Pair<>(new Pair<>(i, col), removeValueSet));
                }
            }
        }
    }

    private void addRemovePositionAndValuesBox(int row, int col, Set<Integer> pairValues, Set<Pair<Pair<Integer, Integer>, Set<Integer>>> results) {
        // Meghatározzuk a 3x3-as blokk kezdő pozícióját
        int startRow = row - row % 3;
        int startCol = col - col % 3;

        // Halmazok az eltávolítandó pozíciók és értékek tárolására
        Set<Pair<Integer, Integer>> removePositionSet = new HashSet<>();
        Set<Integer> removeValueSet = new HashSet<>();

        // Végigmegyünk a 3x3-as blokkon
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                // Kizárjuk a meztelen pár celláit
                if ((i == row && j == col) || sudokuBoard[i][j].getPossibleValues().equals(pairValues)) {
                    continue;
                }
                // Ellenőrizzük, hogy mely értékeket kell eltávolítani
                for (Integer value : pairValues) {
                    if (sudokuBoard[i][j].getPossibleValues().contains(value)) {
                        removeValueSet.add(value);
                        removePositionSet.add(new Pair<>(i, j));
                    }
                }
            }
        }

        if (!removeValueSet.isEmpty()) {
            for (Pair<Integer, Integer> position : removePositionSet) {
                results.add(new Pair<>(position, new HashSet<>(removeValueSet)));
            }
        }
    }

    public NakedPairsType checkHiddenPairs() {
        NakedPairsType hiddenPairsType = new NakedPairsType();
        Set<Pair<Integer, Integer>> hiddenPairsPositionSet = new HashSet<>();
        Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet = new HashSet<>();

        // Iterálás a táblán
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                CellPosition cell = sudokuBoard[row][col];
                // Csak a több lehetséges értékkel rendelkező cellák érdekelnek
                if (cell.getPossibleValues().size() > 2) {
                    // Ellenőrizzük, hogy van-e rejtett pár a sorban, oszlopban és blokkban
                    checkDirectionForHiddenPairs(row, col, hiddenPairsPositionSet, removeSet, "row");
                    checkDirectionForHiddenPairs(row, col, hiddenPairsPositionSet, removeSet, "col");
                    checkDirectionForHiddenPairs(row, col, hiddenPairsPositionSet, removeSet, "box");
                }
            }
        }

        if (hiddenPairsPositionSet.isEmpty()) {
            return null;
        }
        hiddenPairsType.setNakedPairsPositionSet(hiddenPairsPositionSet);
        hiddenPairsType.setRemoveSet(removeSet);

        return hiddenPairsType;
    }

    private void checkDirectionForHiddenPairs(int row, int col, Set<Pair<Integer, Integer>> hiddenPairsPositionSet, Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet, String direction) {
        // Tároljuk el a lehetséges értékeket és azok előfordulásait.
        Map<Integer, List<Pair<Integer, Integer>>> valueOccurrences = new HashMap<>();

        // A vizsgálati terület meghatározása a direction alapján
        int startRow = direction.equals("row") ? row : (direction.equals("box") ? row - row % 3 : 0);
        int startCol = direction.equals("col") ? col : (direction.equals("box") ? col - col % 3 : 0);
        int endRow = direction.equals("row") ? row + 1 : (direction.equals("box") ? startRow + 3 : SIZE);
        int endCol = direction.equals("col") ? col + 1 : (direction.equals("box") ? startCol + 3 : SIZE);

        // Összegyűjtjük a lehetséges értékek előfordulásait
        for (int r = startRow; r < endRow; r++) {
            for (int c = startCol; c < endCol; c++) {
                // Kihagyjuk a vizsgálati területen kívüli cellákat
                if ((direction.equals("row") && r != row) || (direction.equals("col") && c != col)) continue;

                Set<Integer> possibleValues = sudokuBoard[r][c].getPossibleValues();
                for (Integer value : possibleValues) {
                    valueOccurrences.computeIfAbsent(value, k -> new ArrayList<>()).add(new Pair<>(r, c));
                }
            }
        }

        // Keresünk rejtett párokat: értékek, amik pontosan két helyen fordulnak elő
        for (Map.Entry<Integer, List<Pair<Integer, Integer>>> entry : valueOccurrences.entrySet()) {
            if (entry.getValue().size() == 2) { // Ha csak két cellában fordul elő az érték
                Integer value = entry.getKey();
                List<Pair<Integer, Integer>> positions = entry.getValue();

                // Ellenőrizzük, hogy van-e párosítás az előfordulások között
                for (Map.Entry<Integer, List<Pair<Integer, Integer>>> otherEntry : valueOccurrences.entrySet()) {
                    if (!otherEntry.getKey().equals(value) && otherEntry.getValue().equals(positions)) {
                        // Megtaláltunk egy rejtett párt

                        if (!hiddenPairsPositionSet.contains(new Pair<>(positions.get(0).getKey(), positions.get(0).getValue()))
                                && !hiddenPairsPositionSet.contains(new Pair<>(positions.get(1).getKey(), positions.get(1).getValue()))) {
                            Logger.debug("HIDDEN PAIR: ({}, {}) and ({}, {})", positions.get(0).getKey(), positions.get(0).getValue(),
                                    positions.get(1).getKey(), positions.get(1).getValue());
                        }

                        hiddenPairsPositionSet.addAll(positions);

                        for (Pair<Integer, Integer> position : positions) {
                            for (int removeValue : sudokuBoard[position.getKey()][position.getValue()].getPossibleValues()) {
                                if (removeValue != value && removeValue != otherEntry.getKey()) {
                                    removeSet.add(new Pair<>(position, new HashSet<>(List.of(removeValue))));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
