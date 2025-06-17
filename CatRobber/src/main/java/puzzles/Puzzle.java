package puzzles;

import entities.Door;
import interfaces.Savable;
import org.json.JSONObject;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Абстрактний клас для реалізації головоломок у грі. Реалізує інтерфейс Savable.
 */
public abstract class Puzzle implements Savable {
    /** Поточний стан головоломки. */
    protected PuzzleState state;

    /** Рішення головоломки. */
    protected Object solution;

    /** Вузол інтерфейсу для відображення головоломки. */
    protected Node uiNode;

    /** Сцена для відображення головоломки. */
    protected Stage stage;

    /** Двері, пов’язані з головоломкою. */
    protected Door linkedDoor;

    /** Callback для повідомлення про результат вирішення головоломки. */
    protected PuzzleCallback callback;

    /**
     * Перелік можливих станів головоломки.
     */
    public enum PuzzleState { UNSOLVED, SOLVED }

    /**
     * Інтерфейс для callback, який викликається після вирішення головоломки.
     */
    public interface PuzzleCallback {
        /**
         * Викликається після вирішення головоломки.
         *
         * @param solved чи була головоломка вирішена
         * @param door пов’язані двері
         */
        void onPuzzleSolved(boolean solved, Door door);
    }

    /**
     * Конструктор для ініціалізації головоломки.
     *
     * @param defaultData JSON-об’єкт із початковими даними, включаючи рішення
     */
    public Puzzle(JSONObject defaultData) {
        this.state = PuzzleState.UNSOLVED;
        this.solution = defaultData.opt("solution");
    }

    /**
     * Встановлює пов’язані двері та callback для головоломки.
     *
     * @param door двері, пов’язані з головоломкою
     * @param callback callback для повідомлення результату
     */
    public void setLinkedDoor(Door door, PuzzleCallback callback) {
        this.linkedDoor = door;
        this.callback = callback;
    }

    /**
     * Абстрактний метод для вирішення головоломки.
     *
     * @param input введені дані для вирішення
     */
    public abstract void solve(Object input);

    /**
     * Абстрактний метод для отримання вузла інтерфейсу головоломки.
     *
     * @return вузол інтерфейсу
     */
    public abstract Node getUI();

    /**
     * Ініціалізує головоломку з початкових даних.
     */
    public void initializeFromDefault() {}

    /**
     * Ініціалізує головоломку з даних збереження.
     *
     * @param saveData JSON-об’єкт із даними збереження
     */
    public void initializeFromSave(JSONObject saveData) {
        this.state = PuzzleState.valueOf(saveData.optString("state", "UNSOLVED"));
    }

    /**
     * Повертає серіалізовані дані головоломки для збереження.
     *
     * @return JSON-об’єкт із даними головоломки
     */
    @Override
    public JSONObject getSerializableData() {
        JSONObject data = new JSONObject();
        data.put("state", state.toString());
        return data;
    }

    /**
     * Встановлює стан головоломки з даних збереження.
     *
     * @param data JSON-об’єкт із даними
     */
    @Override
    public void setFromData(JSONObject data) {
        this.state = PuzzleState.valueOf(data.optString("state", "UNSOLVED"));
    }
}