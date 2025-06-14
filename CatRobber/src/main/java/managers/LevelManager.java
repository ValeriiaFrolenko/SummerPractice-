package managers;

import interfaces.GameObject;
import org.json.JSONObject;
import puzzles.Puzzle;
import utils.GameLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Керує завантаженням рівнів гри
public class LevelManager {
    // Поля
    private Map<Integer, JSONObject> levels; // Кеш рівнів у форматі JSON
    private int currentLevelId; // ID поточного рівня
    private GameLoader gameLoader; // Завантажувач JSON і об’єктів
    private SaveManager saveManager; // Менеджер збереження
    private List<GameManager.Room> collisionMap; // Карта колізій (кімнати)

    // Конструктор: ініціалізує менеджер рівнів
    public LevelManager() {
        levels = new HashMap<>();
        gameLoader = new GameLoader();
        saveManager = new SaveManager();
        collisionMap = new ArrayList<>();
    }

    // --- Завантаження ---

    // Завантажує рівень (викликається з GameManager.loadLevel())
    public void loadLevel(int id, boolean isNewGame) {
        currentLevelId = id;
        String levelFile = "data/levels/level" + id + "/level" + id + ".tmj";
        JSONObject levelData = gameLoader.loadJSON(levelFile);
        if (levelData == null) {
            System.err.println("Failed to load level " + id);
            return;
        }
        levels.put(id, levelData);
        // Завантажуємо карту колізій
        collisionMap = gameLoader.loadCollisionMap(levelData);
        // Встановлюємо фон
        String backgroundPath = "background/level" + id + "/rooms.png";
        GameManager.getInstance().setBackgroundImage(backgroundPath);
        // Завантажуємо об’єкти
        if (isNewGame) {
            createDefaultFiles(levelData); // Генеруємо дефолтні файли
            loadFromDefaults();
        } else {
            loadFromSave();
        }
        // Встановлюємо карту колізій
        GameManager.getInstance().setCollisionMap(collisionMap);
    }

    // Створює дефолтні файли для нового рівня
    public void createDefaultFiles(JSONObject tiledData) {
        gameLoader.createDefaultFiles(tiledData, currentLevelId);
    }

    // Завантажує збереження
    private void loadFromSave() {
        String saveFile = "data/saves/game_progress.json";
        File file = new File(saveFile);
        if (file.exists()) {
            saveManager.loadGame(saveFile);
        } else {
            loadFromDefaults();
        }
    }
    // Завантажує дефолтні об’єкти
    private void loadFromDefaults() {
        List<GameObject> objects = new ArrayList<>();
        // Гравець
        JSONObject playerData = gameLoader.loadJSON("data/defaults/player/player_level_" + currentLevelId + ".json");
        if (playerData != null) {
            objects.addAll(gameLoader.parseTiledJSON(playerData));
        }
        // Поліцейські
        JSONObject policeData = gameLoader.loadJSON("data/defaults/police/police_level_" + currentLevelId + ".json");
        if (policeData != null) {
            objects.addAll(gameLoader.parseTiledJSON(policeData));
        }
        // Камери
        JSONObject cameraData = gameLoader.loadJSON("data/defaults/cameras/cameras_level_" + currentLevelId + ".json");
        if (cameraData != null) {
            objects.addAll(gameLoader.parseTiledJSON(cameraData));
        }
        JSONObject doorData = gameLoader.loadJSON("data/defaults/doors/door_level_" + currentLevelId + ".json");
        if (doorData != null) {
            objects.addAll(gameLoader.parseTiledJSON(doorData));

        }
        // Інтерактивні об’єкти
        JSONObject interactiveData = gameLoader.loadJSON("data/defaults/interactiveObjects/interactiveObjects_level_" + currentLevelId + ".json");
        if (interactiveData != null) {
            objects.addAll(gameLoader.parseTiledJSON(interactiveData));
        }
        // Головоломки
        JSONObject puzzleData = gameLoader.loadJSON("data/defaults/puzzles/puzzles_level_" + currentLevelId + ".json");
        if (puzzleData != null) {
        }
        if (puzzleData != null) {
            for (String key : puzzleData.keySet()) {
                JSONObject puzzleObj = puzzleData.getJSONObject(key);
                Puzzle puzzle = gameLoader.createSinglePuzzle(puzzleObj);
                if (puzzle != null) {
                    GameManager.getInstance().getPuzzles().add(puzzle);
                }
            }
        }
        GameManager.getInstance().setGameObjects(objects);
    }

    // --- Геттери ---

    // Повертає JSON-даних рівня
    public JSONObject getLevelData() {
        return levels.get(currentLevelId);
    }

    // Повертає ID поточного рівня
    public int getCurrentLevelId() {
        return currentLevelId;
    }

    // Повертає карту колізій
    public List<GameManager.Room> getCollisionMap() {
        return collisionMap;
    }
}