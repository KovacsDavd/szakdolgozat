package hu.unideb.sudoku.controller;

import hu.unideb.sudoku.model.CellPosition;
import hu.unideb.sudoku.model.GameHistory;
import hu.unideb.sudoku.model.GameHistoryService;
import hu.unideb.sudoku.model.GameModel;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.IOException;
import java.util.*;

public class GameController {
    private final GameModel model = new GameModel();
    private final TextArea[][] textAreas = new TextArea[9][9];
    private static final String INITIAL_NUMBER = "initial-number";
    private static final String POSSIBLE_VALUES = "possible-values";
    private static final String ERROR = "error";
    private static final String HINT = "hint";

    private Timeline timeline;
    private Duration time = Duration.ZERO;

    @FXML
    private GridPane board;

    @FXML
    private CheckBox possibleValuesCheckbox;

    @FXML
    private Label timerLabel;

    public void initialize() {
        if (!GameModel.isNeedHistoryLoad()) {
            createBoard();
            addCheckboxListener();
            startTimer();
        }
    }

    public void initializeWithHistory(GameHistory history) {
        model.loadGameFromHistory(history);
        GameModel.setNeedHistoryLoad(false);
        initialize();
    }

    private void startTimer() {
        if (timeline != null) {
            timeline.stop();
        }
        time = Duration.ZERO;
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void updateTimer() {
        time = time.add(Duration.seconds(1));
        timerLabel.setText(formatDuration(time));
    }

    private String formatDuration(Duration duration) {
        long seconds = (long) duration.toSeconds();
        return String.format("%d:%02d", seconds / 60, seconds % 60);
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
                    textArea.getStyleClass().add(POSSIBLE_VALUES);
                }

                textAreas[row][col] = textArea;
                textArea.getStyleClass().addAll(styles);
                setupTextArea(textArea, row, col);
                board.add(textArea, col, row);
            }
        }
    }

    private void addCheckboxListener() {
        possibleValuesCheckbox.setSelected(true);
        possibleValuesCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> togglePossibleValuesDisplay(newValue));
    }

    private void togglePossibleValuesDisplay(boolean show) {
        boolean hasError = false;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (updateTextAreaBasedOnVisibility(show, row, col)) {
                    hasError = true;
                }
            }
        }
        if (hasError) {
            showAlert("Hiba", "A cellában nincsenek lehetséges értékek.");
        }
    }

    private boolean updateTextAreaBasedOnVisibility(boolean show, int row, int col) {
        TextArea textArea = textAreas[row][col];
        if (!isInitialNumber(textArea) && textArea.getStyleClass().contains(POSSIBLE_VALUES)) {
            if (show) {
                return updateForVisibleState(textArea, row, col);
            } else {
                clearTextArea(textArea);
            }
        }
        return false;
    }

    private boolean updateForVisibleState(TextArea textArea, int row, int col) {
        Set<Integer> possibleValues = model.getPossibleValuesAt(row, col);
        String possibleValuesText = formatNumbers(possibleValues);
        textArea.setText(possibleValuesText);
        boolean hasError = false;
        if (possibleValues.isEmpty()) {
            addErrorStyleIfNeeded(textArea);
            hasError = true;
        } else {
            textArea.getStyleClass().remove(ERROR);
        }
        ensurePossibleValuesStyle(textArea);
        return hasError;
    }

    private void clearTextArea(TextArea textArea) {
        textArea.setText("");
        textArea.getStyleClass().remove(ERROR);
    }

    private void addErrorStyleIfNeeded(TextArea textArea) {
        if (!textArea.getStyleClass().contains(ERROR)) {
            textArea.getStyleClass().add(ERROR);
        }
    }

    private void ensurePossibleValuesStyle(TextArea textArea) {
        if (!textArea.getStyleClass().contains(POSSIBLE_VALUES)) {
            textArea.getStyleClass().add(POSSIBLE_VALUES);
        }
    }


    @FXML
    public void saveGame() {
        long elapsedTimeSeconds = (long) time.toSeconds();
        GameHistory gameHistory = new GameHistory(model.getOriginalBoard(), model.getSolvedBoard(), model.getSudokuBoard(), elapsedTimeSeconds, GameModel.getDifficult().toString());
        GameHistoryService.saveGameHistory(gameHistory);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void checkSudoku() {
        if (model.isComplete()) {
            if (model.isCorrect()) {
                timeline.stop();
                showAlert("Gratulálunk!", "Sikeresen megoldottad a Sudoku-t!");
                setEditingEnabled(false);
            } else {
                showAlert("Hiba", "Helytelen megoldás");
            }
        } else {
            showAlert("Hiba", "A megoldás befejezetlen.");
        }
    }

    private void setupTextArea(TextArea textArea, int row, int col) {
        if (!isInitialNumber(textArea)) {
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
        if (!isInitialNumber(textArea)) {
            if (text.isEmpty()) {
                revertTextAreaToModelValues(textArea, row, col);
            } else if (isSingleDigit(text)) {
                processSingleDigit(textArea, row, col, text);
            } else {
                processPossibleValues(textArea, row, col, text);
            }
        }
    }

    private boolean isInitialNumber(TextArea textArea) {
        return textArea.getStyleClass().contains(INITIAL_NUMBER);
    }

    private boolean isSingleDigit(String text) {
        return text.matches("\\d");
    }

    private void processSingleDigit(TextArea textArea, int row, int col, String text) {
        int value = Integer.parseInt(text);
        textArea.getStyleClass().remove(ERROR);
        updateTextArea(textArea, text, false);

        if (!model.isValueValid(row, col, value)) {
            textArea.getStyleClass().add(ERROR);
            showAlert("Hiba", "A(z) " + value + " szám már szerepel a sorban, oszlopban, vagy a 3x3-as dobozban.");
        }
        model.setValueAt(row, col, value);
    }

    private void processPossibleValues(TextArea textArea, int row, int col, String text) {
        textArea.getStyleClass().remove(ERROR);
        Set<Integer> newPossibleValues = extractNumbers(text);
        if (newPossibleValues.size() == 1) {
            revertTextAreaToModelValues(textArea, row, col);
        } else {
            if (!newPossibleValues.equals(model.getPossibleValuesAt(row, col))) {
                if (isValid(newPossibleValues)) {
                    updateTextAreaWithPossibleValues(textArea, row, col, newPossibleValues);
                } else {
                    revertTextAreaToModelValues(textArea, row, col);
                }
            } else {
                revertTextAreaToModelValues(textArea, row, col);
            }
        }
    }

    private boolean isValid(Set<Integer> possibleValues) {
        return possibleValues.stream().allMatch(num -> num >= 1 && num <= 9);
    }

    private void updateTextAreaWithPossibleValues(TextArea textArea, int row, int col, Set<Integer> newPossibleValues) {
        model.setPossibleValuesAt(row, col, newPossibleValues);
        updateTextArea(textArea, formatNumbers(newPossibleValues), true);
    }

    private void revertTextAreaToModelValues(TextArea textArea, int row, int col) {
        textArea.getStyleClass().remove(ERROR);
        Set<Integer> possibleValues = model.getPossibleValuesAt(row, col);
        String text = possibleValues.isEmpty() ? formatNumbers(Collections.singleton(model.getValueAt(row, col))) : formatNumbers(possibleValues);
        updateTextArea(textArea, text, !possibleValues.isEmpty());
    }

    private void updateTextArea(TextArea textArea, String text, boolean addPossibleValuesClass) {
        textArea.setText(text);
        if (addPossibleValuesClass && !textArea.getStyleClass().contains(POSSIBLE_VALUES)) {
            textArea.getStyleClass().add(POSSIBLE_VALUES);
        } else if (!addPossibleValuesClass) {
            textArea.getStyleClass().remove(POSSIBLE_VALUES);
            textArea.getStyleClass().remove(HINT);
        }
    }

    private Set<Integer> extractNumbers(String text) {
        Set<Integer> numbers = new HashSet<>();
        String[] parts = text.split("[,\\s]+");
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
        model.solve();
        updateViewWithSudokuBoard(model.getSolvedBoard());
        timeline.stop();
    }

    private void updateViewWithSudokuBoard(CellPosition[][] sudokuBoard) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                String textValue = sudokuBoard[row][col].getValue() != 0 ? String.valueOf(sudokuBoard[row][col].getValue()) : formatNumbers(sudokuBoard[row][col].getPossibleValues());

                textAreas[row][col].setText(textValue);
                textAreas[row][col].getStyleClass().remove(ERROR);
                textAreas[row][col].getStyleClass().remove(HINT);
                updateTextAreaStyles(textAreas[row][col], sudokuBoard[row][col].getValue(), sudokuBoard[row][col].getPossibleValues());
            }
        }
    }

    private void updateTextAreaStyles(TextArea textArea, int value, Set<Integer> possibleValues) {
        if (value != 0) {
            textArea.getStyleClass().remove(POSSIBLE_VALUES);
        } else if (!possibleValues.isEmpty() && (!textArea.getStyleClass().contains(POSSIBLE_VALUES))) {
            textArea.getStyleClass().add(POSSIBLE_VALUES);
        }
    }

    @FXML
    public void updatePossibleValues() {
        model.storePossibleValues();
        togglePossibleValuesDisplay(true);
    }

    @FXML
    public void backToMainMenu(ActionEvent event) throws IOException {
        Parent gameView = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("StarterView.fxml")));
        Scene gameScene = new Scene(gameView);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(gameScene);
        window.show();
    }

    @FXML
    public void resetBoard() {
        model.resetBoard();
        updateViewWithSudokuBoard(model.getOriginalBoard());
        setEditingEnabled(true);
        togglePossibleValuesDisplay(possibleValuesCheckbox.isSelected());
        startTimer();
    }

    private void setEditingEnabled(boolean enabled) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                TextArea textArea = textAreas[row][col];
                if (!textArea.getStyleClass().contains(INITIAL_NUMBER)) {
                    textArea.setEditable(enabled);
                }
            }
        }
    }

    @FXML
    public void helpStrategy() {
        updatePossibleValues();
        Set<Pair<Integer, Pair<Integer, Integer>>> resultSet = new HashSet<>();

        if (!resultSet.addAll(model.checkFullHouse()) && (!resultSet.addAll(model.checkNakedSingles()))) {
            resultSet.addAll(model.checkHiddenSingles());
        }

        if (!resultSet.isEmpty()) {
            applyStyleToCells(resultSet);
        }
    }

    public void applyStyleToCells(Set<Pair<Integer, Pair<Integer, Integer>>> cells) {
        for (Pair<Integer, Pair<Integer, Integer>> cellInfo : cells) {
            Pair<Integer, Integer> position = cellInfo.getValue();
            int row = position.getKey();
            int col = position.getValue();

            TextArea cell = textAreas[row][col];

            if (!cell.getStyleClass().contains(HINT)) {
                cell.getStyleClass().add(HINT);
            }
        }
    }

}
