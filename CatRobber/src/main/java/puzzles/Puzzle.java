package puzzles;

import interfaces.Savable;
import org.json.JSONObject;
import javafx.scene.Node;
import javafx.stage.Stage;

// Абстрактний базовий клас для головоломок, реалізує Savable
public abstract class Puzzle implements Savable {
    // Поля
    protected PuzzleState state; // Стан головоломки (UNSOLVED, SOLVED)
    protected Object solution; // Правильне рішення
    protected Node uiNode; // UI-компонент для UIManager
    protected Stage stage; // Окреме вікно для головоломки

    // Енум для стану головоломки
    public enum PuzzleState { UNSOLVED, SOLVED }

    // Конструктор
    // Отримує defaultData з /data/defaults/puzzles_level_X.json
    public Puzzle(JSONObject defaultData) {}

    // Перевіряє рішення
    // Отримує input від UIManager, передає результат у Door
    public abstract void solve(Object input);

    // Повертає UI головоломки
    // Передає Node у UIManager.showPuzzleUI
    public abstract Node getUI();

    // Ініціалізує з дефолтних даних
    // Отримує дані з /data/defaults/puzzles_level_X.json
    public void initializeFromDefault() {}

    // Ініціалізує зі збереження
    // Отримує дані з /data/saves/puzzles_current.json
    public void initializeFromSave(JSONObject saveData) {}

    // Методи Savable
    // Повертає JSON-стан (state, solution)
    @Override
    public JSONObject getSerializableData() { return null; }

    // Ініціалізує з JSON
    @Override
    public void setFromData(JSONObject data) {}
}