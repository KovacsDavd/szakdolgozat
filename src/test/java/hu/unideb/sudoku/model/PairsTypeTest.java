package hu.unideb.sudoku.model;

import javafx.util.Pair;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PairsTypeTest {

    @Test
    void testGetPairsPositionSet() {
        PairsType pairsType = new PairsType();
        Set<Pair<Integer, Integer>> pairsPositionSet = new HashSet<>(Set.of(new Pair<>(1, 2)));
        pairsType.setPairsPositionSet(pairsPositionSet);

        assertEquals(pairsPositionSet, pairsType.getPairsPositionSet());
    }

    @Test
    void testGetRemoveSet() {
        PairsType pairsType = new PairsType();
        Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet = new HashSet<>();
        removeSet.add(new Pair<>(new Pair<>(1, 2), new HashSet<>(Set.of(3, 4))));
        pairsType.setRemoveSet(removeSet);

        assertEquals(removeSet, pairsType.getRemoveSet());
    }
}