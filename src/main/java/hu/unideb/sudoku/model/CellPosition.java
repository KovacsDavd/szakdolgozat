package hu.unideb.sudoku.model;

import java.util.HashSet;
import java.util.Set;

public class CellPosition {
    private int value;
    private Set<Integer> possibleValues;

    public CellPosition(int value, Set<Integer> possibleValues) {
        this.value = value;
        this.possibleValues = (possibleValues != null) ? possibleValues : new HashSet<>();
    }

    public CellPosition() {
        this.possibleValues = new HashSet<>();
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
    }

    public void removePossibleValue(int value) {
        possibleValues.remove(value);
    }

    public void addPossibleValue(int value) {
        possibleValues.add(value);
    }
}
