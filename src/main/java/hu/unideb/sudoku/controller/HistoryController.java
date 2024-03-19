package hu.unideb.sudoku.controller;

import hu.unideb.sudoku.model.GameHistory;
import hu.unideb.sudoku.model.GameHistoryService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;


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
    public void initialize() {
        timeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getElapsedTimeFormatted()));

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

        replayColumn.setCellFactory(col -> {
            return new TableCell<>() {
                private final Button btn = new Button("Újra játszás");

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
            };
        });

        loadHistories();
    }

    private void replayGame(GameHistory history) {
        // Megvalósítandó: a játéktörténet alapján a játék újratöltése
    }

    private void loadHistories() {
        List<GameHistory> histories = GameHistoryService.loadGameHistories();
        if (histories != null) {
            historyTable.getItems().setAll(histories);
        }
    }
}
