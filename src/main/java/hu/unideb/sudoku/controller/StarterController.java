package hu.unideb.sudoku.controller;

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

/**
 * Főmenü kontrollerje.
 */
public class StarterController {
    /**
     * Átadja megjelenítésre a szint választó útvonalat.
     *
     * @param event esemény, mely kiváltja a metódus hívását, tárolja az adatokat
     * @throws IOException kivételt dobunk, ha hiba történne a képernyő betöltése során
     */
    @FXML
    public void showChooseLevelScene(ActionEvent event) throws IOException {
        String fxmlPath = "fxml/ChooseLevelView.fxml";
        showWindow(event, fxmlPath);
    }

    /**
     * Átadja megjelenítésre a mentések útvonalat.
     *
     * @param event esemény, mely kiváltja a metódus hívását, tárolja az adatokat
     * @throws IOException kivételt dobunk, ha hiba történne a képernyő betöltése során
     */
    @FXML
    public void showHistoryView(ActionEvent event) throws IOException {
        String fxmlPath = "fxml/historyView.fxml";
        showWindow(event, fxmlPath);
    }

    /**
     * Megjeleníti a megadott az ablak elérésútja alapján az ablakot.
     *
     * @param event    esemény, mely kiváltja a metódus hívását, tárolja az adatokat
     * @param fxmlPath a betöltendő ablak útvonala
     * @throws IOException kivételt dobunk, ha hiba történne a képernyő betöltése során
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
     * Bezárja az ablakot.
     *
     * @param event esemény, mely kiváltja a metódus hívását, tárolja az adatokat
     */
    @FXML
    public void exit(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
