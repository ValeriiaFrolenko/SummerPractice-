package utils;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.geometry.Point2D;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * Клас для обробки вводу з клавіатури та миші в грі.
 */
public class InputHandler {
    /** Сцена для обробки подій вводу. */
    private Scene scene;

    /** Набір натиснутих клавіш. */
    private Set<KeyCode> pressedKeys;

    /** Поточна позиція миші. */
    private Point2D mousePosition;

    /** Мапа колбеків для клавіш. */
    private Map<KeyCode, Runnable> inputCallbacks;

    /**
     * Конструктор для ініціалізації обробника вводу.
     *
     * @param scene сцена, до якої прив’язуються обробники подій
     */
    public InputHandler(Scene scene) {
        this.scene = scene;
        this.pressedKeys = new HashSet<>();
        this.mousePosition = new Point2D(0, 0);
        this.inputCallbacks = new HashMap<>();
    }

    /**
     * Налаштовує обробники подій для клавіатури.
     * Додає слухачі для подій натискання та відпускання клавіш.
     */
    public void setupKeyHandlers() {
        scene.setOnKeyPressed(event -> {
            KeyCode key = event.getCode();
            pressedKeys.add(key);
            if (inputCallbacks.containsKey(key)) {
                inputCallbacks.get(key).run();
            }
        });
        scene.setOnKeyReleased(event -> {
            KeyCode key = event.getCode();
            pressedKeys.remove(key);
        });
    }

    /**
     * Налаштовує обробники подій для миші.
     * Додає слухачі для подій руху та кліку миші.
     */
    public void setupMouseHandlers() {
        scene.setOnMouseMoved(event -> {
            mousePosition = new Point2D(event.getX(), event.getY());
        });
        scene.setOnMouseClicked(event -> {
            mousePosition = new Point2D(event.getX(), event.getY());
        });
    }

    /**
     * Перевіряє, чи натиснута вказана клавіша.
     *
     * @param key код клавіші для перевірки
     * @return true, якщо клавіша натиснута, інакше false
     */
    public boolean isKeyPressed(KeyCode key) {
        return pressedKeys.contains(key);
    }

    /**
     * Повертає поточну позицію миші.
     *
     * @return координати миші у вигляді Point2D
     */
    public Point2D getMousePosition() {
        return mousePosition;
    }

    /**
     * Реєструє колбек для певної клавіші.
     *
     * @param key код клавіші
     * @param callback дія, яка виконується при натисканні клавіші
     */
    public void registerCallback(KeyCode key, Runnable callback) {
        inputCallbacks.put(key, callback);
    }
}