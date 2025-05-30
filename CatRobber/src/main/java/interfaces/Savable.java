package interfaces;

import org.json.JSONObject;

// Інтерфейс для об’єктів, які можна зберегти і завантажити
public interface Savable {
    // Повертає JSON-стан об’єкта, передає в SaveManager
    JSONObject getSerializableData();

    // Ініціалізує або відновлює стан із JSON, отримує defaultData/saveData
    void setFromData(JSONObject data);
}