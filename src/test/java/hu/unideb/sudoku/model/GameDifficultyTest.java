package hu.unideb.sudoku.model;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameDifficultyTest {

    @Test
    void testEnumValues() {
        assertEquals(GameDifficulty.EASY, GameDifficulty.valueOf("EASY"));
        assertEquals(GameDifficulty.MEDIUM, GameDifficulty.valueOf("MEDIUM"));
        assertEquals(GameDifficulty.HARD, GameDifficulty.valueOf("HARD"));
    }

    @Test
    void testEnumCount() {
        assertEquals(3, GameDifficulty.values().length);
    }
}
