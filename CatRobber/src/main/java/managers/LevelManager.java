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

/**
 * Клас для управління завантаженням рівнів гри, включаючи дані рівня, об’єкти та збереження.
 */
public class LevelManager {
    /** Кеш рівнів у форматі JSON, де ключ — ID рівня. */
    private Map<Integer, JSONObject> levels;

    /** ID поточного рівня. */
    private int currentLevelId;

    /** Завантажувач JSON-файлів і об’єктів гри. */
    private GameLoader gameLoader;

    /** Менеджер збереження гри. */
    private SaveManager saveManager;

    /** Карта колізій, що містить список кімнат. */
    private List<GameManager.Room> collisionMap;

    /**
     * Конструктор для ініціалізації менеджера рівнів.
     */
    public LevelManager() {
        levels = new HashMap<>();
        gameLoader = new GameLoader();
        saveManager = new SaveManager();
        collisionMap = new ArrayList<>();
        currentLevelId = 1;
    }

    /**
     * Завантажує рівень гри за його ID.
     *
     * @param id ID рівня для завантаження
     * @param isNewGame чи є це новою грою
     */
    public void loadLevel(int id, boolean isNewGame) {
        currentLevelId = id;
        String levelFile = "data/levels/level" + id + "/level" + id + ".tmj";
        JSONObject levelData = gameLoader.loadJSON(levelFile);
        if (levelData == null) {
            System.err.println("Failed to load level " + id);
            return;
        }
        levels.put(id, levelData);
        collisionMap = gameLoader.loadCollisionMap(levelData);
        String backgroundPath = "background/level" + id + "/rooms.png";
        GameManager.getInstance().setBackgroundImage(backgroundPath);
        if (isNewGame) {
            createDefaultFiles(levelData);
            loadFromDefaults();
            String saveFile = "data/saves/game_progress.json";
            File file = new File(saveFile);
            if (file.exists()) {
                JSONObject saveData = gameLoader.loadJSON(saveFile);
                GameManager.getInstance().setFromData(saveData);
                GameManager.getInstance().setCurrentLevelId(id);
                GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);
            }
        } else {
            loadFromSave();
        }
        GameManager.getInstance().setCollisionMap(collisionMap);
    }

    /**
     * Створює дефолтні файли для нового рівня.
     *
     * @param tiledData JSON-дані рівня
     */
    public void createDefaultFiles(JSONObject tiledData) {
        gameLoader.createDefaultFiles(tiledData, currentLevelId);
    }

    /**
     * Завантажує збережені дані гри.
     */
    private void loadFromSave() {
        String saveFile = "data/saves/game_progress.json";
        File file = new File(saveFile);
        if (file.exists()) {
            saveManager.loadGame(saveFile);
        } else {
            loadFromDefaults();
        }
    }

    /**
     * Завантажує дефолтні об’єкти для рівня.
     */
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
        // Двері
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

    /**
     * Повертає JSON-дані поточного рівня.
     *
     * @return JSON-об’єкт із даними рівня або null, якщо рівень не завантажено
     */
    public JSONObject getLevelData() {
        return levels.get(currentLevelId);
    }

    /**
     * Повертає ID поточного рівня.
     *
     * @return ID поточного рівня
     */
    public int getCurrentLevelId() {
        return currentLevelId;
    }

    /**
     * Повертає карту колізій (список кімнат).
     *
     * @return список кімнат
     */
    public List<GameManager.Room> getCollisionMap() {
        return collisionMap;
    }
}