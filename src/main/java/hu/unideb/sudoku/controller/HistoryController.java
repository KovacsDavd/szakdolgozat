package hu.unideb.sudoku.controller;

import hu.unideb.sudoku.model.GameHistory;
import hu.unideb.sudoku.model.GameHistoryService;
import hu.unideb.sudoku.model.GameModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Mentések kontrollerje.
 */
public class HistoryController {

    @FXML
    private TableView<GameHistory> historyTable;

    @FXML
    private TableColumn<GameHistory, String> indexColumn;

    @FXML
    private TableColumn<GameHistory, String> timeColumn;

    @FXML
    private TableColumn<GameHistory, String> replayColumn;

    @FXML
    private TableColumn<GameHistory, String> difficultyColumn;

    /**
     * Inicializálja az oszlopokat.
     */
    @FXML
    public void initialize() {
        timeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getElapsedTimeFormatted()));
        difficultyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDifficulty()));

        indexColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        replayColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = createReplayButton();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    GameHistory data = getTableView().getItems().get(getIndex());
                    btn.setOnAction(event -> replayGame(data));
                    setGraphic(btn);
                }
            }
        });

        loadHistories();
    }

    /**
     * Létrehozza az újra játszás gombot, és rárakja a stílust.
     *
     * @return visszaadja az elkészült gombot
     */
    private Button createReplayButton() {
        Button btn = new Button("Újra");
        btn.getStyleClass().add("button-common");

        return btn;
    }

    /**
     * Betölti a játék kinézetet a megadott adatok alapján.
     *
     * @param history tárolja az adatokat, amelykből dolgozva újra töltjük a táblát
     */
    @FXML
    private void replayGame(GameHistory history) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/GameView.fxml"));
            GameModel.setNeedHistoryLoad(true);
            Parent gameView = loader.load();

            GameController gameController = loader.getController();
            gameController.initializeWithHistory(history);

            Stage stage = (Stage) historyTable.getScene().getWindow();
            stage.setScene(new Scene(gameView));
            stage.show();
        } catch (IOException e) {
            Logger.debug("Failed to load the game view", e);
        }
    }

    /**
     * Json fileból betölti az adatokat.
     */
    private void loadHistories() {
        List<GameHistory> histories = GameHistoryService.loadGameHistories();
        if (histories != null) {
            historyTable.getItems().setAll(histories);
        }
    }

    /**
     * Betölti a főmenü ablakot.
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
}
