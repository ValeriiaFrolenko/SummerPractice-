package utils;

import interfaces.GameObject;
import org.json.JSONObject;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import java.util.List;
import utils.Vector2D;

// Завантажує JSON, зображення, звуки, парсить Tiled JSON, створює дефолтні файли
public class GameLoader {
    // Завантажує JSON-файл
    // Отримує шлях із /data/ (наприклад, /data/levels/level_1.json)
    // Передає в LevelManager, SaveManager
    public JSONObject loadJSON(String filename) { return null; }

    // Парсить Tiled JSON, створює об’єкти гри
    // Отримує tiledData з /data/levels/level_X.json
    // Передає список GameObject в LevelManager
    public List<GameObject> parseTiledJSON(JSONObject tiledData) { return null; }

    // Створює дефолтні файли для об’єктів (/data/defaults/[type]_level_X.json)
    // Отримує tiledData і levelId (наприклад, 1 для level_1)
    // Зберігає в /data/defaults/player/, /data/defaults/police/, тощо
    public void createDefaultFiles(JSONObject tiledData, int levelId) {}

    // Завантажує зображення
    // Отримує шлях із /assets/images/ (наприклад, player_idle.png)
    // Передає в об’єкти (Player, Police, Door, тощо)
    public Image loadImage(String path) { return null; }

    // Завантажує звук
    // Отримує шлях із /assets/sounds/ (наприклад, step.wav)
    // Передає в SoundManager
    public AudioClip loadSound(String path) { return null; }
}