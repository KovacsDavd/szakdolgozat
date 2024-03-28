package hu.unideb.sudoku.controller;

import hu.unideb.sudoku.model.GameDifficulty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static hu.unideb.sudoku.model.GameModel.setDifficulty;

/**
 * Nehézségi szint választó kontrollerje
 * Itt állítjuk be a nehézségi  szintet
 * Átirányít a játék vagy a főmenü ablakhoz.
 */
public class ChooseLevelController {

    /**
     * Beállítja a megadott nehézségi szintet, majd átadja megjelenítésre a játék ablakot.
     *
     * @param difficulty nehézségi szint
     * @param event      esemény, mely tárolja az adatokat
     * @throws IOException kivételt dobunk, ha hiba történne a képernyő betöltése során
     */
    private void loadGameScreen(GameDifficulty difficulty, ActionEvent event) throws IOException {
        setDifficulty(difficulty);

        String fxmlPath = "fxml/GameView.fxml";
        showWindow(event, fxmlPath);
    }

    /**
     * Átadja megjelenítésre a főmenü ablakot.
     *
     * @param event esemény, mely kiváltja a metódus hívását, tárolja az adatokat
     * @throws IOException kivételt dob, ha hiba történne a képernyő betöltése során
     */
    @FXML
    private void backToMainMenu(ActionEvent event) throws IOException {
        String fxmlPath = "fxml/StarterView.fxml";
        showWindow(event, fxmlPath);
    }

    /**
     * Megjeleníti a megfelelő ablakot.
     *
     * @param event    tárolja az adatokat a forrásról
     * @param fxmlPath meghatározza, hogy melyik view-t kell megjeleníteni
     * @throws IOException kivételt dob, ha hiba történne a képernyő betöltése során
     */
    private void showWindow(ActionEvent event, String fxmlPath) throws IOException {
        URL fxmlUrl = Objects.requireNonNull(getClass().getClassLoader().getResource(fxmlPath));
        Parent gameView = FXMLLoader.load(fxmlUrl);
        Scene gameScene = new Scene(gameView);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(gameScene);
        window.show();
    }

    /**
     * Meghívja a loadGameScreen(...)-t, paraméterként átadja az event-et, könnyű nehézségi szinttel.
     *
     * @param event esemény, mely kiváltja a metódus hívását, tárolja az adatokat
     * @throws IOException kivételt dobunk, ha hiba történne a képernyő betöltése során
     */
    @FXML
    public void handleEasyButtonAction(ActionEvent event) throws IOException {
        loadGameScreen(GameDifficulty.EASY, event);
    }

    /**
     * Meghívja a loadGameScreen(...)-t, paraméterként átadja az event-et, közepes nehézségi szinttel.
     *
     * @param event esemény, mely kiváltja a metódus hívását, tárolja az adatokat
     * @throws IOException kivételt dobunk, ha hiba történne a képernyő betöltése során
     */
    @FXML
    public void handleMediumButtonAction(ActionEvent event) throws IOException {
        loadGameScreen(GameDifficulty.MEDIUM, event);
    }

    /**
     * Meghívja a loadGameScreen(...)-t, paraméterként átadja az event-et, nehéz nehézségi szinttel.
     *
     * @param event esemény, mely kiváltja a metódus hívását, tárolja az adatokat
     * @throws IOException kivételt dobunk, ha hiba történne a képernyő betöltése során
     */
    @FXML
    public void handleHardButtonAction(ActionEvent event) throws IOException {
        loadGameScreen(GameDifficulty.HARD, event);
    }
}
