package managers;

import entities.InteractiveObject;
import entities.Player;
import entities.Police;
import entities.SecurityCamera;
import interfaces.GameObject;
import interfaces.Interactable;
import interfaces.Savable;
import org.json.JSONObject;
import puzzles.CodeLockPuzzle;
import puzzles.LaserLockPuzzle;
import puzzles.LockPickPuzzle;
import puzzles.Puzzle;
import utils.GameLoader;
import utils.SaveFile;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// Керує збереженням і завантаженням стану гри
public class SaveManager {
    // Поля
    private List<SaveFile> saveFiles; // Список файлів збереження
    private String saveDirectory; // Директорія для збережень (/data/saves/)
    private GameLoader gameLoader; // Завантажувач JSON і ресурсів

    // Конструктор: ініціалізує менеджер збереження
    public SaveManager() {
        saveFiles = new ArrayList<>();
        saveDirectory = "data/saves/";
        gameLoader = new GameLoader();
        loadSaveFiles();
    }

    // --- Ініціалізація ---

    // Завантажує список наявних збережень із директорії
    private void loadSaveFiles() {
        File dir = new File(saveDirectory);
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.getName().startsWith("save_level_") && file.getName().endsWith(".json")) {
                    JSONObject data = gameLoader.loadJSON(file.getPath());
                    if (data != null) {
                        SaveFile saveFile = new SaveFile(file.getPath(), data.getInt("levelId"), data.getString("timestamp"));
                        saveFiles.add(saveFile);
                    }
                }
            }
        }
    }

    // --- Збереження ---

    // Зберігає стан гри (викликається з GameManager.saveGame())
    public void saveGame(GameManager.GameState gameState) {
        JSONObject saveData = new JSONObject();
        saveData.put("gameState", gameState.toString());
        saveData.put("levelId", GameManager.getInstance().getCurrentLevelId());
        saveData.put("timestamp", java.time.LocalDateTime.now().toString());
        saveData.put("noteCode", GameManager.getInstance().getNoteCode());
        // Save other objects
        savePlayer(GameManager.getInstance().getPlayer());
        savePolice(GameManager.getInstance().getPolice());
        saveCameras(GameManager.getInstance().getCameras());
        saveInteractiveObjects(GameManager.getInstance().getInteractables());
        savePuzzles(GameManager.getInstance().getPuzzles());
        saveProgress(GameManager.getInstance().getCurrentLevelId());
        // Save main save file
        String filename = saveDirectory + "save_level_" + GameManager.getInstance().getCurrentLevelId() + ".json";
        saveJSON(saveData, filename);
        saveFiles.add(new SaveFile(filename, GameManager.getInstance().getCurrentLevelId(), saveData.getString("timestamp")));
    }

    // Зберігає гравця
    public void savePlayer(Player player) {
        if (player == null) return;
        JSONObject data = player.getSerializableData();
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
    // Зберігає прогрес гри
    public void saveProgress(int levelId) {
        JSONObject data = new JSONObject();
        data.put("levelId", levelId);
        saveJSON(data, saveDirectory + "game_progress.json");
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
            System.out.println("Збереження не знайдено: " + filename);
            return;
        }
        List<GameObject> objects = new ArrayList<>();
        String basePath = saveDirectory;
        // Load player
        JSONObject playerData = gameLoader.loadJSON(basePath + "player_current.json");
        if (playerData != null) {
            objects.addAll(gameLoader.parseTiledJSON(playerData));
        }
        // Load police
        JSONObject policeData = gameLoader.loadJSON(basePath + "police_current.json");
        if (policeData != null) {
            objects.addAll(gameLoader.parseTiledJSON(policeData));
        }
        // Load cameras
        JSONObject camerasData = gameLoader.loadJSON(basePath + "cameras_current.json");
        if (camerasData != null) {
            objects.addAll(gameLoader.parseTiledJSON(camerasData));
        }
        // Load interactables
        JSONObject interactiveObjectsData = gameLoader.loadJSON(basePath + "interactiveObjects_current.json");
        if (interactiveObjectsData != null) {
            objects.addAll(gameLoader.parseTiledJSON(interactiveObjectsData));
        }
        // Load puzzles
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
        // Pass objects to GameManager
        GameManager.getInstance().setGameObjects(objects);
        GameManager.getInstance().setFromData(saveData);
    }
    // --- Геттери ---

    // Повертає список файлів збереження для меню
    public List<String> getSaveFiles() {
        List<String> filenames = new ArrayList<>();
        for (SaveFile saveFile : saveFiles) {
            filenames.add(saveFile.getFileName());
        }
        return filenames;
    }
}