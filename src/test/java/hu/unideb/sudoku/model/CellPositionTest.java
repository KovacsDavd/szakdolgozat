package hu.unideb.sudoku.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CellPositionTest {

    @Test
    void testGetValue() {
        CellPosition cellPosition = new CellPosition();
        cellPosition.setValue(2);

        assertEquals(2, cellPosition.getValue());
    }

    @Test
    void testGetPossibleValues() {
        CellPosition cellPosition =  new CellPosition();
        cellPosition.setPossibleValues(new HashSet<>(Set.of(10, 11)));

        assertEquals(2, cellPosition.getPossibleValues().size());
    }

    @Test
    void testFullConstructor() {
        Set<Integer> possibleValues = new HashSet<>(Set.of(1, 2));
        CellPosition cell = new CellPosition(5, possibleValues);

        assertEquals(5, cell.getValue());
        assertEquals(possibleValues, cell.getPossibleValues());
    }
}