package hu.unideb.sudoku.model;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.Type;

public class GameHistoryService {
    private static final String FILE_PATH = "src/main/resources/json/sudoku_history.json";
    private static final int MAX_HISTORY_SIZE = 5;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
                e.printStackTrace();
            }
        }

        histories.add(gameHistory);

        if (histories.size() > MAX_HISTORY_SIZE) {
            histories.remove(0);
        }

        try (Writer writer = Files.newBufferedWriter(path)) {
            gson.toJson(histories, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<GameHistory> loadGameHistories() {
        try {
            Path path = Paths.get(FILE_PATH).toAbsolutePath();
            if (Files.exists(path)) {
                String content = new String(Files.readAllBytes(path));
                Type gameHistoryListType = new TypeToken<List<GameHistory>>(){}.getType();
                return gson.fromJson(content, gameHistoryListType);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}