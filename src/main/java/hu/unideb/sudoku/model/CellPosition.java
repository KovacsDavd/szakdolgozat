package hu.unideb.sudoku.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Cella értékét, és lehetséges értékeit tárolja.
 */
public class CellPosition {
    private int value;
    private Set<Integer> possibleValues;

    /**
     * Alapértelmezett konstruktor.
     * Inicializálja a lehetséges értékeket
     */
    public CellPosition() {
        this.possibleValues = new HashSet<>();
    }

    /**
     * Teljes konstruktor egy adott értékkel és lehetséges értékek halmazával.
     *
     * @param value          A cella értéke
     * @param possibleValues A cellában lehetséges értékek halmaza
     */
    public CellPosition(int value, Set<Integer> possibleValues) {
        this.value = value;
        this.possibleValues = new HashSet<>(possibleValues);
    }

    /**
     * Visszaadja a cella értékét.
     *
     * @return A cella értéke
     */
    public int getValue() {
        return value;
    }

    /**
     * Beállítja a cella értékét, és törli a lehetséges értékeket.
     *
     * @param value A beállítandó érték
     */
    public void setValue(int value) {
        this.value = value;
        this.possibleValues.clear();
    }

    /**
     * Visszaadja a cella lehetséges értékek halmazát.
     *
     * @return A lehetséges értékek halmaza
     */
    public Set<Integer> getPossibleValues() {
        return possibleValues;
    }

    /**
     * Beállítja a cella lehetséges értékeit.
     *
     * @param possibleValues A beállítandó lehetséges értékek halmaza.
     */
    public void setPossibleValues(Set<Integer> possibleValues) {
        this.possibleValues = possibleValues;
        this.value = 0;
    }
}
