package managers;

import utils.GameLoader;
import org.json.JSONObject;
import java.util.Map;

// Завантажує рівні, парсить Tiled JSON, створює дефолтні файли
public class LevelManager {
    // Поля
    private Map<Integer, JSONObject> levels; // Дані рівнів (Tiled JSON)
    private int currentLevelId; // Поточний ID рівня
    private GameLoader gameLoader; // Завантажувач JSON і ресурсів

    // Конструктор
    public LevelManager() {}

    // Завантажує рівень
    // Отримує id і isNewGame (true: дефолтні файли, false: збереження)
    // Завантажує /data/levels/level_X.json, створює дефолтні файли якщо isNewGame
    public void loadLevel(int id, boolean isNewGame) {}

    // Створює дефолтні файли (/data/defaults/[type]_level_X.json)
    // Отримує tiledData, викликає GameLoader.createDefaultFiles
    public void createDefaultFiles(JSONObject tiledData) {}

    // Парсить Tiled JSON
    // Отримує tiledData, викликає GameLoader.parseTiledJSON
    public void parseTiledJSON(JSONObject tiledData) {}

    // Створює об’єкти гри
    // Отримує levelData (Tiled JSON), повертає об’єкти в GameManager
    public void createGameObjects(JSONObject levelData) {}
}