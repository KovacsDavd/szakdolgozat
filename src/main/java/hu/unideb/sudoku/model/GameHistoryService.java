package hu.unideb.sudoku.model;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.tinylog.Logger;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameHistoryService {
    private static final String FILE_PATH = "src/main/resources/json/sudoku_history.json";
    private static final int MAX_HISTORY_SIZE = 5;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private GameHistoryService() {
    }

    public static void saveGameHistory(GameHistory gameHistory) {
        List<GameHistory> histories = new ArrayList<>();

        Path path = Paths.get(FILE_PATH).toAbsolutePath();
        if (Files.exists(path)) {
            try {
                String content = new String(Files.readAllBytes(path));
                if (!content.isEmpty()) {
                    JsonArray jsonArray = JsonParser.parseString(content).getAsJsonArray();
                    for (JsonElement element : jsonArray) {
                        histories.add(gson.fromJson(element, GameHistory.class));
                    }
                }
            } catch (IOException e) {
                Logger.debug("Nem tudtuk beolvasni a json file-t!", e);
            }
        }

        histories.add(gameHistory);

        if (histories.size() > MAX_HISTORY_SIZE) {
            histories.remove(0);
        }

        try (Writer writer = Files.newBufferedWriter(path)) {
            gson.toJson(histories, writer);
        } catch (IOException e) {
            Logger.debug("Nem tudtuk file-ba írni!", e);
        }
    }

    public static List<GameHistory> loadGameHistories() {
        try {
            Path path = Paths.get(FILE_PATH).toAbsolutePath();
            if (Files.exists(path)) {
                String content = new String(Files.readAllBytes(path));
                Type gameHistoryListType = new TypeToken<List<GameHistory>>() {
                }.getType();
                return gson.fromJson(content, gameHistoryListType);
            }
        } catch (IOException e) {
            Logger.debug("Nem tudtuk beolvasni a json file-t", e);
        }
        return Collections.emptyList();
    }
}
