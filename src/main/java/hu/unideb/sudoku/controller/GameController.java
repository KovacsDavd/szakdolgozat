
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
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class GameController {
    private final GameModel model = new GameModel();
    private final TextArea[][] textAreas = new TextArea[9][9];

    @FXML
    private Label levelLabel;

    @FXML
    private GridPane board;

    public void initialize() {
        levelLabel.setText(GameModel.getDifficult().toString());
        createBoard();
    }


    private void createBoard() {
        CellPosition[][] sudokuBoard = model.getSudokuBoard();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                TextArea textArea = new TextArea();
                List<String> styles = determineBorderStyles(row, col);

                if (sudokuBoard[row][col].getValue() != 0) {
                    textArea.setText(String.valueOf(sudokuBoard[row][col].getValue()));
                    styles.add("initial-number");
                    textArea.setEditable(false);
                } else {
                    String possibleValuesText = getPossibleValuesText(sudokuBoard[row][col].getPossibleValues());
                    textArea.setText(possibleValuesText);
                    textArea.getStyleClass().add("possible-values");
                    textArea.setEditable(true);
                }

                textAreas[row][col] = textArea;
                textArea.getStyleClass().addAll(styles);
                setupTextArea(textArea, row, col);
                board.add(textArea, col, row);
            }
        }
    }

    private void setupTextArea(TextArea textArea, int row, int col) {
        if (!textArea.getStyleClass().contains("initial-number")) {
            textArea.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("([1-9,\\n ]*)")) {
                    textArea.setText(oldVal);
                }
            });
        } else {
            textArea.setEditable(false);
        }

        textArea.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (Boolean.FALSE.equals(newVal)) {
                handleCellFocusLost(textArea, row, col);
            }
        });
    }

    private void handleCellFocusLost(TextArea textArea, int row, int col) {
        String text = textArea.getText().trim();
        if (!textArea.getStyleClass().contains("initial-number")) {
            if (text.matches("\\d")) {
                // Ha pontosan egy szám van a TextArea-ban, frissítjük a modellt
                int playerValue = Integer.parseInt(text);
                model.setPlayerValue(row, col, playerValue);
                textArea.getStyleClass().add("user-input");
            } else {
                // Ha nem egy szám van, vagy üres, visszaállítjuk az eredeti lehetséges értékeket
                Set<Integer> possibleValues = model.getPossibleValues(row, col);
                String possibleValuesText = getPossibleValuesText(possibleValues);
                textArea.setText(possibleValuesText);
                textArea.getStyleClass().remove("user-input");
                textArea.getStyleClass().add("possible-values");
            }
        }
    }

    private String getPossibleValuesText(Set<Integer> possibleValues) {
        StringBuilder sb = new StringBuilder();
        List<Integer> sortedValues = new ArrayList<>(possibleValues);
        Collections.sort(sortedValues);

        int count = 0;
        for (int value : sortedValues) {
            sb.append(value);
            count++;

            if (count < sortedValues.size() && count % 3 != 0) {
                sb.append(" ");
            }

            if (count % 3 == 0) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }


    private List<String> determineBorderStyles(int row, int col) {
        List<String> styles = new ArrayList<>();
        styles.add("sudoku-text-area");

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

    private void updateViewWithSudokuBoard(CellPosition[][] sudokuBoard) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (sudokuBoard[row][col].getValue() != 0) {
                    textAreas[row][col].setText(String.valueOf(sudokuBoard[row][col].getValue()));
                }
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
