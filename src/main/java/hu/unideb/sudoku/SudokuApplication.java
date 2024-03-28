package hu.unideb.sudoku;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * A Sudoku alkalmazás JavaFX alkalmazás osztálya.
 * Ez az osztály felelős a Sudoku játék grafikus felületének inicializálásáért és megjelenítéséért
 */
public class SudokuApplication extends Application {
    /**
     * A JavaFX alkalmazás belépési pontja.
     * Ez a metódus hozza létre és jeleníti meg a kezdőképernyőt az alkalmazás indításakor.
     *
     * @param stage ablak (színpad), amelyre a felhasználói felület kerül.
     * @throws IOException kivételt dob, ha hiba történne a képernyő betöltése során
     */
    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/StarterView.fxml")));
        stage.setTitle("Sudoku");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
