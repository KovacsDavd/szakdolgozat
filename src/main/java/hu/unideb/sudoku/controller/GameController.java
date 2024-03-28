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

/**
 * A játék kontroller osztálya, összeköti a játék ablakot a játék logikával.
 */
public class GameController {
    private final GameModel model = new GameModel();
    private final TextArea[][] textAreas = new TextArea[9][9];
    private Set<Pair<Integer, Pair<Integer, Integer>>> singleHelpSet = new HashSet<>();
    PairsType pairsType;

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
    private Label helpCounter;
    @FXML
    private Button helpButton;
    @FXML
    private Button recalculateButton;
    @FXML
    private Button resetButton;

    /**
     * Inicializálja az osztályt.
     */
    public void initialize() {
        if (!GameModel.isNeedHistoryLoad()) {
            createBoard();
            addCheckboxListener();
            startTimer();
            updateHelpCounterDisplay();
        }
    }

    /**
     * Inicializálja az osztályt, ha a mentések részből indítjuk el.
     *
     * @param history tárolja az megjelenítéshez szükséges adatokat
     */
    public void initializeWithHistory(GameHistory history) {
        model.loadGameFromHistory(history);
        GameModel.setNeedHistoryLoad(false);
        initialize();
    }

    /**
     * Elindítja a stoppert.
     */
    private void startTimer() {
        if (timeline != null) {
            timeline.stop();
        }
        time = Duration.ZERO;
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Növeli a stopper idejét.
     */
    private void updateTimer() {
        time = time.add(Duration.seconds(1));
        timerLabel.setText(formatDuration(time));
    }

    /**
     * Megformázza a stoppert "perc:másodperc" formába.
     *
     * @param duration Az időtartam, amelyet formázni kívánunk
     * @return A formázott időtartam sztringként visszatér
     */
    private String formatDuration(final Duration duration) {
        long seconds = (long) duration.toSeconds();
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    /**
     * Lekéri modelből a táblát.
     * Ráteszi a cellákra a megfelelő stílust
     */
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

    /**
     * A checkbox-hoz rendel egy listener-t.
     * Kezdetben aktívra teszi
     * Ha aktív: letárolja a lehetséges értékeket, majd megjeleníti
     * Ha inaktív: segítség céljából lévő gombokat letiltja
     */
    private void addCheckboxListener() {
        possibleValuesCheckbox.setSelected(true);
        possibleValuesCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            getHelpButton().setDisable(!newValue);
            getRecalculateButton().setDisable(!newValue);
            getResetButton().setDisable(!newValue);
            model.storePossibleValues();
            togglePossibleValuesDisplay(newValue);
        });
    }

    /**
     * Megjeleníti, vagy eltünteti a képernyőről a lehetséges értékeket.
     *
     * @param show ez mutatja, hogy látható lesz e vagy sem
     */
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

    /**
     * Frissíti a cella stílusát attól függően, hogy látható lesz e vagy sem.
     *
     * @param show ez mutatja, hogy látható lesz e vagy sem
     * @param row  a cella sor indexe
     * @param col  a cella oszlop indexe
     * @return visszatér igazzal, ha valami probléma van
     */
    private boolean updateTextAreaBasedOnVisibility(boolean show, int row, int col) {
        TextArea textArea = textAreas[row][col];
        if (isNotInitialNumber(textArea) && textArea.getStyleClass().contains(POSSIBLE_VALUES)) {
            if (show) {
                Set<Integer> possibleValues = model.getPossibleValuesAt(row, col);
                String possibleValuesText = formatNumbers(possibleValues);
                textArea.setText(possibleValuesText);
                boolean hasError = false;
                if (possibleValues.isEmpty()) {
                    addStyleToTextArea(textArea, ERROR);
                    hasError = true;
                } else {
                    textArea.getStyleClass().remove(ERROR);
                }
                addStyleToTextArea(textArea, POSSIBLE_VALUES);
                return hasError;
            } else {
                clearTextArea(textArea);
            }
        }
        return false;
    }

    /**
     * Kirörli a szöveget a cellából és eltávolítja az 'error' stílust.
     *
     * @param textArea a cella amelyet kezelünk
     */
    private void clearTextArea(TextArea textArea) {
        textArea.setText("");
        textArea.getStyleClass().remove(ERROR);
    }

    /**
     * Hozzáadja a cellához a megadott stílust.
     *
     * @param textArea a cella amelyet módosítunk
     * @param style    amit rárakunk a cellára
     */
    private void addStyleToTextArea(TextArea textArea, String style) {
        if (!textArea.getStyleClass().contains(style)) {
            textArea.getStyleClass().add(style);
        }
    }

    /**
     * Meghívja azt a metódust amely elmenti a játékot.
     */
    @FXML
    private void saveGame() {
        long elapsedTimeSeconds = (long) time.toSeconds();
        GameHistory gameHistory = new GameHistory(model.getOriginalBoard(), model.getSolvedBoard(), elapsedTimeSeconds, GameModel.getDifficulty().toString());
        GameHistoryService.saveGameHistory(gameHistory);
    }

    /**
     * Megjelenít egy felugró ablakot a megadott cím, és szöveg alapján.
     *
     * @param title   ez lesz a címe az ablaknak
     * @param message ez a szöveg fog megjelenni az ablakon
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/sudoku-style.css")).toExternalForm());
        dialogPane.getStyleClass().add("myDialog");

        alert.showAndWait();
    }

    /**
     * Ellenörzi a táblát, hogy milyen állapotban van.
     * Készen van-e vagy sem
     * Hibás vagy sem
     */
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
            Set<Pair<Integer, Integer>> incorrectValues = model.getIncorrectValues();
            if (incorrectValues.isEmpty()) {
                showAlert("Info", "Eddig nincs hiba!");
            } else {
                setTextAreasIncorrectValues(incorrectValues);
            }
        }
    }

    /**
     * Beállítja a cella tulajdonságait.
     * Ha egy alapból a táblában szereplő cella: nem szerkeszthető
     * Ha nem szerepel alapból a táblában: beállítja mit fogadjon el
     *
     * @param textArea a cella amelyet szerkesztünk
     * @param row      cella sora
     * @param col      cella oszlopa
     */
    private void setupTextArea(TextArea textArea, int row, int col) {
        if (isNotInitialNumber(textArea)) {
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

    /**
     * Itt állítjuk, hogy mi történjen ha elveszti a fókuszt a cella.
     * Ha üres: visszaállítja az eredetire
     * Ha egy érték: feldolgozza mint érték
     * Ha több: feldolgozza mint lehetséges érték
     *
     * @param textArea a cella amelyet szerkesztünk
     * @param row      cella sora
     * @param col      cella oszlopa
     */
    private void handleCellFocusLost(TextArea textArea, int row, int col) {
        String text = textArea.getText().trim();
        if (isNotInitialNumber(textArea)) {
            if (text.isEmpty()) {
                revertTextAreaToModelValues(textArea, row, col);
            } else if (text.matches("\\d")) {
                processSingleDigit(textArea, row, col, text);
            } else {
                processPossibleValues(textArea, row, col, text);
            }
        }
    }

    /**
     * Visszaadja, hogy kezdetben megmutatott számról(celláról) van-e szó vagy sem.
     *
     * @param textArea cella amelyet vizsgálunk
     * @return igazzal tér vissza ha nem előre beírt celláról van szó
     */
    private boolean isNotInitialNumber(TextArea textArea) {
        return !textArea.getStyleClass().contains(INITIAL_NUMBER);
    }

    /**
     * Feldolgozza az egy értékű cellát.
     * Frissíti a cella kinézetét.
     * Ha nem a helyes érték szerepel benne, akkor hibát dob
     * Ha helyes, akkor beállítja a cella értékeként
     *
     * @param textArea a cella amelyet szerkesztünk
     * @param row      cella sora
     * @param col      cella oszlopa
     * @param text     a szám amely benne szerepel
     */
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

    /**
     * Feldolgozza a több értékű cellát.
     * Egy Set-ben tárolja ezeket, így kiszürve az azonosokat
     * Ha egy elemű lesz a Set- akkor visszaállítja az eredetit
     * Ha validak az értékek akkor frissíti a cellát
     *
     * @param textArea a cella amelyet szerkesztünk
     * @param row      cella sora
     * @param col      cella oszlopa
     * @param text     a számok amely benne szerepel
     */
    private void processPossibleValues(TextArea textArea, int row, int col, String text) {
        textArea.getStyleClass().remove(ERROR);
        Set<Integer> newPossibleValues = extractNumbers(text);
        if (newPossibleValues.size() == 1) {
            revertTextAreaToModelValues(textArea, row, col);
        } else {
            if (isValid(newPossibleValues)) {
                updateTextAreaWithPossibleValues(textArea, row, col, newPossibleValues);
            } else {
                revertTextAreaToModelValues(textArea, row, col);
            }
        }
    }

    /**
     * Megvizsgálja, hogy valós a beírt lehetséges értékek.
     * Valós: 1-9 közé esik
     *
     * @param possibleValues lehetséges értékek
     * @return igazzal tér vissza ha 1-9 közé esik
     */
    private boolean isValid(Set<Integer> possibleValues) {
        return possibleValues.stream().allMatch(num -> num >= 1 && num <= 9);
    }

    /**
     * Meghívja azt a metódust, amely beállítja a lehetséges értékeket.
     * Meghívja azt amely frissíti a cella kinézetét
     *
     * @param textArea          a cella amelyet szerkesztünk
     * @param row               cella sora
     * @param col               cella oszlopa
     * @param newPossibleValues új lehetséges értékek halmaza
     */
    private void updateTextAreaWithPossibleValues(TextArea textArea, int row, int col, Set<Integer> newPossibleValues) {
        model.setPossibleValuesAt(row, col, newPossibleValues);
        updateTextArea(textArea, formatNumbers(newPossibleValues), true);
    }

    /**
     * Visszaállítja a cella lehetséges értékeit a modelből dolgozva.
     *
     * @param textArea a cella amelyet szerkesztünk
     * @param row      cella sora
     * @param col      cella oszlopa
     */
    private void revertTextAreaToModelValues(TextArea textArea, int row, int col) {
        textArea.getStyleClass().remove(ERROR);
        model.setValueAt(row, col, 0);
        Set<Integer> possibleValues = model.getNewPossibleValues(row, col);

        if (possibleValuesCheckbox.isSelected()) {
            String text = formatNumbers(possibleValues);
            model.setPossibleValuesAt(row, col, possibleValues);
            updateTextArea(textArea, text, !possibleValues.isEmpty());
        } else {
            textArea.getStyleClass().add(POSSIBLE_VALUES); //KELL: Hiba esetén kitörölve a cellát, majd megmutatni a lehetséges értékeket -> nem jelenik meg a possible values
        }
    }

    /**
     * Frissíti a cella stílusát.
     *
     * @param textArea               a cella amelyet szerkesztünk
     * @param text                   ez lesz a cella szövege
     * @param addPossibleValuesClass lehetséges érték stílusáról van-e szó vagy sem
     */
    private void updateTextArea(TextArea textArea, String text, boolean addPossibleValuesClass) {
        textArea.setText(text);
        if (addPossibleValuesClass) {
            addStyleToTextArea(textArea, POSSIBLE_VALUES);
        } else {
            textArea.getStyleClass().remove(POSSIBLE_VALUES);
            textArea.getStyleClass().remove(HINT);
        }
    }

    /**
     * Szövegként kapott értékeket, egy elemű számként adja vissza.
     *
     * @param text lehetséges értékek a cellában
     * @return halmazként visszaadja a lehetséges értékeket
     */
    private Set<Integer> extractNumbers(String text) {
        Set<Integer> numbers = new HashSet<>();
        String[] parts = text.split("[,\\s]+");
        for (String part : parts) {
            int number = Integer.parseInt(part);
            numbers.add(number);
        }
        return numbers;
    }

    /**
     * Megformázza a lehetséges értékeket.
     *
     * @param numbers lehetséges értékek
     * @return formázott szövegként visszaadja a számokat
     */
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


    /**
     * A cella szegélyeinek stílusát határozza meg.
     *
     * @param row cella sora
     * @param col cella oszlopa
     * @return listaként visszaadja a stílusokat
     */
    private List<String> determineBorderStyles(int row, int col) {
        List<String> styles = new ArrayList<>();
        styles.add("sudoku-text-area");

        addSpecialBorderStyle(styles, row, col);
        addGeneralBorderStyle(styles, row, col);

        return styles;
    }

    /**
     * Speciális esetekben határozza meg a cella szegély stílusát.
     *
     * @param styles eddigi stílusok
     * @param row    cella sora
     * @param col    cella oszlopa
     */
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

    /**
     * Alap esetekben határozza meg a cella szegély stílusát.
     *
     * @param styles eddigi stílusok
     * @param row    cella sora
     * @param col    cella oszlopa
     */
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

    /**
     * Megvizsgálja, hogy a cella sora 2, 5 vagy 8.
     *
     * @param row cella sora
     * @return igazzal tér vissza ha igen
     */
    private boolean isThickBorderRow(int row) {
        return row == 2 || row == 5 || row == 8;
    }

    /**
     * Megvizsgálja, hogy a cella oszlopa 2, 5 vagy 8.
     *
     * @param col cella oszlopa
     * @return igazzal tér vissza ha igen
     */
    private boolean isThickBorderCol(int col) {
        return col == 2 || col == 5 || col == 8;
    }

    /**
     * Meghívja a megoldó algortimust.
     * Frissíti a kinézetet
     * Leállítja a stoppert
     */
    @FXML
    private void solveSudoku() {
        model.solve();
        updateViewWithSudokuBoard(model.getSolvedBoard());
        timeline.stop();
    }

    /**
     * Frissíti a teljes táblát.
     *
     * @param sudokuBoard megadott tábla állapota
     */
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

    /**
     * Frissíti a cella stílusát attól függően, hogy van-e megadott érték vagy sem.
     *
     * @param textArea       cella amelyet szerkesztünk
     * @param value          megadott érték
     * @param possibleValues lehetséges értékek
     */
    private void updateTextAreaStyles(TextArea textArea, int value, Set<Integer> possibleValues) {
        if (value != 0) {
            textArea.getStyleClass().remove(POSSIBLE_VALUES);
        } else if (!possibleValues.isEmpty()) {
            addStyleToTextArea(textArea, POSSIBLE_VALUES);
        }
    }

    /**
     * Elmenteti az aktuális lehetséges értékeket, majd frissíti a kinézetet.
     */
    @FXML
    private void updatePossibleValues() {
        model.storeActualPossibleValues();
        togglePossibleValuesDisplay(true);
    }

    /**
     * Elmenti az eredeti lehetséges értékeket, amjd frissítni a kinézetet.
     */
    @FXML
    void resetPossibleValues() {
        model.storePossibleValues();
        togglePossibleValuesDisplay(true);
    }

    /**
     * Megjeleníti a főmenü ablakot.
     *
     * @param event esemény, mely kiváltja a metódus hívását, tárolja az adatokat
     * @throws IOException kivételt dob, ha hiba történne a képernyő betöltése során
     */
    @FXML
    private void backToMainMenu(ActionEvent event) throws IOException {
        Parent gameView = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/StarterView.fxml")));
        Scene gameScene = new Scene(gameView);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(gameScene);
        window.show();
    }

    /**
     * Visszaállítja a táblát az eredeti állapotba.
     */
    @FXML
    private void resetBoard() {
        model.resetBoard();
        updateViewWithSudokuBoard(model.getOriginalBoard());
        setEditingEnabled(true);
        togglePossibleValuesDisplay(possibleValuesCheckbox.isSelected());
        startTimer();
        updateHelpCounterDisplay();
        needMoreHelp = false;
        singleHelpSet = new HashSet<>();
        pairsType = null;
    }

    /**
     * A cellák szerkeszthetőségét állítja.
     *
     * @param enabled true ha engedályezi, false ha nem
     */
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

    /**
     * Megvizsgálja, hogy segítség után lett-e hiba a cellákban vagy sem.
     */
    private void validateBoardAfterHelp() {
        boolean conflictFound = false;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int cellValue = model.getValueAt(row, col);
                if (cellValue != 0 && !model.isValueValid(row, col, cellValue)) {
                    conflictFound = true;
                    textAreas[row][col].getStyleClass().add(ERROR);
                }
            }
        }
        if (conflictFound) {
            showAlert("Hiba", "A segítségnyújtás után konfliktus alakult ki a táblán.");
        }
    }

    /**
     * A rossz helyen szereplő számokra 'error' stílust tesz.
     *
     * @param incorrectValues értékek pozíciója, amelyek rossz cellában vannak
     */
    private void setTextAreasIncorrectValues(Set<Pair<Integer, Integer>> incorrectValues) {
        showAlert("Hiba", "Sajnos hiba van a táblában!");
        for (Pair<Integer, Integer> position : incorrectValues) {
            TextArea textArea = textAreas[position.getKey()][position.getValue()];
            addStyleToTextArea(textArea, ERROR);
        }
    }

    /**
     * Meghatározza, hogy az aktuális állapotban melyik segítő algoritmus tud segíteni először.
     * Ha megvan, akkor ezekre 'hint' stílust tesz
     */
    private void applyStrategyforHelp() {
        needMoreHelp = true;
        updatePossibleValues();

        if (!singleHelpSet.addAll(model.checkFullHouse()) && (!singleHelpSet.addAll(model.checkNakedSingles())) && (!singleHelpSet.addAll(model.checkHiddenSingles()))) {
            pairsType = model.checkNakedPairs();
            if (pairsType == null || pairsType.getRemoveSet().isEmpty()) {
                pairsType = model.checkHiddenPairs();
            }
        }
        if (!singleHelpSet.isEmpty()) {
            Set<Pair<Integer, Integer>> simplifiedSet = singleHelpSet.stream().map(Pair::getValue).collect(Collectors.toSet());
            applyStyleToCells(simplifiedSet);
        } else if (pairsType != null && !pairsType.getRemoveSet().isEmpty()) {
            applyStyleToCells(pairsType.getPairsPositionSet());
        } else {
            needMoreHelp = false;
            if (!model.isComplete()) {
                showAlert("Info", "Megmutatunk egy véletlenszerű cellát.");
                revealRandomCell();
            }
        }
    }

    /**
     * Alkalmazza a kiválasztott segítő algoritmust.
     */
    @FXML
    private void helpStrategy() {
        model.increaseHelpCounter();
        updateHelpCounterDisplay();

        Set<Pair<Integer, Integer>> incorrectValues = model.getIncorrectValues();
        if (!incorrectValues.isEmpty()) {
            setTextAreasIncorrectValues(incorrectValues);
            return;
        }

        if (!needMoreHelp) {
            applyStrategyforHelp();
        } else {
            needMoreHelp = false;
            if (!singleHelpSet.isEmpty()) {
                for (Pair<Integer, Pair<Integer, Integer>> hint : singleHelpSet) {
                    int value = hint.getKey();
                    Pair<Integer, Integer> position = hint.getValue();
                    int row = position.getKey();
                    int col = position.getValue();

                    TextArea textArea = textAreas[row][col];
                    model.setValueAt(row, col, value);
                    textArea.setText(String.valueOf(value));
                    textArea.getStyleClass().remove(POSSIBLE_VALUES);
                    textArea.getStyleClass().remove(HINT);
                }
                validateBoardAfterHelp();
                singleHelpSet.clear();
            } else {
                Set<Pair<Pair<Integer, Integer>, Set<Integer>>> removeSet = pairsType.getRemoveSet();
                Set<Pair<Integer, Integer>> nakedPairsPositionSet = pairsType.getPairsPositionSet();

                model.addCheckedPairSet(removeSet);

                for (Pair<Pair<Integer, Integer>, Set<Integer>> removeEntry : removeSet) {
                    Pair<Integer, Integer> position = removeEntry.getKey();
                    Set<Integer> valuesToRemove = removeEntry.getValue();

                    int row = position.getKey();
                    int col = position.getValue();

                    model.removePossibleValuesAt(row, col, valuesToRemove);

                    togglePossibleValuesDisplay(true);
                }
                for (Pair<Integer, Integer> pair : nakedPairsPositionSet) {
                    int row = pair.getKey();
                    int col = pair.getValue();
                    textAreas[row][col].getStyleClass().remove(HINT);
                }
                pairsType = new PairsType();
            }
        }
    }

    /**
     * Frissíti a lekért segítségek számlálót.
     */
    private void updateHelpCounterDisplay() {
        helpCounter.setText(String.valueOf(model.getHelpCounter()));
    }

    /**
     * A megadott cellákra 'hint' stílust rak.
     *
     * @param cells cellák pozíciója, emelyekre stílust akarunk rakni
     */
    private void applyStyleToCells(Set<Pair<Integer, Integer>> cells) {
        for (Pair<Integer, Integer> cell : cells) {
            int row = cell.getKey();
            int col = cell.getValue();

            TextArea textArea = textAreas[row][col];
            addStyleToTextArea(textArea, HINT);
        }
    }

    /**
     * Megjelenít egy random cellát.
     */
    private void revealRandomCell() {
        List<Pair<Integer, Integer>> emptyCells = new ArrayList<>();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (model.getSudokuBoard()[row][col].getValue() == 0) {
                    emptyCells.add(new Pair<>(row, col));
                }
            }
        }

        Collections.shuffle(emptyCells);
        Pair<Integer, Integer> chosenCell = emptyCells.get(0);
        int row = chosenCell.getKey();
        int col = chosenCell.getValue();
        int correctValue = model.getSolvedBoard()[row][col].getValue();

        model.setValueAt(row, col, correctValue);
        TextArea textArea = textAreas[row][col];
        textArea.setText(String.valueOf(correctValue));
        textArea.getStyleClass().remove(POSSIBLE_VALUES);
        textArea.getStyleClass().remove(HINT);
    }

    /**
     * Visszaadja a 'segítség' gombot.
     * @return 'segítség' gomb
     */
    private Button getHelpButton() {
        return helpButton;
    }

    /**
     * Visszaadja a 'lehetséges értékek újra számolása' gombot.
     * @return 'lehetséges értékek újra számolása' gomb
     */
    private Button getRecalculateButton() {
        return recalculateButton;
    }

    /**
     * Visszaadja a 'lehetséges értékek alaphelyzetbe állítása' gombot.
     * @return 'lehetséges értékek alaphelyzetbe állítása'
     */
    private Button getResetButton() {
        return resetButton;
    }
}
