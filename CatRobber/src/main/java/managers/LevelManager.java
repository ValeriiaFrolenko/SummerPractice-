package managers;

import interfaces.GameObject;
import org.json.JSONObject;
import utils.GameLoader;
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
        System.out.println("Loading level from file: " + levelFile);
        JSONObject levelData = gameLoader.loadJSON(levelFile);
        if (levelData == null) {
            System.out.println("Failed to load level " + id);
            return;
        }
        levels.put(id, levelData);
        System.out.println("Level " + id + " successfully loaded");
        // Завантажуємо карту колізій
        collisionMap = gameLoader.loadCollisionMap(levelData);
        // Завантажуємо об’єкти
        List<GameObject> objects = gameLoader.parseTiledJSON(levelData);
        GameManager.getInstance().setGameObjects(objects);
        GameManager.getInstance().setCollisionMap(collisionMap);
        // Завантажуємо збереження або дефолтні дані
        if (isNewGame) {
            System.out.println("Creating default files for new game...");
            createDefaultFiles(levelData);
            loadFromDefaults();
        } else {
            System.out.println("Loading from save...");
            loadFromSave();
        }
    }

    // Створює дефолтні файли для нового рівня
    public void createDefaultFiles(JSONObject tiledData) {
        gameLoader.createDefaultFiles(tiledData, currentLevelId);
    }

    // Завантажує збереження
    public void loadFromSave() {
        String saveFile = "data/saves/save_level_" + currentLevelId + ".json";
        System.out.println("Attempting to load save: " + saveFile);
        saveManager.loadGame(saveFile);
        if (GameManager.getInstance().getGameObjects().isEmpty()) {
            System.out.println("Save file missing or empty, loading default files...");
            loadFromDefaults();
        }
    }

    // Завантажує дефолтні об’єкти
    private void loadFromDefaults() {
        String basePath = "data/defaults/";
        List<GameObject> objects = new ArrayList<>();
        String[] fileTypes = {
                "player/player_level_" + currentLevelId + ".json",
                "police/police_level_" + currentLevelId + ".json",
                "doors/door_level_" + currentLevelId + ".json",
                "cameras/cameras_level_" + currentLevelId + ".json",
                "interactables/interactable_objects_level_" + currentLevelId + ".json"
        };
        for (String fileType : fileTypes) {
            String filePath = basePath + fileType;
            System.out.println("Loading default file: " + filePath);
            JSONObject data = gameLoader.loadJSON(filePath);
            if (data != null) {
                List<GameObject> typeObjects = gameLoader.createObjectsFromJSON(data);
                objects.addAll(typeObjects);
                System.out.println("Added " + typeObjects.size() + " objects from " + fileType);
            } else {
                System.out.println("Failed to load default file: " + filePath);
            }
        }
        GameManager.getInstance().setGameObjects(objects);
        GameManager.getInstance().setCollisionMap(collisionMap);
        System.out.println("Total loaded " + objects.size() + " objects from default files");
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