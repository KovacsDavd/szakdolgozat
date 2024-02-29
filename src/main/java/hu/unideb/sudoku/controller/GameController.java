package hu.unideb.sudoku.controller;

import hu.unideb.sudoku.model.CellPosition;
import hu.unideb.sudoku.model.GameModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameController {
    private final GameModel model = new GameModel();
    private final TextField[][] textFields = new TextField[9][9];

    @FXML
    private Label levelLabel;

    @FXML
    private GridPane board;

    public void initialize() {
        levelLabel.setText(GameModel.getDifficult().toString());
        generateSudoku();
        createBoard();
    }

    private void generateSudoku() {
        model.generateSudoku();
    }

    private void createBoard() {
        CellPosition[][] sudokuBoard = model.getSudokuBoard();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                TextField textField = new TextField();
                textField.setPrefHeight(40);
                textField.setPrefWidth(40);

                List<String> styles = determineBorderStyles(row, col);

                if (sudokuBoard[row][col].getValue() != 0) {
                    styles.add("initial-number");
                }

                textField.getStyleClass().addAll(styles);
                setupTextField(textField);
                textField.setText(String.valueOf(sudokuBoard[row][col].getValue()));

                board.add(textField, col, row);
                textFields[row][col] = textField;
            }
        }
    }

    private void setupTextField(TextField textField) {
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("([1-9]|)") || newVal.length() > 1) {
                textField.setText(oldVal);
            }
        });

        if (textField.getStyleClass().contains("initial-number")) {
            textField.setEditable(false);
        }
    }

    private List<String> determineBorderStyles(int row, int col) {
        List<String> styles = new ArrayList<>();
        styles.add("sudoku-text-field");

        addSpecialBorderStyle(styles, row, col);
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
    public void solveSudoku() {
        model.solveSudoku();
        updateViewWithSudokuBoard(model.getSudokuBoardValues());
    }

    private void updateViewWithSudokuBoard(int[][] sudokuBoard) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                textFields[row][col].setText(String.valueOf(sudokuBoard[row][col]));
            }
        }
    }

    @FXML
    public void backToMainMenu(ActionEvent event) throws IOException {
        Parent gameView = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("StarterView.fxml")));
        Scene gameScene = new Scene(gameView);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(gameScene);
        window.show();
    }
}
