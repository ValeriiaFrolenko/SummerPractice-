package managers;

import entities.Player;
import entities.Police;
import entities.SecurityCamera;
import interfaces.Interactable;
import puzzles.Puzzle;
import utils.SaveFile;

import java.util.List;

// Керує збереженням і завантаженням гри
public class SaveManager {
    // Поля
    private List<SaveFile> saveFiles; // Список збережень
    private String saveDirectory; // /data/saves/

    // Конструктор
    public SaveManager() {}

    // Зберігає гру
    // Отримує GameState від GameManager, викликає Savable.getSerializableData
    public void saveGame(GameManager.GameState gameState) {}

    // Завантажує гру
    // Отримує filename, викликає Savable.setFromData
    public void loadGame(String filename) {}

    // Зберігає гравця
    // Отримує Player, записує в /data/saves/player_current.json
    public void savePlayer(Player player) {}

    // Зберігає поліцейських
    // Отримує список Police, записує в /data/saves/police_current.json
    public void savePolice(List<Police> police) {}

    // Зберігає камери
    // Отримує список Camera, записує в /data/saves/cameras_current.json
    public void saveCameras(List<SecurityCamera> cameras) {}

    // Зберігає об’єкти взаємодії
    // Отримує список Interactable, записує в /data/saves/interactables_current.json
    public void saveInteractables(List<Interactable> interactables) {}

    // Зберігає головоломки
    // Отримує список Puzzle, записує в /data/saves/puzzles_current.json
    public void savePuzzles(List<Puzzle> puzzles) {}

    // Зберігає прогрес
    // Отримує levelId, записує в /data/saves/game_progress.json
    public void saveProgress(int levelId) {}

    // Повертає список збережень
    // Передає в Menu для вибору
    public List<String> getSaveFiles() { return null; }
}