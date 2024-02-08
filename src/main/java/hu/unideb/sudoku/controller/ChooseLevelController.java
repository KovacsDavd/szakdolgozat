package hu.unideb.sudoku.controller;

import hu.unideb.sudoku.model.GameDifficult;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

import static hu.unideb.sudoku.model.GameModel.setDifficult;

public class ChooseLevelController {
    private void loadGameScreen(GameDifficult difficult, ActionEvent event) throws IOException {
        setDifficult(difficult);

        Parent gameView = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("GameView.fxml")));
        Scene gameScene = new Scene(gameView);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(gameScene);
        window.show();
    }

    @FXML
    public void handleEasyButtonAction(ActionEvent event) throws IOException {
        loadGameScreen(GameDifficult.EASY, event);
    }

    @FXML
    public void handleMediumButtonAction(ActionEvent event) throws IOException {
        loadGameScreen(GameDifficult.MEDIUM, event);
    }

    @FXML
    public void handleHardButtonAction(ActionEvent event) throws IOException {
        loadGameScreen(GameDifficult.HARD, event);
    }
}
