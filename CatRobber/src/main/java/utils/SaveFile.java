package utils;

import org.json.JSONObject;

// Зберігає метадані одного файлу збереження
public class SaveFile {
    // Поля
    private String fileName; // Шлях до файлу (/data/saves/player_current.json, тощо)
    private int levelId; // ID рівня, пов’язаного зі збереженням
    private String timestamp; // Дата і час створення збереження

    // Конструктор
    // Отримує метадані збереження
    public SaveFile(String fileName, int levelId, String timestamp) {}

    // Повертає назву файлу
    // Передає в SaveManager.getSaveFiles для Menu
    public String getFileName() { return null; }

    // Повертає ID рівня
    // Передає в Menu для відображення
    public int getLevelId() { return 0; }

    // Повертає дату створення
    // Передає в Menu для відображення
    public String getTimestamp() { return null; }

    // Повертає JSON-метадані збереження
    // Використовується SaveManager для збереження/завантаження
    public JSONObject toJSON() { return null; }

    // Ініціалізує з JSON
    // Отримує дані від SaveManager
    public void fromJSON(JSONObject data) {}
}