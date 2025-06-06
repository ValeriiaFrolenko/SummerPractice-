package utils;

import org.json.JSONObject;

// Представляє метадані файлу збереження
public class SaveFile {
    // Поля
    private String fileName; // Шлях до файлу збереження
    private int levelId; // ID рівня
    private String timestamp; // Час створення збереження

    // Конструктор: ініціалізує файл збереження
    public SaveFile(String fileName, int levelId, String timestamp) {
        this.fileName = fileName;
        this.levelId = levelId;
        this.timestamp = timestamp;
    }

    // --- Серіалізація ---

    // Повертає JSON-метадані збереження
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("fileName", fileName);
        json.put("levelId", levelId);
        json.put("timestamp", timestamp);
        return json;
    }

    // Ініціалізує з JSON
    public void fromJSON(JSONObject data) {
        this.fileName = data.getString("fileName");
        this.levelId = data.getInt("levelId");
        this.timestamp = data.getString("timestamp");
    }

    // --- Геттери ---

    // Повертає назву файлу
    public String getFileName() {
        return fileName;
    }

    // Повертає ID рівня
    public int getLevelId() {
        return levelId;
    }

    // Повертає час створення
    public String getTimestamp() {
        return timestamp;
    }
}