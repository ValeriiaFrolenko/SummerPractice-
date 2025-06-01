package utils;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.geometry.Point2D;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

// Обробляє ввід із клавіатури та миші
public class InputHandler {
    // Поля
    private Scene scene; // Сцена для обробки подій
    private Set<KeyCode> pressedKeys; // Набір натиснутих клавіш
    private Point2D mousePosition; // Позиція миші
    private Map<KeyCode, Runnable> inputCallbacks; // Колбеки для клавіш

    // Конструктор
    // Отримує scene з GameWindow.initializeManagers
    public InputHandler(Scene scene) {
        this.scene = scene;
        this.pressedKeys = new HashSet<>();
        this.mousePosition = new Point2D(0, 0);
        this.inputCallbacks = new HashMap<>();
    }

    // Налаштовує обробники клавіатури
    // Додає слухачі для scene
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

    // Налаштовує обробники миші
    // Додає слухачі для scene
    public void setupMouseHandlers() {
        scene.setOnMouseMoved(event -> {
            mousePosition = new Point2D(event.getX(), event.getY());
        });

        scene.setOnMouseClicked(event -> {
            mousePosition = new Point2D(event.getX(), event.getY());
        });
    }

    // Перевіряє, чи натиснута клавіша
    // Отримує key, повертає boolean в GameManager, UIManager
    public boolean isKeyPressed(KeyCode key) { return pressedKeys.contains(key);}

    // Повертає позицію миші
    // Передає в UIManager для UI-елементів
    public Point2D getMousePosition() { return mousePosition; }

    // Реєструє колбек для клавіші
    // Отримує key і callback, зберігає в inputCallbacks
    public void registerCallback(KeyCode key, Runnable callback) {inputCallbacks.put(key, callback);}
}