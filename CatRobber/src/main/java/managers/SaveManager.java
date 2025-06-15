package managers;

import entities.*;
import interfaces.GameObject;
import interfaces.Interactable;
import interfaces.Savable;
import org.json.JSONObject;
import puzzles.CodeLockPuzzle;
import puzzles.LaserLockPuzzle;
import puzzles.LockPickPuzzle;
import puzzles.Puzzle;
import utils.GameLoader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Керує збереженням і завантаженням стану гри
public class SaveManager {
    // Поля
    private String saveDirectory; // Директорія для збережень (/data/saves/)
    private GameLoader gameLoader; // Завантажувач JSON і ресурсів
    private static final String PROGRESS_FILE = "game_progress.json";

    // Конструктор: ініціалізує менеджер збереження
    public SaveManager() {
        saveDirectory = "data/saves/";
        gameLoader = new GameLoader();
        File dir = new File(saveDirectory);
        if (!dir.exists()) {
            dir.mkdirs(); // Створюємо директорію, якщо вона не існує
        }
    }

    // --- Збереження ---

    // Зберігає стан гри (викликається з GameManager.saveGame())
    public void saveGame(GameManager.GameState gameState) {
        JSONObject saveData = new JSONObject();
        // Основні дані гри
        saveData.put("completedLevels", new ArrayList<Integer>());
        saveData.put("currentLevelId", GameManager.getInstance().getCurrentLevelId());
        saveData.put("totalMoney", GameManager.getInstance().getTotalMoney());
        saveData.put("gameState", gameState.toString());
        saveData.put("code", GameManager.getInstance().getCode());

        // Збереження об'єктів гри
        savePlayer(GameManager.getInstance().getPlayer());
        savePolice(GameManager.getInstance().getPolice());
        saveCameras(GameManager.getInstance().getCameras());
        saveDoors(GameManager.getInstance().getDoors());
        saveInteractiveObjects(GameManager.getInstance().getInteractables());
        savePuzzles(GameManager.getInstance().getPuzzles());

        // Збереження всіх даних в один файл
        saveJSON(saveData, saveDirectory + PROGRESS_FILE);
    }

    // Зберігає гравця
    public void savePlayer(Player player) {
        if (player == null) return;
        JSONObject data = new JSONObject();
        data.put("player_0", player.getSerializableData());
        saveJSON(data, saveDirectory + "player_current.json");
    }

    // Зберігає поліцейських
    public void savePolice(List<Police> police) {
        JSONObject data = new JSONObject();
        for (int i = 0; i < police.size(); i++) {
            data.put("police_" + i, police.get(i).getSerializableData());
        }
        saveJSON(data, saveDirectory + "police_current.json");
    }

    // Зберігає камери
    public void saveCameras(List<SecurityCamera> cameras) {
        JSONObject data = new JSONObject();
        for (int i = 0; i < cameras.size(); i++) {
            data.put("camera_" + i, cameras.get(i).getSerializableData());
        }
        saveJSON(data, saveDirectory + "cameras_current.json");
    }

    public void saveDoors(List<Door> doors) {
        JSONObject data = new JSONObject();
        for (int i = 0; i < doors.size(); i++) {
            data.put("door_" + i, doors.get(i).getSerializableData());
        }
        saveJSON(data, saveDirectory + "doors_current.json");
    }

    // Зберігає інтерактивні об’єкти
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

    // Зберігає головоломки
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

    private String getPuzzleType(Puzzle puzzle) {
        if (puzzle instanceof CodeLockPuzzle) return "CodeLockPuzzle";
        if (puzzle instanceof LockPickPuzzle) return "LockPickPuzzle";
        if (puzzle instanceof LaserLockPuzzle) return "LaserLockPuzzle";
        return "UNKNOWN";
    }

    // Зберігає JSON у файл
    private void saveJSON(JSONObject data, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(data.toString(2));
        } catch (IOException e) {
            System.out.println("Не можу зберегти файл: " + filename);
        }
    }

    // --- Завантаження ---

    // Завантажує гру з файлу (викликається з LevelManager.loadFromSave())
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

    // --- Геттери ---

    // Повертає список файлів збереження для меню
    public List<String> getSaveFiles() {
        List<String> filenames = new ArrayList<>();
        File progressFile = new File(saveDirectory + PROGRESS_FILE);
        if (progressFile.exists()) {
            filenames.add(saveDirectory + PROGRESS_FILE);
        }
        return filenames;
    }
}