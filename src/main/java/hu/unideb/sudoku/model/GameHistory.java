package hu.unideb.sudoku.model;

/**
 * A Sudoku játék egy mentett állását reprezentáló osztály.
 * Tárolja az eredeti és a megoldott tábla állapotát, az eltelt időt,
 * valamint a játék nehézségi szintjét.
 */
public class GameHistory {
    private final CellPosition[][] originalBoard;
    private final CellPosition[][] solvedBoard;
    private final long elapsedTimeSeconds;
    private final String difficulty;

    /**
     * Teljes konstruktor a játék mentett állásának létrehozásához.
     *
     * @param originalBoard      A játék eredeti táblája.
     * @param solvedBoard        A játék megoldott táblája.
     * @param elapsedTimeSeconds Az eltelt idő másodpercben.
     * @param difficulty         A játék nehézségi szintje.
     */
    public GameHistory(CellPosition[][] originalBoard, CellPosition[][] solvedBoard, long elapsedTimeSeconds, String difficulty) {
        this.originalBoard = originalBoard;
        this.solvedBoard = solvedBoard;
        this.elapsedTimeSeconds = elapsedTimeSeconds;
        this.difficulty = difficulty;
    }

    /**
     * Visszaadja a játék eredeti tábláját.
     *
     * @return A játék eredeti táblája.
     */
    public CellPosition[][] getOriginalBoard() {
        return originalBoard;
    }

    /**
     * Visszaadja a játék megoldott tábláját.
     *
     * @return A játék megoldott táblája.
     */
    public CellPosition[][] getSolvedBoard() {
        return solvedBoard;
    }

    /**
     * Az eltelt idő formázott megjelenítése percekben és másodpercekben.
     *
     * @return Az eltelt idő formázott alakban Stringként.
     */
    public String getElapsedTimeFormatted() {
        long minutes = (elapsedTimeSeconds % 3600) / 60;
        long seconds = elapsedTimeSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Visszaadja a játék nehézségi szintjét.
     *
     * @return A játék nehézségi szintje.
     */
    public String getDifficulty() {
        return difficulty;
    }
}

