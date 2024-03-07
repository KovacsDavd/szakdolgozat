package hu.unideb.sudoku.model;

import java.util.HashSet;
import java.util.Set;

public class CellPosition {
    private int value;
    private Set<Integer> possibleValues;


    public CellPosition() {
        this.possibleValues = new HashSet<>();
    }

    public CellPosition(int value, Set<Integer> possibleValues) {
        this.value = value;
        this.possibleValues = new HashSet<>(possibleValues);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        this.possibleValues.clear();
    }

    public Set<Integer> getPossibleValues() {
        return possibleValues;
    }

    public void setPossibleValues(Set<Integer> possibleValues) {
        this.possibleValues = (possibleValues != null) ? possibleValues : new HashSet<>();
        this.value = 0;
    }
}
