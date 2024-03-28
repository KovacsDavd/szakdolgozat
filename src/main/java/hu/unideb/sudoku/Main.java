package hu.unideb.sudoku;

import javafx.application.Application;

/**
 * Az alkalmazás belépési pontja, amely elindítja a Sudoku játékot.
 */
public class Main {
    /**
     * Elindítja a JavaFX alkalmazást.
     *
     * @param args parancssori argumentumok, amelyekkel az alkalmazás indítva lesz
     */
    public static void main(String[] args) {
        Application.launch(SudokuApplication.class, args);
    }
}