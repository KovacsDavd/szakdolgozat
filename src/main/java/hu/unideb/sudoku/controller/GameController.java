package hu.unideb.sudoku.controller;

import hu.unideb.sudoku.model.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GameController {
    private final GameModel model = new GameModel();
    private final TextArea[][] textAreas = new TextArea[9][9];
    private Set<Pair<Integer, Pair<Integer, Integer>>> singleHelpSet = new HashSet<>();

    NakedPairsType nakedPairsType;

    private static final String INITIAL_NUMBER = "initial-number";
    private static final String POSSIBLE_VALUES = "possible-values";
    private static final String ERROR = "error";
    private static final String HINT = "hint";
    private boolean needMoreHelp = false;

    private Timeline timeline;
    private Duration time = Duration.ZERO;

    @FXML
    private GridPane board;

    @FXML
    private CheckBox possibleValuesCheckbox;

    @FXML
    private Label timerLabel;

    @FXML
    private Button helpButton;

    @FXML
    private Button recalculateButton;

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
        possibleValuesCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            getHelpButton().setDisable(!newValue);
            getRecalculateButton().setDisable(!newValue);
            togglePossibleValuesDisplay(newValue);
        });
    }

    private Button getHelpButton() {
        return helpButton;
    }

    private Button getRecalculateButton() {
        return recalculateButton;
    }

    private void togglePossibleValuesDisplay(boolean show) {
        model.storePossibleValues();
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
        Set<Integer> possibleValues = model.getNewPossibleValues(row, col);
        if (possibleValuesCheckbox.isSelected()) {
            String text = possibleValues.isEmpty() ? formatNumbers(Collections.singleton(model.getValueAt(row, col))) : formatNumbers(possibleValues);
            updateTextArea(textArea, text, !possibleValues.isEmpty());
        }
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
        needMoreHelp = false;
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

    //TODO: MÁS KELL MERT ez meghívja az ujra szamolást, amely az alapján számol ujra, hogy mik vannak beírva
    // Talán ki kellene ezt cserélni
    // VAGY MARADHAT DE UGY MODOSÍTANi, hogy csak elvegyen, hozzáadni ne

    // Segítségnél, ujraszámolásnál csak elvegyen
    // Lehetséges értékek reset (de ez elveszi a nakedpair-t is): letárolni honnan mit vett el, ezt ne rakja vissza
    @FXML
    public void helpStrategy() {
        if (!needMoreHelp) {
            updatePossibleValues();

            if (!singleHelpSet.addAll(model.checkFullHouse()) && (!singleHelpSet.addAll(model.checkNakedSingles())) &&
                    (!singleHelpSet.addAll(model.checkHiddenSingles()))) {
                nakedPairsType = model.checkNakedPairs();
                if (nakedPairsType == null) {
                    nakedPairsType = model.checkHiddenPairs();
                }
            }
            if (!singleHelpSet.isEmpty()) {
                Set<Pair<Integer, Integer>> simplifiedSet = singleHelpSet.stream()
                        .map(Pair::getValue)
                        .collect(Collectors.toSet());
                applyStyleToCells(simplifiedSet);

            } else if (nakedPairsType != null) {
                applyStyleToCells(nakedPairsType.getNakedPairsPositionSet());
            }
            needMoreHelp = true;
        } else {
            needMoreHelp = false;
            if (!singleHelpSet.isEmpty()) {
                for (Pair<Integer, Pair<Integer, Integer>> hint : singleHelpSet) {
                    int value = hint.getKey();
                    Pair<Integer, Integer> position = hint.getValue();
                    int row = position.getKey();
                    int col = position.getValue();
                    if (value == model.getSolvedBoard()[row][col].getValue()) {
                        TextArea textArea = textAreas[row][col];
                        model.setValueAt(row, col, value);
                        textArea.setText(String.valueOf(value));
                        textArea.getStyleClass().remove(POSSIBLE_VALUES);
                        textArea.getStyleClass().remove(HINT);
                    }
                }
                singleHelpSet.clear();
            } else {
                Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet = nakedPairsType.getRemoveSet();
                Set<Pair<Integer, Integer>> nakedPairsPositionSet = nakedPairsType.getNakedPairsPositionSet();

                for (Pair<Pair<Integer, Integer>, Set<Integer>> removeEntry : removeSet) {
                    Pair<Integer, Integer> position = removeEntry.getKey();
                    Set<Integer> valuesToRemove = removeEntry.getValue();

                    int row = position.getKey();
                    int col = position.getValue();

                    model.removePossibleValuesAt(row, col, valuesToRemove);

                    togglePossibleValuesDisplay(true);
                }
                for (Pair<Integer, Integer> pair: nakedPairsPositionSet) {
                    int row = pair.getKey();
                    int col = pair.getValue();
                    textAreas[row][col].getStyleClass().remove(HINT);
                }
                nakedPairsType = new NakedPairsType();
            }
        }
    }

    public void applyStyleToCells(Set<Pair<Integer, Integer>> cells) {
        for (Pair<Integer, Integer> cell : cells) {
            int row = cell.getKey();
            int col = cell.getValue();

            TextArea textArea = textAreas[row][col];
            if (!textArea.getStyleClass().contains(HINT)) {
                textArea.getStyleClass().add(HINT);
            }
        }
    }

}
