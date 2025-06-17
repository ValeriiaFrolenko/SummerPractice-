package utils;

import org.json.JSONObject;

/**
 * Клас для представлення метаданих файлу збереження гри.
 */
public class SaveFile {
    /** Шлях до файлу збереження. */
    private String fileName;

    /** Ідентифікатор рівня гри. */
    private int levelId;

    /** Час створення файлу збереження. */
    private String timestamp;

    /**
     * Конструктор для ініціалізації об’єкта збереження.
     *
     * @param fileName шлях до файлу збереження
     * @param levelId ідентифікатор рівня гри
     * @param timestamp час створення збереження
     */
    public SaveFile(String fileName, int levelId, String timestamp) {
        this.fileName = fileName;
        this.levelId = levelId;
        this.timestamp = timestamp;
    }

    /**
     * Серіалізує метадані збереження у JSON-об’єкт.
     *
     * @return JSON-об’єкт із метаданими збереження
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("fileName", fileName);
        json.put("levelId", levelId);
        json.put("timestamp", timestamp);
        return json;
    }

    /**
     * Ініціалізує об’єкт із JSON-даних.
     *
     * @param data JSON-об’єкт із метаданими збереження
     */
    public void fromJSON(JSONObject data) {
        this.fileName = data.getString("fileName");
        this.levelId = data.getInt("levelId");
        this.timestamp = data.getString("timestamp");
    }

    /**
     * Повертає шлях до файлу збереження.
     *
     * @return шлях до файлу
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Повертає ідентифікатор рівня гри.
     *
     * @return ID рівня
     */
    public int getLevelId() {
        return levelId;
    }

    /**
     * Повертає час створення збереження.
     *
     * @return час створення у вигляді рядка
     */
    public String getTimestamp() {
        return timestamp;
    }
}