package hu.unideb.sudoku.model;

import javafx.util.Pair;

import java.util.Set;

public class NakedPairsType {
    private Set<Pair<Integer, Integer>> nakedPairsPositionSet;
    private Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet;

    public Set<Pair<Integer, Integer>> getNakedPairsPositionSet() {
        return nakedPairsPositionSet;
    }

    public void setNakedPairsPositionSet(Set<Pair<Integer, Integer>> nakedPairsPositionSet) {
        this.nakedPairsPositionSet = nakedPairsPositionSet;
    }

    public Set<Pair<Pair<Integer, Integer>, Set<Integer>>> getRemoveSet() {
        return removeSet;
    }

    public void setRemoveSet(Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet) {
        this.removeSet = removeSet;
    }
}
