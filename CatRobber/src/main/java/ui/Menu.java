package ui;

import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import org.json.JSONObject;
import managers.GameManager;
import managers.SaveManager;

// Головне меню гри, розширює UIWindow
public class Menu implements UIWindow {
    // Поля
    private VBox menuPane; // Контейнер для UI
    private Button newGameButton; // Кнопка "Нова гра"
    private Button selectLevelButton; // Кнопка "Почати з рівня"
    private Button continueButton; // Кнопка "Продовжити"
    private ChoiceBox<Integer> levelChoice; // Вибір рівня

    // Конструктор
    // Отримує defaultData з /data/settings/config.json
    public Menu(JSONObject defaultData) {}

    // Обробляє ввід
    // Отримує KeyEvent від UIManager, передає в GameManager
    public void handleInput(KeyEvent event) {}

    // Починає нову гру
    // Викликає GameManager.loadLevel(1, true) для дефолтних файлів
    public void startNewGame() {}

    // Починає гру з рівня
    // Викликає GameManager.loadLevel(levelId, true) для дефолтних файлів
    public void startLevel(int levelId) {}

    // Продовжує гру
    // Викликає GameManager.loadFromSave для збережень
    public void continueGame() {}

    // Створює UI меню
    // Налаштовує menuPane, додає кнопки
    public void createMenuUI() {}

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public Node getRoot() {
        return null;
    }
}