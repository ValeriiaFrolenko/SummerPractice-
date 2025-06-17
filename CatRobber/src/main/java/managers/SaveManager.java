package managers;

import entities.*;
import interfaces.GameObject;
import interfaces.Interactable;
import interfaces.Savable;
import org.json.JSONArray;
import org.json.JSONObject;
import puzzles.CodeLockPuzzle;
import puzzles.LaserLockPuzzle;
import puzzles.LockPickPuzzle;
import puzzles.Puzzle;
import ui.ShopItem;
import utils.GameLoader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Клас для управління збереженням і завантаженням стану гри.
 */
public class SaveManager {
    /** Директорія для зберігання файлів збереження. */
    private String saveDirectory;

    /** Завантажувач JSON-файлів і ресурсів гри. */
    private GameLoader gameLoader;

    /** Назва файлу для збереження прогресу гри. */
    private static final String PROGRESS_FILE = "game_progress.json";

    /**
     * Конструктор для ініціалізації менеджера збереження.
     */
    public SaveManager() {
        saveDirectory = "data/saves/";
        gameLoader = new GameLoader();
        File dir = new File(saveDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Зберігає поточний стан гри у файл.
     *
     * @param gameState поточний стан гри
     */
    public void saveGame(GameManager.GameState gameState) {
        JSONObject saveData = new JSONObject();
        saveData.put("completedLevels", GameManager.getInstance().getCompletedLevels());
        saveData.put("currentLevelId", GameManager.getInstance().getCurrentLevelId());
        saveData.put("totalMoney", GameManager.getInstance().getTotalMoney());
        saveData.put("gameState", gameState.toString());
        saveData.put("code", GameManager.getInstance().getCode());
        saveData.put("temporaryMoney", GameManager.getInstance().getTemporaryMoney());
        savePlayer(GameManager.getInstance().getPlayer());
        savePolice(GameManager.getInstance().getPolice());
        saveCameras(GameManager.getInstance().getCameras());
        saveDoors(GameManager.getInstance().getDoors());
        saveInteractiveObjects(GameManager.getInstance().getInteractables());
        savePuzzles(GameManager.getInstance().getPuzzles());
        saveJSON(saveData, saveDirectory + PROGRESS_FILE);
    }

    /**
     * Зберігає дані гравця у файл.
     *
     * @param player об’єкт гравця
     */
    public void savePlayer(Player player) {
        if (player == null) return;
        JSONObject data = new JSONObject();
        data.put("player_0", player.getSerializableData());
        saveJSON(data, saveDirectory + "player_current.json");
    }

    /**
     * Зберігає дані поліцейських у файл.
     *
     * @param police список поліцейських
     */
    public void savePolice(List<Police> police) {
        JSONObject data = new JSONObject();
        for (int i = 0; i < police.size(); i++) {
            data.put("police_" + i, police.get(i).getSerializableData());
        }
        saveJSON(data, saveDirectory + "police_current.json");
    }

    /**
     * Зберігає дані камер спостереження у файл.
     *
     * @param cameras список камер
     */
    public void saveCameras(List<SecurityCamera> cameras) {
        JSONObject data = new JSONObject();
        for (int i = 0; i < cameras.size(); i++) {
            data.put("camera_" + i, cameras.get(i).getSerializableData());
        }
        saveJSON(data, saveDirectory + "cameras_current.json");
    }

    /**
     * Зберігає дані дверей у файл.
     *
     * @param doors список дверей
     */
    public void saveDoors(List<Door> doors) {
        JSONObject data = new JSONObject();
        for (int i = 0; i < doors.size(); i++) {
            data.put("door_" + i, doors.get(i).getSerializableData());
        }
        saveJSON(data, saveDirectory + "doors_current.json");
    }

    /**
     * Зберігає дані інтерактивних об’єктів у файл.
     *
     * @param interactables список інтерактивних об’єктів
     */
    public void saveInteractiveObjects(List<Interactable> interactables) {
        JSONObject data = new JSONObject();
        int index = 0;
        for (Interactable interactable : interactables) {
            if (interactable instanceof InteractiveObject && interactable instanceof Savable) {
                data.put("interactiveObjects_" + index, ((Savable) interactable).getSerializableData());
                index++;
            }
        }
        saveJSON(data, saveDirectory + "interactiveObjects_current.json");
    }

    /**
     * Зберігає дані головоломок у файл.
     *
     * @param puzzles список головоломок
     */
    public void savePuzzles(List<Puzzle> puzzles) {
        JSONObject data = new JSONObject();
        for (int i = 0; i < puzzles.size(); i++) {
            Puzzle puzzle = puzzles.get(i);
            JSONObject puzzleData = puzzle.getSerializableData();
            puzzleData.put("puzzleType", getPuzzleType(puzzle));
            puzzleData.put("type", "Puzzle");
            data.put("puzzle_" + i, puzzleData);
        }
        saveJSON(data, saveDirectory + "puzzles_current.json");
    }

    /**
     * Визначає тип головоломки для збереження.
     *
     * @param puzzle об’єкт головоломки
     * @return рядок із типом головоломки
     */
    private String getPuzzleType(Puzzle puzzle) {
        if (puzzle instanceof CodeLockPuzzle) return "CodeLockPuzzle";
        if (puzzle instanceof LockPickPuzzle) return "LockPickPuzzle";
        if (puzzle instanceof LaserLockPuzzle) return "LaserLockPuzzle";
        return "UNKNOWN";
    }

    /**
     * Зберігає JSON-дані у вказаний файл.
     *
     * @param data JSON-об’єкт для збереження
     * @param filename шлях до файлу
     */
    private void saveJSON(JSONObject data, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(data.toString(2));
        } catch (IOException e) {
            System.err.println("Не можу зберегти файл: " + filename);
        }
    }

    /**
     * Завантажує стан гри з файлу.
     *
     * @param filename шлях до файлу збереження
     */
    public void loadGame(String filename) {
        JSONObject saveData = gameLoader.loadJSON(filename);
        if (saveData == null) {
            System.err.println("Збереження не знайдено: " + filename);
            return;
        }
        List<GameObject> objects = new ArrayList<>();
        String basePath = saveDirectory;
        JSONObject playerData = gameLoader.loadJSON(basePath + "player_current.json");
        if (playerData != null) {
            objects.addAll(gameLoader.parseTiledJSON(playerData));
        }
        JSONObject policeData = gameLoader.loadJSON(basePath + "police_current.json");
        if (policeData != null) {
            objects.addAll(gameLoader.parseTiledJSON(policeData));
        }
        JSONObject camerasData = gameLoader.loadJSON(basePath + "cameras_current.json");
        if (camerasData != null) {
            objects.addAll(gameLoader.parseTiledJSON(camerasData));
        }
        JSONObject doorsData = gameLoader.loadJSON(basePath + "doors_current.json");
        if (doorsData != null) {
            objects.addAll(gameLoader.parseTiledJSON(doorsData));
        }
        JSONObject interactiveObjectsData = gameLoader.loadJSON(basePath + "interactiveObjects_current.json");
        if (interactiveObjectsData != null) {
            objects.addAll(gameLoader.parseTiledJSON(interactiveObjectsData));
        }
        JSONObject puzzlesData = gameLoader.loadJSON(basePath + "puzzles_current.json");
        if (puzzlesData != null) {
            for (String key : puzzlesData.keySet()) {
                JSONObject puzzleObj = puzzlesData.getJSONObject(key);
                Puzzle puzzle = gameLoader.createSinglePuzzle(puzzleObj);
                if (puzzle != null) {
                    GameManager.getInstance().getPuzzles().add(puzzle);
                }
            }
        }
        GameManager.getInstance().setGameObjects(objects);
        GameManager.getInstance().setFromData(saveData);
        GameManager.getInstance().setCurrentLevelId(saveData.getInt("currentLevelId"));
    }

}