package utils;

import entities.*;
import interfaces.GameObject;
import javafx.geometry.BoundingBox;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.media.AudioClip;
import managers.GameManager;
import org.json.JSONArray;
import org.json.JSONObject;
import puzzles.CodeLockPuzzle;
import puzzles.LaserLockPuzzle;
import puzzles.LockPickPuzzle;
import puzzles.Puzzle;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

// Завантажує ресурси гри (JSON, зображення, звуки, об’єкти)
public class GameLoader {
    // --- Завантаження ресурсів ---

    // Завантажує JSON-файл
    public JSONObject loadJSON(String filename) {
        try (FileReader reader = new FileReader(filename)) {
            StringBuilder text = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                text.append((char) c);
            }
            return new JSONObject(text.toString());
        } catch (IOException e) {
            System.err.println("Не можу прочитати файл: " + filename + ", помилка: " + e.getMessage());
            System.out.println(System.getProperty("user.dir"));
            return null;
        }
    }

    // Завантажує зображення
    public Image loadImage(String path) {
        try {
            String[] possiblePaths = {"assets/images/" + path, "assets/" + path, path};
            for (String possiblePath : possiblePaths) {
                File file = new File(possiblePath);
                if (file.exists()) {
                    return new Image(new FileInputStream(file));
                }
            }
            System.err.println("Зображення не знайдено за жодним з шляхів: " + java.util.Arrays.toString(possiblePaths));
            return null;
        } catch (FileNotFoundException e) {
            System.err.println("Зображення не знайдено: " + path);
            return null;
        }
    }

    // Завантажує звук
    public AudioClip loadAudio(String path) {
        try {
            return new AudioClip(new File("assets/sounds/" + path).toURI().toString());
        } catch (Exception e) {
            System.err.println("Звук не знайдено: " + path);
            return null;
        }
    }

    // Розбиває спрайт-лист на кадри
    public Image[] splitSpriteSheet(String path, int frameCount) {
        Image spriteSheet = loadImage(path);
        if (spriteSheet == null) {
            System.err.println("Не вдалося завантажити спрайт-лист: " + path);
            return new Image[0];
        }
        if (frameCount <= 0) {
            System.err.println("Некоректна кількість кадрів: " + frameCount + " для " + path);
            return new Image[]{spriteSheet};
        }
        double spriteWidth = spriteSheet.getWidth();
        double spriteHeight = spriteSheet.getHeight();
        double frameWidth = spriteWidth / frameCount;
        if (frameWidth < 1.0) {
            System.err.println("Занадто велика кількість кадрів: " + frameCount + " для ширини " + spriteWidth);
            return new Image[]{spriteSheet};
        }
        Image[] frames = new Image[frameCount];
        PixelReader reader = spriteSheet.getPixelReader();
        for (int i = 0; i < frameCount; i++) {
            WritableImage frame = new WritableImage((int) frameWidth, (int) spriteHeight);
            try {
                frame.getPixelWriter().setPixels(0, 0, (int) frameWidth, (int) spriteHeight,
                        reader, (int) (i * frameWidth), 0);
                frames[i] = frame;
            } catch (Exception e) {
                System.err.println("Помилка при вирізанні кадру " + i + " для " + path + ": " + e.getMessage());
                frames[i] = null;
            }
        }
        return frames;
    }

    // --- Створення об’єктів ---

    // Парсить JSON у список об’єктів
    public List<GameObject> parseTiledJSON(JSONObject tiledData) {
        return createObjectsFromJSON(tiledData);
    }

    // Створює об’єкти з JSON
    public List createObjectsFromJSON(JSONObject data) {
        List objects = new ArrayList<>();
        List puzzles = new ArrayList<>();

        // Перевіряємо, чи JSON є Tiled-форматом (має "layers")
        if (data.has("layers")) {
            JSONArray layers = data.getJSONArray("layers");
            for (int i = 0; i < layers.length(); i++) {
                JSONObject layer = layers.getJSONObject(i);
                if (layer.getString("type").equals("objectgroup")) {
                    JSONArray layerObjects = layer.getJSONArray("objects");
                    for (int j = 0; j < layerObjects.length(); j++) {
                        JSONObject obj = layerObjects.getJSONObject(j);
                        if (obj.getString("type").equals("Puzzle")) {
                            Puzzle puzzle = createSinglePuzzle(obj);
                            if (puzzle != null) {
                                puzzles.add(puzzle);
                            }
                        } else {
                            GameObject gameObject = createSingleObject(obj);
                            if (gameObject != null) {
                                objects.add(gameObject);
                            }
                        }
                    }
                }
            }
        } else {
            // Якщо JSON не має "layers", обробляємо як набір об'єктів (наприклад, збереження)
            for (String key : data.keySet()) {
                Object value = data.get(key);

                // Перевіряємо, чи значення є JSONObject
                if (value instanceof JSONObject) {
                    JSONObject obj = (JSONObject) value;

                    // Перевіряємо, чи об'єкт має поле "type"
                    if (obj.has("type")) {
                        if (obj.getString("type").equals("Puzzle")) {
                            Puzzle puzzle = createSinglePuzzle(obj);
                            if (puzzle != null) {
                                puzzles.add(puzzle);
                            }
                        } else {
                            GameObject gameObject = createSingleObject(obj);
                            if (gameObject != null) {
                                objects.add(gameObject);
                            }
                        }
                    }
                }
            }
        }

        // Додаємо головоломки до GameManager
        GameManager.getInstance().getPuzzles().addAll(puzzles);
        return objects;
    }
    // Створює окремий об’єкт із JSON
    private GameObject createSingleObject(JSONObject obj) {
        String type = obj.getString("type");
        float x = obj.optFloat("x", 0.0f);
        float y = obj.optFloat("y", 0.0f);

        // Створюємо properties і копіюємо ВСІ поля з оригінального об'єкта
        JSONObject properties = new JSONObject();

        // Копіюємо всі існуючі поля
        for (String key : obj.keySet()) {
            properties.put(key, obj.get(key));
        }

        // Додаємо/перезаписуємо координати
        properties.put("x", x);
        properties.put("y", y);

        // Додаємо розміри, якщо є
        if (obj.has("width")) {
            properties.put("width", obj.getDouble("width"));
        }
        if (obj.has("height")) {
            properties.put("height", obj.getDouble("height"));
        }

        // Обробляємо додаткові properties (якщо є)
        if (obj.has("properties")) {
            JSONArray props = obj.getJSONArray("properties");
            for (int i = 0; i < props.length(); i++) {
                JSONObject prop = props.getJSONObject(i);
                properties.put(prop.getString("name"), prop.get("value"));
            }
        }

        switch (type) {
            case "Player":
                return new Player(new Vector2D(x, y), properties);
            case "Police":
                return new Police(new Vector2D(x, y), properties);
            case "Door":
                return new Door(new Vector2D(x, y), properties);
            case "Camera":
                return new SecurityCamera(new Vector2D(x, y), properties);
            case "InteractiveObject":
                return new InteractiveObject(new Vector2D(x, y), properties);
            default:
                System.err.println("Невідомий тип об'єкта: " + type);
                return null;
        }
    }
    public Puzzle createSinglePuzzle(JSONObject obj) {
        String type = obj.getString("type");
        if (!type.equals("Puzzle")) {
            return null; // Пропускаємо, якщо не головоломка
        }
        JSONObject properties = new JSONObject();
        if (obj.has("properties")) {
            JSONArray props = obj.getJSONArray("properties");
            for (int i = 0; i < props.length(); i++) {
                JSONObject prop = props.getJSONObject(i);
                properties.put(prop.getString("name"), prop.get("value"));
            }
        }
        properties.put("x", obj.optFloat("x", 0.0f));
        properties.put("y", obj.optFloat("y", 0.0f));
        if (obj.has("width")) {
            properties.put("width", obj.getDouble("width"));
        }
        if (obj.has("height")) {
            properties.put("height", obj.getDouble("height"));
        }
        String puzzleType = properties.optString("puzzleType", "CodeLockPuzzle");
        JSONObject puzzleData = new JSONObject();
        puzzleData.put("state", properties.optString("state", "UNSOLVED"));
        puzzleData.put("x", properties.optFloat("x", 0.0f));
        puzzleData.put("y", properties.optFloat("y", 0.0f));
        puzzleData.put("solution", properties.optInt("solution"));
        puzzleData.put("type", "Puzzle");
        switch (puzzleType) {
            case "CodeLockPuzzle":
                return new CodeLockPuzzle(puzzleData);
            case "LockPickPuzzle":
                return new LockPickPuzzle(puzzleData);
            case "LaserLockPuzzle":
                return new LaserLockPuzzle(puzzleData);
            default:
                System.err.println("Невідомий тип головоломки: " + puzzleType);
                return null;
        }
    }

    // --- Створення дефолтних файлів ---

    // Створює дефолтні файли для рівня (викликається з LevelManager.createDefaultFiles())
    public void createDefaultFiles(JSONObject levelData, int levelId) {
        String basePath = "data/defaults/";
        JSONObject playerData = new JSONObject();
        JSONObject policeData = new JSONObject();
        JSONObject doorData = new JSONObject();
        JSONObject cameraData = new JSONObject();
        JSONObject interactableData = new JSONObject();
        JSONObject puzzleData = new JSONObject(); // Додаємо для головоломок

        JSONArray layers = levelData.getJSONArray("layers");
        for (int i = 0; i < layers.length(); i++) {
            JSONObject layer = layers.getJSONObject(i);
            if (layer.getString("type").equals("objectgroup")) {
                JSONArray objects = layer.getJSONArray("objects");
                for (int j = 0; j < objects.length(); j++) {
                    JSONObject obj = objects.getJSONObject(j);
                    String type = obj.getString("type");
                    int id = obj.getInt("id");
                    switch (type) {
                        case "Player":
                            playerData.put("player_" + id, obj);
                            break;
                        case "Police":
                            policeData.put("police_" + id, obj);
                            break;
                        case "Door":
                            doorData.put("door_" + id, obj);
                            break;
                        case "Camera":
                            cameraData.put("camera_" + id, obj);
                            break;
                        case "InteractiveObject":
                            interactableData.put("interactiveObjects_" + id, obj);
                            break;
                        case "Puzzle":
                            puzzleData.put("puzzles_" + id, obj);
                            break;
                    }
                }
            }
        }

        // Оновлюємо масив шляхів до файлів, додаючи файл для головоломок
        String[] filePaths = {
                basePath + "player/player_level_" + levelId + ".json",
                basePath + "police/police_level_" + levelId + ".json",
                basePath + "doors/door_level_" + levelId + ".json",
                basePath + "cameras/cameras_level_" + levelId + ".json",
                basePath + "interactiveObjects/interactiveObjects_level_" + levelId + ".json",
                basePath + "puzzles/puzzles_level_" + levelId + ".json"
        };

        // Видаляємо старі файли
        for (String filePath : filePaths) {
            File file = new File(filePath);
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("Видалено старий файл: " + filePath);
                } else {
                    System.err.println("Не вдалося видалити старий файл: " + filePath);
                }
            }
        }

        // Створюємо директорії
        createDirectoryIfNotExists(basePath + "player/");
        createDirectoryIfNotExists(basePath + "police/");
        createDirectoryIfNotExists(basePath + "doors/");
        createDirectoryIfNotExists(basePath + "cameras/");
        createDirectoryIfNotExists(basePath + "interactiveObjects/");
        createDirectoryIfNotExists(basePath + "puzzles/"); // Додаємо директорію для головоломок

        // Зберігаємо файли
        saveJSON(playerData, basePath + "player/player_level_" + levelId + ".json");
        saveJSON(policeData, basePath + "police/police_level_" + levelId + ".json");
        saveJSON(doorData, basePath + "doors/door_level_" + levelId + ".json");
        saveJSON(cameraData, basePath + "cameras/cameras_level_" + levelId + ".json");
        saveJSON(interactableData, basePath + "interactiveObjects/interactiveObjects_level_" + levelId + ".json");
        saveJSON(puzzleData, basePath + "puzzles/puzzles_level_" + levelId + ".json"); // Зберігаємо головоломки
    }
    // Створює директорію, якщо не існує
    private void createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Створено директорію: " + path);
            } else {
                System.err.println("Не вдалося створити директорію: " + path);
            }
        }
    }

    // Зберігає JSON у файл
    private void saveJSON(JSONObject data, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(data.toString(2));
        } catch (IOException e) {
            System.err.println("Не можу зберегти файл: " + filename + ", помилка: " + e.getMessage());
        }
    }

    // --- Карта колізій ---

    // Завантажує карту колізій із JSON
    public List<GameManager.Room> loadCollisionMap(JSONObject levelData) {
        List<GameManager.Room> rooms = new ArrayList<>();
        if (levelData == null || !levelData.has("layers")) {
            System.err.println("Немає шарів у даних рівня для карти колізій");
            return rooms;
        }
        JSONArray layers = levelData.getJSONArray("layers");
        for (int i = 0; i < layers.length(); i++) {
            JSONObject layer = layers.getJSONObject(i);
            if (layer.getString("type").equals("objectgroup")) {
                JSONArray layerObjects = layer.getJSONArray("objects");
                for (int j = 0; j < layerObjects.length(); j++) {
                    JSONObject obj = layerObjects.getJSONObject(j);
                    String type = obj.getString("type");
                    if (type.equals("Room")) {
                        double x = obj.getDouble("x");
                        double y = obj.getDouble("y");
                        double width = obj.getDouble("width");
                        double height = obj.getDouble("height");
                        int roomId = -1;
                        if (obj.has("properties")) {
                            JSONArray props = obj.getJSONArray("properties");
                            for (int k = 0; k < props.length(); k++) {
                                JSONObject prop = props.getJSONObject(k);
                                if (prop.getString("name").equals("roomId")) {
                                    roomId = prop.getInt("value");
                                    break;
                                }
                            }
                        }
                        if (roomId == -1) {
                            System.err.println("Попередження: roomId не знайдено для об’єкта Room з id " + obj.getInt("id"));
                            continue;
                        }
                        rooms.add(new GameManager.Room(roomId, new BoundingBox(x, y, width, height)));
                    }
                }
            }
        }
        return rooms;
    }
}