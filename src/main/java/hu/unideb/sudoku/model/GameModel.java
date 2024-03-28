package hu.unideb.sudoku.model;

import javafx.util.Pair;
import org.tinylog.Logger;

import java.util.*;

/**
 * Játék üzleti logikáját valósítja meg.
 */
public class GameModel {
    private static GameDifficulty difficulty;
    private static final int SIZE = 9;
    private static final int EASY_MOD_REVOME_DIGITS = 44;
    private static final int MEDIUM_MOD_REVOME_DIGITS = 48;
    private static final int HARD_MOD_REVOME_DIGITS = 55;
    private static final String SINGLE_LOG_FORMAT = "[{}][{}] = {}";
    private static final String PAIR_LOG_FORMAT = "[{}, {}] and [{}, {}]";
    private static final String NAKED_PAIR = "NAKED PAIR:";
    private final CellPosition[][] sudokuBoard;
    private final CellPosition[][] solvedBoard;
    private final CellPosition[][] originalBoard;
    private static final Random rand = new Random();
    private static boolean needHistoryLoad = false;
    private int helpCounter = 0;
    private final Set<Pair<Pair<Integer, Integer>, Set<Integer>>> checkedPairSet = new HashSet<>();

    /**
     * Inicializálja a játékot.
     * Létrehozza a játéktáblát, a megoldott táblát és az eredeti táblát.
     * Amennyiben nincs szükség előzmények betöltésére, generál egy új Sudoku táblát.
     */
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

    /**
     * Betölti a játékot egy korábban mentett állapotból.
     * Beállítja a játék nehézségi szintjét és a táblákat az előzmények alapján.
     *
     * @param history A betöltendő játék előzménye.
     */
    public void loadGameFromHistory(GameHistory history) {
        setDifficulty(GameDifficulty.valueOf(history.getDifficulty()));
        deepCopy(history.getOriginalBoard(), originalBoard);
        deepCopy(history.getOriginalBoard(), sudokuBoard);
        deepCopy(history.getSolvedBoard(), solvedBoard);
    }

    /**
     * Megoldja a játékot, átmásolva a megoldott tábla állapotát a jelenlegi táblába.
     */
    public void solve() {
        deepCopy(solvedBoard, sudokuBoard);
    }

    /**
     * Generál egy új Sudoku táblát.
     * Először kitölti az átlós 3x3-as blokkokat, majd a maradék helyeket.
     * végül eltávolít néhány számot a nehézségi szintnek megfelelően.
     */
    private void generateSudoku() {
        fillDiagonal();
        fillRemaining(0, 3);

        deepCopy(sudokuBoard, solvedBoard);

        if (difficulty == GameDifficulty.EASY) {
            removeDigits(EASY_MOD_REVOME_DIGITS);
        } else if (difficulty == GameDifficulty.MEDIUM) {
            removeDigits(MEDIUM_MOD_REVOME_DIGITS);
        } else {
            removeDigits(HARD_MOD_REVOME_DIGITS);
        }
        storePossibleValues();

        deepCopy(sudokuBoard, originalBoard);
    }

    /**
     * Átmásolja a source tábla értékeit, a destination táblába.
     *
     * @param source      tábla amelyet szeretnénk másolni
     * @param destination tábla ahova szeeretnénk másolni
     */
    public void deepCopy(CellPosition[][] source, CellPosition[][] destination) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                destination[i][j] = new CellPosition(source[i][j].getValue(), new HashSet<>(source[i][j].getPossibleValues()));
            }
        }
    }

    /**
     * Kitölti tábla átlós 3x3-as blokkjait véletlenszerű számokkal.
     */
    private void fillDiagonal() {
        for (int i = 0; i < SIZE; i = i + 3) {
            fillBox(i, i);
        }
    }

    /**
     * Kitölt egy 3x3-as blokkot véletlenszerű számokkal,.
     *
     * @param row A blokk kezdő sorának indexe.
     * @param col A blokk kezdő oszlopának indexe.
     */
    private void fillBox(int row, int col) {
        int num;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                do {
                    num = rand.nextInt(SIZE) + 1;
                } while (!unUsedInBox(sudokuBoard, row, col, num));

                sudokuBoard[row + i][col + j].setValue(num);
            }
    }

    /**
     * Ellenőrzi, hogy egy adott szám szerepel-e már az adott sorban.
     *
     * @param board A tábla.
     * @param row   A sor indexe.
     * @param value Az ellenőrizendő szám.
     * @return Igaz, ha a szám nincs jelen a sorban, különben hamis.
     */
    private boolean unUsedInRow(CellPosition[][] board, int row, int value) {
        for (int col = 0; col < SIZE; col++) {
            if (board[row][col].getValue() == value) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ellenőrzi, hogy egy adott szám szerepel-e már az adott oszlopban.
     *
     * @param board A tábla.
     * @param col   A sor indexe, amiben keresünk.
     * @param value Az ellenőrizendő szám.
     * @return Igaz, ha a szám nincs jelen a oszlopban, különben hamis.
     */
    private boolean unUsedInCol(CellPosition[][] board, int col, int value) {
        for (int row = 0; row < SIZE; row++) {
            if (board[row][col].getValue() == value) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ellenőrzi, hogy egy adott szám szerepel-e már az adott blokkban.
     *
     * @param board    A tábla.
     * @param rowStart A blokk kezdő sorának indexe.
     * @param colStart A blokk kezdő oszlopának indexe.
     * @param value    Az ellenőrizendő szám.
     * @return Igaz, ha a szám nincs jelen a blokkban, különben hamis.
     */
    private boolean unUsedInBox(CellPosition[][] board, int rowStart, int colStart, int value) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[rowStart + i][colStart + j].getValue() == value) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Megpróbálja kitölteni a tábla maradék részeit anélkül, hogy szabályokat sértenénk.
     * Rekurzív megoldást használ, visszalépéssel, ha nem talál megoldást egy adott útvonalon.
     *
     * @param i A jelenlegi sor indexe.
     * @param j A jelenlegi oszlop indexe.
     * @return Igaz, ha sikerült kitölteni az egész táblát, különben hamis.
     */
    private boolean fillRemaining(int i, int j) {
        if (i == SIZE - 1 && j == SIZE) {
            return true;
        }

        if (j == SIZE) {
            i++;
            j = 0;
        }

        if (sudokuBoard[i][j].getValue() != 0) {
            return fillRemaining(i, j + 1);
        }

        for (int num = 1; num <= SIZE; num++) {
            if (isPlacementValid(sudokuBoard, i, j, num)) {
                sudokuBoard[i][j].setValue(num);
                if (fillRemaining(i, j + 1)) {
                    return true;
                }
                sudokuBoard[i][j].setValue(0);
            }
        }
        return false;
    }

    /**
     * Eltárolja az összes cella lehetséges értékeit.
     */
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

    /**
     * Eltárolja a cellák lehetséges értékeit az aktuális tábla állapota alapján.
     */
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

    /**
     * Eltávolítja a megadott értékeket a cella lehetséges értékei közül.
     *
     * @param row            A cella sorának indexe.
     * @param col            A cella oszlopának indexe.
     * @param valuesToRemove Az eltávolítandó értékek halmaza.
     */
    public void removePossibleValuesAt(int row, int col, Set<Integer> valuesToRemove) {
        Set<Integer> possibleValues = new HashSet<>(sudokuBoard[row][col].getPossibleValues());
        possibleValues.removeAll(valuesToRemove);
        sudokuBoard[row][col].setPossibleValues(possibleValues);
    }

    /**
     * Kiszámítja egy adott cella számára az új lehetséges értékeket a tábla aktuális állapota alapján.
     *
     * @param row A cella sorának indexe.
     * @param col A cella oszlopának indexe.
     * @return A cella számára lehetséges új értékek halmaza.
     */
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
        for (Pair<Pair<Integer, Integer>, Set<Integer>> checkedPosition : checkedPairSet) {
            if (checkedPosition.getKey().equals(currentPosition)) {
                possibleValues.removeAll(checkedPosition.getValue());
            }
        }

        return possibleValues;
    }

    /**
     * Ellenőrzi, hogy egy adott érték hozzáadható-e az adott cellához anélkül, hogy szabályokat sértenénk.
     *
     * @param row   A cella sorának indexe.
     * @param col   A cella oszlopának indexe.
     * @param value A hozzáadni kívánt érték.
     * @return Igaz, ha az érték hozzáadható anélkül, hogy szabályokat sértenénk, egyébként hamis.
     */
    public boolean isValueValid(int row, int col, int value) {
        return isValueUnusedInRow(row, col, value) && isValueUnusedInCol(row, col, value) && isValueUnusedInBox(row, col, value);
    }

    /**
     * Ellenőrzi, hogy egy adott érték szerepel-e egy adott sorban.
     *
     * @param row   A sor indexe.
     * @param col   A oszlop indexe .
     * @param value Az ellenőrizendő érték.
     * @return Igaz, ha az érték nem szerepel a sorban, egyébként hamis.
     */
    private boolean isValueUnusedInRow(int row, int col, int value) {
        for (int j = 0; j < 9; j++) {
            if (j != col && sudokuBoard[row][j].getValue() == value) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ellenőrzi, hogy egy adott érték szerepel-e egy adott oszlopban.
     *
     * @param row   A sor indexe.
     * @param col   A cella oszlopának indexe
     * @param value Az ellenőrizendő érték.
     * @return Igaz, ha az érték nem szerepel a oszlopban, egyébként hamis.
     */
    private boolean isValueUnusedInCol(int row, int col, int value) {
        for (int i = 0; i < 9; i++) {
            if (i != row && sudokuBoard[i][col].getValue() == value) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ellenőrzi, hogy egy adott érték szerepel-e egy adott blokkban.
     *
     * @param row   A sor indexe.
     * @param col   A cella oszlopának indexe
     * @param value Az ellenőrizendő érték.
     * @return Igaz, ha az érték nem szerepel a blokkban, egyébként hamis.
     */
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

    /**
     * Eltávolít véletlenszerűen számokat a cellából.
     * Biztosítja, hogy minden eltávolított szám után a tábla még mindig egyedi megoldással rendelkezzen.
     *
     * @param count Az eltávolítandó számok száma.
     */
    private void removeDigits(int count) {
        Set<Pair<Integer, Integer>> cellsToRemove = new HashSet<>();
        while (cellsToRemove.size() < count) {
            int i = rand.nextInt(SIZE);
            int j = rand.nextInt(SIZE);
            if (sudokuBoard[i][j].getValue() != 0) {
                int backupValue = sudokuBoard[i][j].getValue();
                sudokuBoard[i][j].setValue(0);

                // Ellenőrizze az egyediséget
                if (!hasUniqueSolution()) {
                    // Ha nincs egyedi megoldás, visszaállítja a számot
                    sudokuBoard[i][j].setValue(backupValue);
                } else {
                    // Sikeres eltávolítás, hozzáadjuk a sethez
                    cellsToRemove.add(new Pair<>(i, j));
                }
            }
        }
    }

    /**
     * Ellenőrzi, hogy a táblának van-e egyedi megoldása.
     * Ez a metódus a backtracking algoritmust használja, hogy megtalálja az összes lehetséges megoldást,
     * és megszakítja a keresést, ha több mint egy megoldást talál.
     *
     * @return Igaz, ha a táblának pontosan egy egyedi megoldása van, egyébként hamis.
     */
    public boolean hasUniqueSolution() {
        int[] numberOfSolutions = new int[1];
        checkForUniqueSolution(0, 0, numberOfSolutions);
        return numberOfSolutions[0] == 1;
    }

    /**
     * Rekurzív metódus, amely megszámolja a tábla összes lehetséges megoldását.
     * A tábla minden lehetséges értékével próbálkozik minden üres cellában,
     * és rekurzívan ellenőrzi, hogy a tábla ezekkel az értékekkel megoldható-e.
     * A metódus az első megoldás megtalálása után is folytatja a keresést, hogy meghatározza,
     * van-e több megoldás.
     *
     * @param row               A jelenlegi cella sorának indexe.
     * @param col               A jelenlegi cella oszlopának indexe.
     * @param numberOfSolutions Az eddig megtalált megoldások számát tartalmazó tömb.
     * @return Igaz, ha csak egy megoldás van, egyébként hamis.
     */
    private boolean checkForUniqueSolution(int row, int col, int[] numberOfSolutions) {
        if (row == SIZE) {
            numberOfSolutions[0]++; // Egy lehetséges megoldás megtalálva
            return numberOfSolutions[0] == 1; // Csak akkor tér vissza igaz értékkel, ha ez az első megoldás
        }

        int nextRow = (col == SIZE - 1) ? row + 1 : row;
        int nextCol = (col == SIZE - 1) ? 0 : col + 1;

        if (sudokuBoard[row][col].getValue() != 0) {
            // Ugrás a következő cellára, ha ez már ki van töltve
            return checkForUniqueSolution(nextRow, nextCol, numberOfSolutions);
        } else {
            for (int num = 1; num <= SIZE; num++) {
                if (isValueValid(row, col, num)) {
                    sudokuBoard[row][col].setValue(num);
                    if (checkForUniqueSolution(nextRow, nextCol, numberOfSolutions) && (numberOfSolutions[0] > 1)) {
                        return false; // Már több mint egy megoldás van, nem kell többet keresni

                    }
                    sudokuBoard[row][col].setValue(0); // BackTrack
                }
            }
        }
        return numberOfSolutions[0] == 1;
    }

    /**
     * Ellenőrzi, hogy egy adott érték hozzáadható-e egy cellához anélkül, hogy megsértené a Sudoku szabályait.
     *
     * @param board aktuális tábla
     * @param row   A cella sorának indexe.
     * @param col   A cella oszlopának indexe.
     * @param num   érték amit vizsgálunk
     * @return Igaz, ha az érték hozzáadható a cellához, egyébként hamis.
     */
    private boolean isPlacementValid(CellPosition[][] board, int row, int col, int num) {
        return (unUsedInRow(board, row, num) && unUsedInCol(board, col, num) && unUsedInBox(board, row - row % 3, col - col % 3, num));
    }

    /**
     * Alaphelyzetbe állítja a játéktáblát, visszaállítva az eredeti állapotot.
     */
    public void resetBoard() {
        helpCounter = 0;
        checkedPairSet.clear();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                sudokuBoard[i][j] = new CellPosition(originalBoard[i][j].getValue(), new HashSet<>(originalBoard[i][j].getPossibleValues()));
            }
        }
    }

    /**
     * Ellenőrzi, hogy a játék befejeződött-e, azaz minden cellában van érték.
     *
     * @return Igaz, ha a játék befejeződött, egyébként hamis.
     */
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

    /**
     * Ellenőrzi, hogy a játéktábla helyes-e.
     * Összeveti a jelenlegi tábla és a helyesen megoldott tábla értékeit.
     *
     * @return Igaz, ha a két tábla értékei azonosak, egyébként hamis.
     */
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

    /**
     * Azon cellák halmazát adja vissza, ahol a játékos által beírt érték helytelen.
     * Egy cella helytelen, ha eltér a megoldástól és nem üres.
     *
     * @return A helytelenül kitöltött cellák halmaza.
     */
    public Set<Pair<Integer, Integer>> getIncorrectValues() {
        Set<Pair<Integer, Integer>> incorrectValues = new HashSet<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int value = sudokuBoard[i][j].getValue();
                int correctValue = solvedBoard[i][j].getValue();
                if (value != 0 && value != correctValue) {
                    incorrectValues.add(new Pair<>(i, j));
                }
            }
        }
        return incorrectValues;
    }

    /**
     * Segítő algoritmus.
     * Bejárja a sorokat, oszlopokat és 3x3 boxokat.
     * Olyan cellákat keres ahol már csak egy cella maradt kitöltetlen.
     * Ekkor az értéket, és a pozícót letárolja egy halmazba
     *
     * @return a letárolt pozíció és értékek halmaza.
     */
    public Set<Pair<Integer, Pair<Integer, Integer>>> checkFullHouse() {
        Set<Pair<Integer, Pair<Integer, Integer>>> results = new HashSet<>();

        results.addAll(checkFullHouseForRowCOl(true));
        results.addAll(checkFullHouseForRowCOl(false));
        results.addAll(checkFullHouseByBoxes());

        return results;
    }

    /**
     * A full house algoritmus sorokban és oszlopokban való keresésre van.
     *
     * @param isRow Igaz, ha sorokat kell ellenőrizni, hamis, ha oszlopokat.
     * @return a letárolt pozíció és értékek halmaza.
     */
    private Set<Pair<Integer, Pair<Integer, Integer>>> checkFullHouseForRowCOl(boolean isRow) {
        Set<Pair<Integer, Pair<Integer, Integer>>> results = new HashSet<>();
        for (int i = 0; i < SIZE; i++) {
            int emptyCellCount = 0;
            Pair<Integer, Integer> fullHousePosition = null;
            int value = 0;
            for (int j = 0; j < SIZE; j++) {
                CellPosition cell = isRow ? sudokuBoard[i][j] : sudokuBoard[j][i];
                if (cell.getValue() == 0) {
                    emptyCellCount++;
                    value = cell.getPossibleValues().iterator().next();
                    fullHousePosition = isRow ? new Pair<>(i, j) : new Pair<>(j, i);
                }
            }
            addFullHouseResult(emptyCellCount, value, fullHousePosition, results);
        }
        return results;
    }

    /**
     * A full house algoritmus 3x3 blokk való keresésre van.
     *
     * @return a letárolt pozíció és értékek halmaza.
     */
    private Set<Pair<Integer, Pair<Integer, Integer>>> checkFullHouseByBoxes() {
        Set<Pair<Integer, Pair<Integer, Integer>>> results = new HashSet<>();
        int boxSize = 3;
        for (int boxRow = 0; boxRow < SIZE; boxRow += boxSize) {
            for (int boxCol = 0; boxCol < SIZE; boxCol += boxSize) {
                int emptyCellCount = 0;
                Pair<Integer, Integer> fullHousePosition = null;
                int value = 0;

                for (int i = 0; i < boxSize; i++) {
                    for (int j = 0; j < boxSize; j++) {
                        CellPosition cell = sudokuBoard[boxRow + i][boxCol + j];
                        if (cell.getValue() == 0) {
                            emptyCellCount++;
                            value = cell.getPossibleValues().iterator().next();
                            fullHousePosition = new Pair<>(boxRow + i, boxCol + j);
                        }
                    }
                }
                addFullHouseResult(emptyCellCount, value, fullHousePosition, results);
            }
        }
        return results;
    }

    /**
     * Hozzáadja a megtalált cella értékét és pozícióját a halmazhoz.
     *
     * @param emptyCellCount    ennyi üres cella van (nincs értéke)
     * @param value             beírandó érték
     * @param fullHousePosition megtalált cella pozíciója
     * @param results           ezeket tároló halmaz
     */
    private void addFullHouseResult(int emptyCellCount, int value, Pair<Integer, Integer> fullHousePosition, Set<Pair<Integer, Pair<Integer, Integer>>> results) {
        if (emptyCellCount == 1) {
            results.add(new Pair<>(value, fullHousePosition));
            Logger.debug("FULL HOUSE: " + SINGLE_LOG_FORMAT, fullHousePosition.getKey(), fullHousePosition.getValue(), value);
        }
    }

    /**
     * Segítő algortmus.
     * Azokat a cellákat keresi, amelyekben pontosan egy lehetséges szám található, amit biztonságosan elhelyezhetünk.
     * Ezek a cellák olyan egyértelmű választást jelentenek, ahol nincs más lehetőség.
     *
     * @return Egy halmazt ad vissza, amely tartalmazza azon cellák pozícióját és az egyetlen lehetséges számot,
     * amelyet az adott cellákban elhelyezhetünk.
     */
    public Set<Pair<Integer, Pair<Integer, Integer>>> checkNakedSingles() {
        Set<Pair<Integer, Pair<Integer, Integer>>> results = new HashSet<>();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                CellPosition cell = sudokuBoard[row][col];
                if (cell.getValue() == 0) {
                    Set<Integer> possibleValues = cell.getPossibleValues();

                    if (possibleValues.size() == 1) {
                        int value = possibleValues.iterator().next();
                        results.add(new Pair<>(value, new Pair<>(row, col)));
                        Logger.debug("NAKED SINGLE: " + SINGLE_LOG_FORMAT, row, col, value);
                    }
                }
            }
        }
        return results;
    }

    /**
     * Segítő algortmus.
     * Azokat a cellákat keresi, ahol egy adott szám csak egyetlen cellában lehetséges egy sorban, oszlopban vagy 3x3-as blokkban.
     * Bár a cella több lehetséges számot is tartalmazhat, de adott szám máshol nem lehetséges, így az adott cellába kell kerülnie.
     *
     * @return Egy halmazt ad vissza, amely tartalmazza azon cellák pozícióját és az egyetlen lehetséges számot,
     * amelyet az adott cellákban elhelyezhetünk.
     */
    public Set<Pair<Integer, Pair<Integer, Integer>>> checkHiddenSingles() {
        Set<Pair<Integer, Pair<Integer, Integer>>> results = new HashSet<>();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                CellPosition cell = sudokuBoard[row][col];
                if (cell.getValue() == 0) {
                    for (int value : cell.getPossibleValues()) {
                        if (isHiddenSingleCell(row, col, value)) {
                            results.add(new Pair<>(value, new Pair<>(row, col)));
                            Logger.debug("HIDDEN SINGLE: " + SINGLE_LOG_FORMAT, row, col, value);
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * Megvizsgálja, hogy az adott érték szerepel-e más cellában is mint lehetséges érték.
     *
     * @param row   cella sora
     * @param col   cella oszlopa
     * @param value ellenőrizendő érték
     * @return Igaz, ha a vizsgált szám csak az adott cellában lehetséges az adott kontextusban (sor, oszlop, blokk),
     * egyébként hamis.
     */
    private boolean isHiddenSingleCell(int row, int col, int value) {
        return !isPossibleValueInRow(row, col, value) && !isPossibleValueInCol(row, col, value) && !isPossibleValueInBox(row, col, value);
    }

    /**
     * Megvizsgálja, hogy az adott sorban, az adott érték szerepel-e más cellában is mint lehetséges érték.
     *
     * @param row   cella sora
     * @param col   cella oszlopa
     * @param value ellenőrizendő érték
     * @return Igaz, ha a vizsgált szám csak az adott cellában lehetséges az adott sorban,
     * egyébként hamis.
     */
    private boolean isPossibleValueInRow(int row, int col, int value) {
        for (int j = 0; j < SIZE; j++) {
            if (j != col && sudokuBoard[row][j].getPossibleValues().contains(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Megvizsgálja, hogy az adott oszlopban, az adott érték szerepel-e más cellában is mint lehetséges érték.
     *
     * @param row   cella sora
     * @param col   cella oszlopa
     * @param value ellenőrizendő érték
     * @return Igaz, ha a vizsgált szám csak az adott cellában lehetséges az adott oszlopban,
     * egyébként hamis.
     */
    private boolean isPossibleValueInCol(int row, int col, int value) {
        for (int i = 0; i < SIZE; i++) {
            if (i != row && sudokuBoard[i][col].getPossibleValues().contains(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Megvizsgálja, hogy az adott blokkban, az adott érték szerepel-e más cellában is mint lehetséges érték.
     *
     * @param row   cella sora
     * @param col   cella oszlopa
     * @param value ellenőrizendő érték
     * @return Igaz, ha a vizsgált szám csak az adott cellában lehetséges az adott blokkban,
     * egyébként hamis.
     */
    private boolean isPossibleValueInBox(int row, int col, int value) {
        int boxRow = row - row % 3;
        int boxCol = col - col % 3;

        for (int i = boxRow; i < boxRow + 3; i++) {
            for (int j = boxCol; j < boxCol + 3; j++) {
                if ((i != row || j != col) && sudokuBoard[i][j].getPossibleValues().contains(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Segítő algoritmus.
     * Azokat a cellapárokat keresi egy sorban, oszlopban vagy blokkban,
     * ahol csak két lehetséges szám található, és ezek a számok megegyeznek a két cellában.
     * Ez lehetővé teszi, hogy kizárjuk ezeket a számokat más cellák lehetséges értékei közül az adott területen.
     *
     * @return Egy NakedPairsType objektumot ad vissza, amely tárolja ezeket az értékeket és azok
     * pozícióit, valamint azon cellák pozícióit, ahonnan el kell távolítani a lehetséges értékeket.
     */
    public PairsType checkNakedPairs() {
        PairsType nakedPairsType = new PairsType();

        Set<Pair<Integer, Integer>> nakedPairsPositionSet = new HashSet<>();
        Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet = new HashSet<>();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                CellPosition cell = sudokuBoard[row][col];
                // Csak a két lehetséges értékkel rendelkező cellák érdekelnek
                if (cell.getPossibleValues().size() == 2) {
                    Set<Integer> pairValues = cell.getPossibleValues();
                    // Sorban keresés
                    checkNakedPairForRow(row, col, pairValues, nakedPairsPositionSet, removeSet);
                    // Oszlopban keresés
                    checkNakedPairForCol(row, col, pairValues, nakedPairsPositionSet, removeSet);
                    // 3x3-as blokkban keresés
                    checkNakedPairForBox(row, col, pairValues, nakedPairsPositionSet, removeSet);
                }
            }
        }
        return returnPairsType(nakedPairsType, nakedPairsPositionSet, removeSet);
    }

    /**
     * Visszaad egy PairsType objektumot, amely tartalmazza az azonosított párok pozícióit és a kizárandó értékeket.
     *
     * @param pairsType        Az előkészített PairsType objektum.
     * @param pairsPositionSet A talált párok pozícióinak halmaza.
     * @param removeSet        Az eltávolítandó értékek és azok pozícióinak halmaza.
     * @return A PairsType objektum, amelyet frissítettek az azonosított párokkal és eltávolítandó értékekkel.
     */
    private PairsType returnPairsType(PairsType pairsType, Set<Pair<Integer, Integer>> pairsPositionSet, Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet) {
        if (pairsPositionSet.isEmpty()) {
            return null;
        }
        pairsType.setPairsPositionSet(pairsPositionSet);
        pairsType.setRemoveSet(removeSet);

        return pairsType;
    }

    /**
     * Felfedi a Naked párokat egy sorban, és eltávolítja a megfelelő értékeket más cellákból.
     *
     * @param row                   A vizsgált sor indexe.
     * @param col                   A vizsgált oszlop indexe.
     * @param pairValues            A talált párban szereplő értékek halmaza.
     * @param nakedPairsPositionSet A talált párok pozícióinak halmaza.
     * @param removeSet             Az eltávolítandó értékek és azok pozícióinak halmaza.
     */
    private void checkNakedPairForRow(int row, int col, Set<Integer> pairValues, Set<Pair<Integer, Integer>> nakedPairsPositionSet, Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet) {
        for (int j = 0; j < SIZE; j++) {
            if (j != col && sudokuBoard[row][j].getPossibleValues().equals(pairValues)) {
                logForNakedPairRowCol(row, col, nakedPairsPositionSet, j, true);

                nakedPairsPositionSet.add(new Pair<>(row, col));
                nakedPairsPositionSet.add(new Pair<>(row, j));

                addRemovePositionAndValuesRowCol(row, col, j, pairValues, removeSet, true);
            }
        }
    }

    /**
     * Felfedi a Naked párokat egy oszlopban, és eltávolítja a megfelelő értékeket más cellákból.
     *
     * @param row                   A vizsgált sor indexe.
     * @param col                   A vizsgált oszlop indexe.
     * @param pairValues            A talált párban szereplő értékek halmaza.
     * @param nakedPairsPositionSet A talált párok pozícióinak halmaza.
     * @param removeSet             Az eltávolítandó értékek és azok pozícióinak halmaza.
     */
    private void checkNakedPairForCol(int row, int col, Set<Integer> pairValues, Set<Pair<Integer, Integer>> nakedPairsPositionSet, Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet) {
        for (int i = 0; i < SIZE; i++) {
            if (i != row && sudokuBoard[i][col].getPossibleValues().equals(pairValues)) {
                logForNakedPairRowCol(row, col, nakedPairsPositionSet, i, false);

                nakedPairsPositionSet.add(new Pair<>(row, col));
                nakedPairsPositionSet.add(new Pair<>(i, col));

                addRemovePositionAndValuesRowCol(row, col, i, pairValues, removeSet, false);
            }
        }
    }

    /**
     * Felfedi a Naked párokat egy blokkban, és eltávolítja a megfelelő értékeket más cellákból.
     *
     * @param row                   A vizsgált sor indexe.
     * @param col                   A vizsgált oszlop indexe.
     * @param pairValues            A talált párban szereplő értékek halmaza.
     * @param nakedPairsPositionSet A talált párok pozícióinak halmaza.
     * @param removeSet             Az eltávolítandó értékek és azok pozícióinak halmaza.
     */
    private void checkNakedPairForBox(int row, int col, Set<Integer> pairValues, Set<Pair<Integer, Integer>> nakedPairsPositionSet, Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet) {
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                if ((i != row || j != col) && sudokuBoard[i][j].getPossibleValues().equals(pairValues)) {
                    if (!nakedPairsPositionSet.contains(new Pair<>(row, col)) && !nakedPairsPositionSet.contains(new Pair<>(i, j))) {
                        Logger.debug(NAKED_PAIR + PAIR_LOG_FORMAT, row, col, i, j);
                    }

                    nakedPairsPositionSet.add(new Pair<>(row, col));
                    nakedPairsPositionSet.add(new Pair<>(i, j));

                    addRemovePositionAndValuesBox(startRow, startCol, pairValues, removeSet);
                }
            }
        }
    }

    /**
     * Logolja a talált párok információit a megadott sorban vagy oszlopban.
     *
     * @param row                   A vizsgált sor indexe.
     * @param col                   A vizsgált oszlop indexe.
     * @param nakedPairsPositionSet A meztelen párok pozícióinak halmaza.
     * @param otherIndex            A pár másik cellájának indexe.
     * @param isRow                 Igaz, ha a pár egy sorban található, hamis, ha egy oszlopban.
     */
    private void logForNakedPairRowCol(int row, int col, Set<Pair<Integer, Integer>> nakedPairsPositionSet, int otherIndex, boolean isRow) {
        Pair<Integer, Integer> currentPair = new Pair<>(row, col);
        Pair<Integer, Integer> otherPair = isRow ? new Pair<>(row, otherIndex) : new Pair<>(otherIndex, col);

        if (!nakedPairsPositionSet.contains(currentPair) && !nakedPairsPositionSet.contains(otherPair)) {
            if (isRow) {
                Logger.debug(NAKED_PAIR + PAIR_LOG_FORMAT, row, col, row, otherIndex);
            } else {
                Logger.debug(NAKED_PAIR + PAIR_LOG_FORMAT, row, col, otherIndex, col);
            }
        }
    }

    /**
     * Eltávolítja a talált párokban szereplő értékeket más cellákból egy adott sorban vagy oszlopban.
     *
     * @param row        A vizsgált sor indexe.
     * @param col        A vizsgált oszlop indexe.
     * @param j          A pár másik cellájának indexe (sornál aktuális oszlop, oszlop esetén az aktuális sor)
     * @param pairValues A meztelen párban szereplő értékek halmaza.
     * @param results    Az eltávolítandó értékek és azok pozícióinak halmaza.
     * @param isRow      Igaz, ha a pár egy sorban található, hamis, ha egy oszlopban.
     */
    private void addRemovePositionAndValuesRowCol(int row, int col, int j, Set<Integer> pairValues, Set<Pair<Pair<Integer, Integer>, Set<Integer>>> results, boolean isRow) {
        int otherIndex = isRow ? col : row;
        for (int i = 0; i < SIZE; i++) {
            if (i != otherIndex && i != j) {
                CellPosition cell = isRow ? sudokuBoard[row][i] : sudokuBoard[i][col];
                Set<Integer> removeValueSet = new HashSet<>();
                for (int value : pairValues) {
                    if (cell.getPossibleValues().contains(value)) {
                        removeValueSet.add(value);
                    }
                }
                addRemoveValueToSet(row, col, i, removeValueSet, results, isRow);
            }
        }
    }

    /**
     * Hozzáad egy értéket és annak eltávolítási pozícióját a megadott halmazhoz.
     *
     * @param row            A sor indexe, ahol az értéket eltávolítják.
     * @param col            Az oszlop indexe, ahol az értéket eltávolítják.
     * @param i              Sor vizsgálat esetén oszlop index, oszlop vizsgálat esetén sor index.
     * @param removeValueSet Az eltávolítandó értékek halmaza.
     * @param results        A végeredmények halmaza, amely tartalmazza az eltávolítási pozíciókat és a hozzájuk tartozó értékeket.
     * @param isRow          Igaz, ha a sorban történik az eltávolítás, hamis, ha oszlopban.
     */
    private void addRemoveValueToSet(int row, int col, int i, Set<Integer> removeValueSet, Set<Pair<Pair<Integer, Integer>, Set<Integer>>> results, boolean isRow) {
        if (!removeValueSet.isEmpty()) {
            if (isRow) {
                results.add(new Pair<>(new Pair<>(row, i), removeValueSet));
            } else {
                results.add(new Pair<>(new Pair<>(i, col), removeValueSet));
            }
        }
    }

    /**
     * Hozzáadja az eltávolítandó értékeket a megfelelő cellákhoz egy 3x3-as blokkban.
     *
     * @param removeValueSet    Az eltávolítandó értékek halmaza.
     * @param results           Az eredmények halmaza, amely tartalmazza a pozíciókat és az eltávolítandó értékeket.
     * @param removePositionSet Azon pozíciók halmaza, ahonnan értékeket kell eltávolítani.
     */
    private void addRemoveValueToSetBox(Set<Integer> removeValueSet, Set<Pair<Pair<Integer, Integer>, Set<Integer>>> results, Set<Pair<Integer, Integer>> removePositionSet) {
        if (!removeValueSet.isEmpty()) {
            for (Pair<Integer, Integer> position : removePositionSet) {
                results.add(new Pair<>(position, new HashSet<>(removeValueSet)));
            }
        }
    }

    /**
     * Eltávolítja a meztelen párokban szereplő értékeket más cellákból egy adott 3x3-as blokkban.
     *
     * @param row        A vizsgált sor indexe a blokkban.
     * @param col        A vizsgált oszlop indexe a blokkban.
     * @param pairValues Talált párban szereplő értékek halmaza.
     * @param results    Az eltávolítandó értékek és azok pozícióinak halmaza.
     */
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
        addRemoveValueToSetBox(removeValueSet, results, removePositionSet);
    }

    /**
     * Segítő algortmus.
     * Olyan cellákat keres, amelyeknek a lehetséges értéke több mint kettő.
     * Ezután bejárja sorokat, oszlopokat és blokkokat és összeszedi a rejtett párokat.
     *
     * @return Egy NakedPairsType objektumot ad vissza, amely tárolja ezeket az értékeket és azok
     * pozícióit, valamint azon cellák pozícióit, ahonnan el kell távolítani a lehetséges értékeket.
     */
    public PairsType checkHiddenPairs() {
        PairsType hiddenPairsType = new PairsType();
        Set<Pair<Integer, Integer>> hiddenPairsPositionSet = new HashSet<>();
        Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet = new HashSet<>();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                CellPosition cell = sudokuBoard[row][col];
                // Csak a több lehetséges értékkel rendelkező cellák érdekelnek
                if (cell.getPossibleValues().size() > 2) {
                    // Ellenőrizzük, hogy van-e rejtett pár a sorban, oszlopban és blokkban
                    checkHiddenPairForRowCol(row, hiddenPairsPositionSet, removeSet, true);
                    checkHiddenPairForRowCol(row, hiddenPairsPositionSet, removeSet, false);
                    checkHiddenPairForBox(row, col, hiddenPairsPositionSet, removeSet);
                }
            }
        }
        return returnPairsType(hiddenPairsType, hiddenPairsPositionSet, removeSet);
    }

    /**
     * Összeszedi az értékek előfordulásait sorban/oszlopban, majd továbbítja, hogy megtaláljuk és feldolgozzuk azokat.
     *
     * @param index                  sor vizsgálat esetén oszlop index, oszlop vizsgálat esetén sor index
     * @param hiddenPairsPositionSet rejtett párok pozíciói
     * @param removeSet              eltávolítandó értékek halmaza
     * @param isRow                  Igaz ha sor, hamis ha oszlop vizsgálat
     */
    private void checkHiddenPairForRowCol(int index, Set<Pair<Integer, Integer>> hiddenPairsPositionSet, Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet, boolean isRow) {
        Map<Integer, List<Pair<Integer, Integer>>> valueOccurrences = new HashMap<>();
        // Összegyűjtjük az értékek előfordulásait a sorban/oszlopban
        for (int i = 0; i < SIZE; i++) {
            Set<Integer> possibleValues = isRow ? sudokuBoard[index][i].getPossibleValues() : sudokuBoard[i][index].getPossibleValues();
            for (Integer value : possibleValues) {
                List<Pair<Integer, Integer>> positions = valueOccurrences.computeIfAbsent(value, k -> new ArrayList<>());
                if (isRow) {
                    positions.add(new Pair<>(index, i));
                } else {
                    positions.add(new Pair<>(i, index));
                }
            }
        }

        // Keresünk olyan értékeket, amelyek pontosan két helyen fordulnak elő
        findAndProcessHiddenPairs(valueOccurrences, hiddenPairsPositionSet, removeSet);
    }

    /**
     * Összeszedi az értékek előfordulásait a blokkokban, majd továbbítja, hogy megtaláljuk és feldolgozzuk azokat.
     *
     * @param row                    sor index
     * @param col                    oszlop index
     * @param hiddenPairsPositionSet rejtett párok pozíciói
     * @param removeSet              eltávolítandó értékek halmaza
     */
    private void checkHiddenPairForBox(int row, int col, Set<Pair<Integer, Integer>> hiddenPairsPositionSet, Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet) {
        Map<Integer, List<Pair<Integer, Integer>>> valueOccurrences = new HashMap<>();
        int startRow = row - row % 3;
        int startCol = col - col % 3;

        // Iterálás a 3x3-as blokkon belül
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                Set<Integer> possibleValues = sudokuBoard[i][j].getPossibleValues();
                for (Integer value : possibleValues) {
                    valueOccurrences.computeIfAbsent(value, k -> new ArrayList<>()).add(new Pair<>(i, j));
                }
            }
        }

        // Keresünk olyan értékeket, amelyek pontosan két helyen fordulnak elő
        findAndProcessHiddenPairs(valueOccurrences, hiddenPairsPositionSet, removeSet);
    }

    /**
     * Megkeresi a rejtett párokat.
     * Ha egy érték csak két cellában szerepel, akkor azt tovább vizsgálja
     * Eztuán ellenőrzi, hogy van-e másik érték, amely ugyanazokban a cellákban fordul elő
     * HA igen, akkor az egy rejtett pár
     *
     * @param valueOccurrences       az értékeket tárolja és azok pozícióit
     * @param hiddenPairsPositionSet rejtett párok pozíciói
     * @param removeSet              eltávolítandó értékek halmaza
     */
    private void findAndProcessHiddenPairs(Map<Integer, List<Pair<Integer, Integer>>> valueOccurrences, Set<Pair<Integer, Integer>> hiddenPairsPositionSet, Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet) {
        for (Map.Entry<Integer, List<Pair<Integer, Integer>>> entry : valueOccurrences.entrySet()) {
            if (entry.getValue().size() == 2) { // Ha csak két cellában fordul elő az érték
                Integer value = entry.getKey();
                List<Pair<Integer, Integer>> positions = entry.getValue();

                // Ellenőrizzük, hogy van-e párosítás az előfordulások között
                for (Map.Entry<Integer, List<Pair<Integer, Integer>>> otherEntry : valueOccurrences.entrySet()) {
                    if (!otherEntry.getKey().equals(value) && otherEntry.getValue().equals(positions)) {
                        // Megtaláltunk egy rejtett párt
                        processHiddenPair(positions, value, otherEntry, hiddenPairsPositionSet, removeSet);
                    }
                }
            }
        }
    }

    /**
     * Feldolgozza a rejtett párokat.
     * Eltávolítja ezekből a felesleges lehetséges értékeket.
     * Logol
     *
     * @param positions              A rejtett pár celláinak pozíciói.
     * @param value                  Az egyik érték a rejtett párból.
     * @param otherEntry             A másik érték és annak pozíciói a rejtett párból.
     * @param hiddenPairsPositionSet eddigi rejtett párok pozíciói
     * @param removeSet              eltávolítandó értékek halmaza
     */
    private void processHiddenPair(List<Pair<Integer, Integer>> positions, Integer value, Map.Entry<Integer, List<Pair<Integer, Integer>>> otherEntry, Set<Pair<Integer, Integer>> hiddenPairsPositionSet, Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet) {
        if (!hiddenPairsPositionSet.contains(new Pair<>(positions.get(0).getKey(), positions.get(0).getValue())) && !hiddenPairsPositionSet.contains(new Pair<>(positions.get(1).getKey(), positions.get(1).getValue()))) {
            Logger.debug("HIDDEN PAIR: " + PAIR_LOG_FORMAT, positions.get(0).getKey(), positions.get(0).getValue(), positions.get(1).getKey(), positions.get(1).getValue());
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

    /**
     * NakedPair és HiddenPair által eltávolított értékeket letárolja (és azok pozícióját).
     *
     * @param removeSet eltávolított értékeket és pozíciójuk halmaza.
     */
    public void addCheckedPairSet(Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet) {
        for (Pair<Pair<Integer, Integer>, Set<Integer>> removeEntry : removeSet) {
            Pair<Integer, Integer> position = removeEntry.getKey();
            Set<Integer> valuesToRemove = removeEntry.getValue();

            Pair<Pair<Integer, Integer>, Set<Integer>> checkedEntry = new Pair<>(position, valuesToRemove);
            checkedPairSet.add(checkedEntry);
        }
    }

    /**
     * Visszaadja a segítség lehívásainak számát.
     *
     * @return A segítség lehívásainak száma.
     */
    public int getHelpCounter() {
        return helpCounter;
    }

    /**
     * Megnöveli segítség lehívásainak számát.
     */
    public void increaseHelpCounter() {
        this.helpCounter++;
    }

    /**
     * Visszaadja a játéktábla aktuális állapotát.
     *
     * @return A játéktábla aktuális állapota.
     */
    public CellPosition[][] getSudokuBoard() {
        return sudokuBoard;
    }

    /**
     * Visszaadja a játék nehézségi szintjét.
     *
     * @return A játék nehézségi szintje.
     */
    public static GameDifficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Beállítja a játék nehézségi szintjét.
     *
     * @param dif A beállítandó nehézségi szint.
     */
    public static void setDifficulty(GameDifficulty dif) {
        difficulty = dif;
    }

    /**
     * Visszaadja a megoldott táblát.
     *
     * @return megoldott tábla.
     */
    public CellPosition[][] getSolvedBoard() {
        return solvedBoard;
    }

    /**
     * Visszaadja a játék eredeti állapotát tartalmazó táblát.
     *
     * @return A játék eredeti állapotát tartalmazó tábla.
     */
    public CellPosition[][] getOriginalBoard() {
        return originalBoard;
    }

    /**
     * Beállítja, hogy szükség van-e a játék előzményeinek betöltésére.
     *
     * @param needHistoryLoad Igaz, ha szükség van az előzmények betöltésére, egyébként hamis.
     */
    public static void setNeedHistoryLoad(boolean needHistoryLoad) {
        GameModel.needHistoryLoad = needHistoryLoad;
    }

    /**
     * Visszaadja, hogy szükség van-e a játék előzményeinek betöltésére.
     *
     * @return Igaz, ha szükség van az előzmények betöltésére, egyébként hamis.
     */
    public static boolean isNeedHistoryLoad() {
        return needHistoryLoad;
    }

    /**
     * Megadott értéket hozzárendeli az adott sor és oszlop pároshoz.
     *
     * @param row   A cella sorának indexe.
     * @param col   A cella oszlopának indexe.
     * @param value Az adott cellához hozzáadandó érték.
     */
    public void setValueAt(int row, int col, int value) {
        sudokuBoard[row][col].setValue(value);
    }

    /**
     * Visszaadja egy adott sorban és oszlopban lévő cella értékét.
     *
     * @param row A cella sorának indexe.
     * @param col A cella oszlopának indexe.
     * @return A cella értéke.
     */
    public int getValueAt(int row, int col) {
        return sudokuBoard[row][col].getValue();
    }

    /**
     * Beállítja az adott sorban és oszlopban lévő cella lehetséges értékeit.
     *
     * @param row    A cella sorának indexe.
     * @param col    A cella oszlopának indexe.
     * @param values A cellához beállítandó lehetséges értékek halmaza.
     */
    public void setPossibleValuesAt(int row, int col, Set<Integer> values) {
        sudokuBoard[row][col].setPossibleValues(values);
    }

    /**
     * Visszaadja egy adott sorban és oszlopban lévő cella lehetséges értékeit.
     *
     * @param row A cella sorának indexe.
     * @param col A cella oszlopának indexe.
     * @return A cella lehetséges értékeinek halmaza.
     */
    public Set<Integer> getPossibleValuesAt(int row, int col) {
        return sudokuBoard[row][col].getPossibleValues();
    }
}
