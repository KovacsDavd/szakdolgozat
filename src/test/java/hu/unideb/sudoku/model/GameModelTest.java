package hu.unideb.sudoku.model;

import javafx.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GameModelTest {
    GameModel underTest;

    @BeforeEach
    void setUp() {
        underTest = new GameModel();
    }

    @Test
    void testLoadGameFromHistory() {
        CellPosition[][] originalBoard = new CellPosition[9][9];
        CellPosition[][] solvedBoard = new CellPosition[9][9];
        String difficulty = "HARD";
        long elapsedTime = 12L;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                originalBoard[i][j] = new CellPosition();
                solvedBoard[i][j] = new CellPosition();
            }
        }

        originalBoard[0][0].setValue(5);
        originalBoard[1][0].setPossibleValues(new HashSet<>(Set.of(1, 2)));
        solvedBoard[0][1].setValue(1);
        solvedBoard[1][1].setPossibleValues(new HashSet<>(Set.of(1, 2, 3)));

        GameHistory history = new GameHistory(originalBoard, solvedBoard, elapsedTime, difficulty);

        underTest.loadGameFromHistory(history);

        assertEquals(GameDifficulty.HARD, GameModel.getDifficulty());
        assertEquals(5, underTest.getOriginalBoard()[0][0].getValue());
        assertEquals(5, underTest.getSudokuBoard()[0][0].getValue());
        assertEquals(0, underTest.getOriginalBoard()[0][0].getPossibleValues().size());
        assertEquals(0, underTest.getSudokuBoard()[0][0].getPossibleValues().size());
        assertEquals(1, underTest.getSolvedBoard()[0][1].getValue());
        assertEquals(0, underTest.getSolvedBoard()[0][1].getPossibleValues().size());
        assertEquals(2, underTest.getOriginalBoard()[1][0].getPossibleValues().size());
        assertEquals(2, underTest.getSudokuBoard()[1][0].getPossibleValues().size());
        assertEquals(3, underTest.getSolvedBoard()[1][1].getPossibleValues().size());
    }

    @Test
    void testSolve() {
        underTest.generateSudoku();
        underTest.setValueAt(0, 1, 2);
        CellPosition[][] solvedBoard = underTest.getSolvedBoard();
        underTest.solve();

        assertEquals(solvedBoard[0][1].getValue(), underTest.getSudokuBoard()[0][1].getValue());
    }

    @Test
    void testGetPossibleValuesAt() {
        Set<Integer> possibleValues = new HashSet<>(Set.of(1, 2));
        underTest.setPossibleValuesAt(1, 1, possibleValues);

        assertEquals(possibleValues, underTest.getPossibleValuesAt(1, 1));
    }

    @Test
    void testgetValueAt() {
        underTest.setValueAt(1, 1, 1);

        assertEquals(1, underTest.getValueAt(1, 1));
    }

    @Test
    void testIsNeedHistoryLoad() {
        GameModel.setNeedHistoryLoad(true);

        assertTrue(GameModel.isNeedHistoryLoad());
    }

    @Test
    void testGetHelpCounter() {
        underTest.increaseHelpCounter();
        underTest.increaseHelpCounter();

        assertEquals(2, underTest.getHelpCounter());
    }

    @Test
    void testGetNewPossibleValues() {
        Pair<Integer, Integer> position = new Pair<>(0, 0);
        Set<Integer> valuesToRemove = new HashSet<>(new HashSet<>(Set.of(1, 2)));
        Pair<Pair<Integer, Integer>, Set<Integer>> entry = new Pair<>(position, valuesToRemove);
        Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet = new HashSet<>(Set.of(entry));

        underTest.addCheckedPairSet(removeSet);

        Set<Integer> possibleValues = underTest.getNewPossibleValues(0, 0);
        assertFalse(possibleValues.contains(1));
        assertFalse(possibleValues.contains(2));
        assertEquals(7, possibleValues.size());
    }

    @Test
    void testGenerateSudokuMedium() {
        GameModel.setDifficulty(GameDifficulty.MEDIUM);
        underTest.generateSudoku();

        long nonZeroCount = Stream.of(underTest.getSudokuBoard()).flatMap(Stream::of).filter(cell -> cell.getValue() != 0).count();

        long hasPossibleValuesCount = Stream.of(underTest.getSudokuBoard()).flatMap(Stream::of).filter(cell -> !cell.getPossibleValues().isEmpty()).count();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                assertNotEquals(0, underTest.getSolvedBoard()[i][j].getValue());
            }
        }
        assertEquals(33, nonZeroCount);
        assertEquals(48, hasPossibleValuesCount);
    }

    @Test
    void testGenerateSudokuEasy() {
        GameModel.setDifficulty(GameDifficulty.EASY);
        underTest.generateSudoku();

        long nonZeroCount = Stream.of(underTest.getSudokuBoard()).flatMap(Stream::of).filter(cell -> cell.getValue() != 0).count();

        long hasPossibleValuesCount = Stream.of(underTest.getSudokuBoard()).flatMap(Stream::of).filter(cell -> !cell.getPossibleValues().isEmpty()).count();

        assertEquals(37, nonZeroCount);
        assertEquals(44, hasPossibleValuesCount);
    }

    @Test
    void testGenerateSudokuHard() {
        GameModel.setDifficulty(GameDifficulty.HARD);
        underTest.generateSudoku();

        long nonZeroCount = Stream.of(underTest.getSudokuBoard()).flatMap(Stream::of).filter(cell -> cell.getValue() != 0).count();

        long hasPossibleValuesCount = Stream.of(underTest.getSudokuBoard()).flatMap(Stream::of).filter(cell -> !cell.getPossibleValues().isEmpty()).count();

        assertEquals(29, nonZeroCount);
        assertEquals(52, hasPossibleValuesCount);
    }

    @Test
    void testRemovePossibleValuesAt() {
        underTest.setPossibleValuesAt(0, 1, new HashSet<>(Set.of(1, 2, 3, 4)));
        underTest.removePossibleValuesAt(0, 1, new HashSet<>(Set.of(1, 2)));

        assertEquals(2, underTest.getPossibleValuesAt(0, 1).size());
    }

    @Test
    void testResetBoard() {
        underTest.generateSudoku();
        Pair<Integer, Integer> position = new Pair<>(0, 0);
        Set<Integer> valuesToRemove = new HashSet<>(new HashSet<>(Set.of(1, 2)));
        Pair<Pair<Integer, Integer>, Set<Integer>> entry = new Pair<>(position, valuesToRemove);
        Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet = new HashSet<>(Set.of(entry));

        underTest.addCheckedPairSet(removeSet);
        underTest.setValueAt(0, 0, 9);
        underTest.resetBoard();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                assertEquals(underTest.getOriginalBoard()[i][j].getValue(), underTest.getSudokuBoard()[i][j].getValue());
            }
        }
    }

    @Test
    void testIsCompleteFalse() {
        underTest.generateSudoku();

        assertFalse(underTest.isComplete());
    }

    @Test
    void testIsCompleteTrue() {
        underTest.generateSudoku();
        underTest.deepCopy(underTest.getSolvedBoard(), underTest.getSudokuBoard());

        assertTrue(underTest.isComplete());
    }


    @Test
    void testIsCorrectFalse() {
        underTest.generateSudoku();
        underTest.deepCopy(underTest.getSolvedBoard(), underTest.getSudokuBoard());
        underTest.setValueAt(0, 2, 0);

        assertFalse(underTest.isCorrect());
    }

    @Test
    void testIsCorrectTrue() {
        underTest.generateSudoku();
        underTest.deepCopy(underTest.getSolvedBoard(), underTest.getSudokuBoard());

        assertTrue(underTest.isCorrect());
    }

    @Test
    void testGetIncorrectValuesZero() {
        underTest.generateSudoku();

        assertEquals(0, underTest.getIncorrectValues().size());
    }

    @Test
    void testGetIncorrectValuesNotZero() {
        underTest.generateSudoku();
        underTest.setValueAt(0, 0, -1);

        assertEquals(1, underTest.getIncorrectValues().size());
    }

    @Test
    void testFullHouseRow() {
        underTest.generateSudoku();
        underTest.setValueAt(0, 0, 1);
        underTest.setValueAt(0, 1, 2);
        underTest.setValueAt(0, 2, 3);
        underTest.setValueAt(0, 3, 4);
        underTest.setValueAt(0, 4, 5);
        underTest.setValueAt(0, 5, 6);
        underTest.setValueAt(0, 6, 7);
        underTest.setValueAt(0, 7, 8);

        underTest.setPossibleValuesAt(0, 8, new HashSet<>(Set.of(9)));

        Set<Pair<Integer, Pair<Integer, Integer>>> results = underTest.checkFullHouse();

        Pair<Integer, Pair<Integer, Integer>> result = results.iterator().next();
        assertEquals(9, result.getKey());
        assertEquals(0, result.getValue().getKey());
        assertEquals(8, result.getValue().getValue());
    }

    @Test
    void testFullHouseCol() {
        underTest.generateSudoku();

        underTest.setValueAt(0, 0, 1);
        underTest.setValueAt(2, 0, 3);
        underTest.setValueAt(3, 0, 4);
        underTest.setValueAt(4, 0, 5);
        underTest.setValueAt(5, 0, 6);
        underTest.setValueAt(6, 0, 7);
        underTest.setValueAt(7, 0, 8);
        underTest.setValueAt(8, 0, 9);

        underTest.setPossibleValuesAt(1, 0, new HashSet<>(Set.of(2)));

        Set<Pair<Integer, Pair<Integer, Integer>>> results = underTest.checkFullHouse();

        Pair<Integer, Pair<Integer, Integer>> result = results.iterator().next();
        assertEquals(2, result.getKey());
        assertEquals(1, result.getValue().getKey());
        assertEquals(0, result.getValue().getValue());
    }

    @Test
    void testFullHouseBox() {
        underTest.generateSudoku();

        underTest.setValueAt(0, 0, 1);
        underTest.setValueAt(0, 1, 2);
        underTest.setValueAt(0, 2, 3);
        underTest.setValueAt(1, 0, 4);
        underTest.setValueAt(1, 2, 6);
        underTest.setValueAt(2, 0, 7);
        underTest.setValueAt(2, 1, 8);
        underTest.setValueAt(2, 2, 9);

        underTest.setPossibleValuesAt(1, 1, new HashSet<>(Set.of(5)));

        Set<Pair<Integer, Pair<Integer, Integer>>> results = underTest.checkFullHouse();

        Pair<Integer, Pair<Integer, Integer>> result = results.iterator().next();
        assertEquals(5, result.getKey());
        assertEquals(1, result.getValue().getKey());
        assertEquals(1, result.getValue().getValue());
    }

    @Test
    void testCheckNakedSingles() {
        underTest.generateSudoku();

        setFullBoard();

        underTest.setValueAt(1, 1, 0);
        underTest.setValueAt(2, 2, 0);

        underTest.setPossibleValuesAt(1, 1, new HashSet<>(Set.of(5, 9)));
        underTest.setPossibleValuesAt(2, 2, new HashSet<>(Set.of(5)));

        Set<Pair<Integer, Pair<Integer, Integer>>> results = underTest.checkNakedSingles();

        assertEquals(1, results.size());

        Pair<Integer, Pair<Integer, Integer>> result = results.iterator().next();
        assertEquals(5, result.getKey());
        assertEquals(2, result.getValue().getKey());
        assertEquals(2, result.getValue().getValue());
    }

    @Test
    void testCheckHiddenSinglesRow() {
        underTest.generateSudoku();

        setFullBoard();
        setValueRow();

        underTest.setPossibleValuesAt(1, 1, new HashSet<>(Set.of(5, 9)));
        underTest.setPossibleValuesAt(1, 2, new HashSet<>(Set.of(5, 7)));
        underTest.setPossibleValuesAt(1, 3, new HashSet<>(Set.of(5, 9)));

        Set<Pair<Integer, Pair<Integer, Integer>>> results = underTest.checkHiddenSingles();

        assertEquals(1, results.size());

        Pair<Integer, Pair<Integer, Integer>> result = results.iterator().next();
        assertEquals(7, result.getKey());
        assertEquals(1, result.getValue().getKey());
        assertEquals(2, result.getValue().getValue());
    }

    @Test
    void testCheckHiddenSinglesCol() {
        underTest.generateSudoku();

        setFullBoard();
        setValueCol();

        underTest.setPossibleValuesAt(1, 1, new HashSet<>(Set.of(5, 9)));
        underTest.setPossibleValuesAt(2, 1, new HashSet<>(Set.of(5, 7)));
        underTest.setPossibleValuesAt(3, 1, new HashSet<>(Set.of(5, 9)));

        Set<Pair<Integer, Pair<Integer, Integer>>> results = underTest.checkHiddenSingles();

        assertEquals(1, results.size());

        Pair<Integer, Pair<Integer, Integer>> result = results.iterator().next();
        assertEquals(7, result.getKey());
        assertEquals(2, result.getValue().getKey());
        assertEquals(1, result.getValue().getValue());
    }

    @Test
    void testCheckHiddenSinglesBox() {
        underTest.generateSudoku();

        setFullBoard();
        setValueBox();

        underTest.setPossibleValuesAt(1, 1, new HashSet<>(Set.of(5, 9)));
        underTest.setPossibleValuesAt(1, 3, new HashSet<>(Set.of(5, 7)));
        underTest.setPossibleValuesAt(2, 2, new HashSet<>(Set.of(5, 9)));

        Set<Pair<Integer, Pair<Integer, Integer>>> results = underTest.checkHiddenSingles();

        assertEquals(1, results.size());

        Pair<Integer, Pair<Integer, Integer>> result = results.iterator().next();
        assertEquals(7, result.getKey());
        assertEquals(1, result.getValue().getKey());
        assertEquals(3, result.getValue().getValue());
    }

    @Test
    void testCheckNakedPairRow() {
        underTest.generateSudoku();

        setFullBoard();
        setValueRow();

        underTest.setPossibleValuesAt(1, 1, new HashSet<>(Set.of(5, 9)));
        underTest.setPossibleValuesAt(1, 2, new HashSet<>(Set.of(5, 7)));
        underTest.setPossibleValuesAt(1, 3, new HashSet<>(Set.of(5, 9)));

        PairsType pairsType = underTest.checkNakedPairs();

        assertEquals(2, pairsType.getPairsPositionSet().size());
        assertFalse(pairsType.getRemoveSet().isEmpty());
    }

    @Test
    void testCheckNakedPairCol() {
        underTest.generateSudoku();

        setFullBoard();
        setValueCol();

        underTest.setPossibleValuesAt(1, 1, new HashSet<>(Set.of(5, 9)));
        underTest.setPossibleValuesAt(2, 1, new HashSet<>(Set.of(5, 7)));
        underTest.setPossibleValuesAt(3, 1, new HashSet<>(Set.of(5, 9)));

        PairsType pairsType = underTest.checkNakedPairs();

        assertEquals(2, pairsType.getPairsPositionSet().size());
        assertFalse(pairsType.getRemoveSet().isEmpty());
    }

    @Test
    void testCheckNakedPairBox() {
        underTest.generateSudoku();

        setFullBoard();
        setValueBox();

        underTest.setPossibleValuesAt(1, 1, new HashSet<>(Set.of(5, 9)));
        underTest.setPossibleValuesAt(1, 3, new HashSet<>(Set.of(5, 7)));
        underTest.setPossibleValuesAt(2, 2, new HashSet<>(Set.of(5, 9)));

        PairsType pairsType = underTest.checkNakedPairs();

        assertEquals(2, pairsType.getPairsPositionSet().size());
    }

    @Test
    void testCheckHiddenPairRow() {
        underTest.generateSudoku();

        setFullBoard();
        setValueRow();

        underTest.setPossibleValuesAt(1, 1, new HashSet<>(Set.of(5, 9, 1)));
        underTest.setPossibleValuesAt(1, 2, new HashSet<>(Set.of(5, 7)));
        underTest.setPossibleValuesAt(1, 3, new HashSet<>(Set.of(5, 9, 1)));

        PairsType pairsType = underTest.checkHiddenPairs();

        assertEquals(2, pairsType.getPairsPositionSet().size());
        assertFalse(pairsType.getRemoveSet().isEmpty());
    }

    @Test
    void testCheckHiddenPairCol() {
        underTest.generateSudoku();

        setFullBoard();
        setValueCol();

        underTest.setPossibleValuesAt(1, 1, new HashSet<>(Set.of(5, 9, 1)));
        underTest.setPossibleValuesAt(2, 1, new HashSet<>(Set.of(5, 7)));
        underTest.setPossibleValuesAt(3, 1, new HashSet<>(Set.of(5, 9, 1)));

        PairsType pairsType = underTest.checkHiddenPairs();

        assertEquals(2, pairsType.getPairsPositionSet().size());
        assertFalse(pairsType.getRemoveSet().isEmpty());
    }

    @Test
    void testCheckHiddenPairBox() {
        underTest.generateSudoku();

        setFullBoard();
        setValueBox();

        underTest.setPossibleValuesAt(1, 1, new HashSet<>(Set.of(5, 9, 1)));
        underTest.setPossibleValuesAt(1, 3, new HashSet<>(Set.of(5, 7)));
        underTest.setPossibleValuesAt(2, 2, new HashSet<>(Set.of(5, 9, 1)));

        PairsType pairsType = underTest.checkHiddenPairs();

        assertEquals(2, pairsType.getPairsPositionSet().size());
        assertFalse(pairsType.getRemoveSet().isEmpty());
    }

    private void setFullBoard() {
        int i = 1;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                underTest.setValueAt(row, col, i);
                i++;
            }
            i = 1;
        }
    }

    private void setValueRow() {
        underTest.setValueAt(1, 1, 0);
        underTest.setValueAt(1, 2, 0);
        underTest.setValueAt(1, 3, 0);
    }

    private void setValueCol() {
        underTest.setValueAt(1, 1, 0);
        underTest.setValueAt(2, 1, 0);
        underTest.setValueAt(3, 1, 0);
    }

    private void setValueBox() {
        underTest.setValueAt(1, 1, 0);
        underTest.setValueAt(1, 3, 0);
        underTest.setValueAt(2, 2, 0);
    }
}