package hu.unideb.sudoku.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class StarterController {
    @FXML
    public void showChooseLevelScene(ActionEvent event) throws IOException {
        Parent gameView = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("ChooseLevelView.fxml")));
        Scene scene = new Scene(gameView);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    @FXML
    public void showHistoryView(ActionEvent event) throws IOException {
        Parent historyView = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("historyView.fxml"))); // Ellenőrizd, hogy a fájl neve megfelelő-e
        Scene scene = new Scene(historyView);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    @FXML
    public void exit(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
