package hu.unideb.sudoku.controller;

import hu.unideb.sudoku.model.GameHistory;
import hu.unideb.sudoku.model.GameHistoryService;
import hu.unideb.sudoku.model.GameModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
            private final Button btn = new Button("Újra játszás");

            // Inicializáló blokk
            {
                btn.setOnAction(event -> {
                    GameHistory data = getTableView().getItems().get(getIndex());
                    replayGame(data);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });


        loadHistories();
    }

    @FXML
    private void replayGame(GameHistory history) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("GameView.fxml"));
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

    private void loadHistories() {
        List<GameHistory> histories = GameHistoryService.loadGameHistories();
        if (histories != null) {
            historyTable.getItems().setAll(histories);
        }
    }
}
