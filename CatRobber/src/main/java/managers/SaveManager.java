package managers;

import entities.Player;
import entities.Police;
import entities.SecurityCamera;
import interfaces.GameObject;
import interfaces.Interactable;
import interfaces.Savable;
import org.json.JSONObject;
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
        // Зберігаємо об’єкти
        savePlayer(GameManager.getInstance().getPlayer());
        savePolice(GameManager.getInstance().getPolice());
        saveCameras(GameManager.getInstance().getCameras());
        saveInteractables(GameManager.getInstance().getInteractables());
        savePuzzles(GameManager.getInstance().getPuzzles());
        saveProgress(GameManager.getInstance().getCurrentLevelId());
        // Зберігаємо основний файл збереження
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
    public void saveInteractables(List<Interactable> interactables) {
        JSONObject data = new JSONObject();
        for (int i = 0; i < interactables.size(); i++) {
            if (interactables.get(i) instanceof Savable) {
                data.put("interactable_" + i, ((Savable) interactables.get(i)).getSerializableData());
            }
        }
        saveJSON(data, saveDirectory + "interactables_current.json");
    }

    // Зберігає головоломки
    public void savePuzzles(List<Puzzle> puzzles) {
        JSONObject data = new JSONObject();
        for (int i = 0; i < puzzles.size(); i++) {
            data.put("puzzle_" + i, puzzles.get(i).getSerializableData());
        }
        saveJSON(data, saveDirectory + "puzzles_current.json");
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
        // Завантажуємо гравця
        JSONObject playerData = gameLoader.loadJSON(basePath + "player_current.json");
        if (playerData != null) {
            objects.addAll(gameLoader.parseTiledJSON(playerData));
        }
        // Завантажуємо поліцейських
        JSONObject policeData = gameLoader.loadJSON(basePath + "police_current.json");
        if (policeData != null) {
            objects.addAll((Collection<? extends GameObject>) gameLoader.parseTiledJSON(policeData));
        }
        // Завантажуємо камери
        JSONObject camerasData = gameLoader.loadJSON(basePath + "cameras_current.json");
        if (camerasData != null) {
            objects.addAll(gameLoader.parseTiledJSON(camerasData));
        }
        // Завантажуємо інтерактивні об’єкти
        JSONObject interactablesData = gameLoader.loadJSON(basePath + "interactables_current.json");
        if (interactablesData != null) {
            objects.addAll(gameLoader.parseTiledJSON(interactablesData));
        }
        // Завантажуємо головоломки
        JSONObject puzzlesData = gameLoader.loadJSON(basePath + "puzzles_current.json");
        if (puzzlesData != null) {
            objects.addAll(gameLoader.parseTiledJSON(puzzlesData));
        }
        // Передаємо об’єкти в GameManager
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