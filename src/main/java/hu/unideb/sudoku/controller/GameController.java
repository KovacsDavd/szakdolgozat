
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
    private static final String INITIAL_NUMBER = "initial-number";

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
                    styles.add(INITIAL_NUMBER);
                    textArea.setEditable(false);
                } else {
                    String possibleValuesText = formatNumbers(sudokuBoard[row][col].getPossibleValues());
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
        if (!textArea.getStyleClass().contains(INITIAL_NUMBER)) {
            textArea.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("([1-9,\\n ]*)")) {
                    textArea.setText(oldVal);
                }
            });
        } else {
            textArea.setEditable(false);
        }

        textArea.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (Boolean.FALSE.equals(newVal)) { // elveszti a focust ha newVal false lesz.
                handleCellFocusLost(textArea, row, col);
            }
        });
    }

    private void handleCellFocusLost(TextArea textArea, int row, int col) {
        String text = textArea.getText().trim();
        if (!textArea.getStyleClass().contains(INITIAL_NUMBER)) {
            if (text.matches("\\d")) { // Ha pontosan egy szám van a TextArea-ban
                int value = Integer.parseInt(text);
                model.setValueAt(row, col, value);
                textArea.getStyleClass().add("user-input");
            } else {
                Set<Integer> newPossibleValues = extractNumbers(text);
                if (!newPossibleValues.equals(model.getPossibleValuesAt(row, col))) {
                    boolean isValid = newPossibleValues.stream().allMatch(num -> num >= 1 && num <= 9);
                    if (isValid) {
                        textArea.setText(formatNumbers(newPossibleValues));
                        model.setPossibleValuesAt(row, col, newPossibleValues);
                    } else {
                        textArea.setText(formatNumbers(model.getPossibleValuesAt(row,col)));
                    }
                } else {
                    textArea.setText(formatNumbers(model.getPossibleValuesAt(row,col)));
                }
            }
        }
    }

    private Set<Integer> extractNumbers(String text) {
        Set<Integer> numbers = new HashSet<>();
        String[] parts = text.split("[,\\s\\n]+");
        for (String part : parts) {

            int number = Integer.parseInt(part);
            numbers.add(number);

        }
        return numbers;
    }

    private String formatNumbers(Set<Integer> numbers) {
        StringBuilder sb = new StringBuilder();
        List<Integer> sortedValues = new ArrayList<>(numbers);
        Collections.sort(sortedValues);

        for (int i = 0; i < sortedValues.size(); i++) {
            sb.append(sortedValues.get(i));
            if ((i + 1) % 3 == 0 && i < sortedValues.size() - 1) {
                sb.append("\n");
            } else if (i < sortedValues.size() - 1) {
                sb.append(" ");
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
        updateViewWithSudokuBoard(model.getSolvedBoard());
    }

    private void updateViewWithSudokuBoard(CellPosition[][] sudokuBoard) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (sudokuBoard[row][col].getValue() != 0) {
                    textAreas[row][col].setText(String.valueOf(sudokuBoard[row][col].getValue()));
                    textAreas[row][col].getStyleClass().remove("possible-values");
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
