package hu.unideb.sudoku.model;

import javafx.util.Pair;

import java.util.Set;

/**
 * NakedPair és HiddenPair vizsgálata során talált párok információit reprezentálja.
 */
public class PairsType {
    private Set<Pair<Integer, Integer>> pairsPositionSet;
    private Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet;

    /**
     * Visszaadja a párok pozícióját.
     *
     * @return párok pozíciója
     */
    public Set<Pair<Integer, Integer>> getPairsPositionSet() {
        return pairsPositionSet;
    }

    /**
     * Beállítja a párok pozícióját.
     *
     * @param pairsPositionSet párok pozíciója.
     */
    public void setPairsPositionSet(Set<Pair<Integer, Integer>> pairsPositionSet) {
        this.pairsPositionSet = pairsPositionSet;
    }

    /**
     * Visszaadja a eltávolítandó értékeket és azok pozícióját.
     *
     * @return eltávolítandó értékeket és azok pozícióját.
     */
    public Set<Pair<Pair<Integer, Integer>, Set<Integer>>> getRemoveSet() {
        return removeSet;
    }

    /**
     * Beállítja az eltávolítandó értékeket és azok pozícióját.
     *
     * @param removeSet eltávolítandó értékeket és azok pozíciója.
     */
    public void setRemoveSet(Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet) {
        this.removeSet = removeSet;
    }
}
