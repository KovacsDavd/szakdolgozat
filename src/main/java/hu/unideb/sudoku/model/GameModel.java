package hu.unideb.sudoku.model;

public class GameModel {
    private static GameDifficult difficult;

    public static GameDifficult getDifficult() {
        return difficult;
    }

    public static void setDifficult(GameDifficult dif) {
        difficult = dif;
    }
}
