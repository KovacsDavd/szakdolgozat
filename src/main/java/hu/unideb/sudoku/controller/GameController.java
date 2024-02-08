package hu.unideb.sudoku.controller;

import hu.unideb.sudoku.model.GameModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameController {
    private GameModel model = new GameModel();

    @FXML
    private Label levelLabel;
    @FXML
    private Button solveBtn;
    @FXML
    private Button hintBtn;
    @FXML
    private Button endBtn;
    @FXML
    private GridPane board;

    public void initialize() {
        levelLabel.setText(GameModel.getDifficult().toString());
        createBoard();
    }

    private void createBoard() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                Pane cell = new Pane();
                List<String> styles = determineBorderStyles(row, col);
                cell.getStyleClass().addAll(styles);
                board.add(cell, col, row);
            }
        }
    }

    private List<String> determineBorderStyles(int row, int col) {
        List<String> styles = new ArrayList<>();
        styles.add("sudoku-cell");

        // Speciális esetek kezelése külön metódusokkal
        addSpecialBorderStyle(styles, row, col);

        // Általános esetek kezelése
        addGeneralBorderStyle(styles, row, col);

        return styles;
    }

    private void addSpecialBorderStyle(List<String> styles, int row, int col) {
        if (col == 0 && row == 0) {
            styles.add("up-left-border-thick");
        } else if (col == 0 && isThickBorderRow(row)) {
            styles.add("bottom-left-border-thick");
        } else if (row == 0 && isThickBorderCol(col)) {
            styles.add("up-right-border-thick");
        } else if (isThickBorderRow(row) && isThickBorderCol(col)) {
            styles.add("bottom-right-border-thick");
        }
    }

    private void addGeneralBorderStyle(List<String> styles, int row, int col) {
        if (row == 0) {
            styles.add("up-border-thick");
        } else if (col == 0) {
            styles.add("left-border-thick");
        } else if (isThickBorderRow(row)) {
            styles.add("bottom-border-thick");
        } else if (isThickBorderCol(col)) {
            styles.add("right-border-thick");
        }
    }

    private boolean isThickBorderRow(int row) {
        return row == 2 || row == 5 || row == 8;
    }

    private boolean isThickBorderCol(int col) {
        return col == 2 || col == 5 || col == 8;
    }

    @FXML
    public void backToLevelChooser(ActionEvent event) throws IOException {
        Parent gameView = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("ChooseLevelView.fxml")));
        Scene gameScene = new Scene(gameView);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(gameScene);
        window.show();
    }
}
