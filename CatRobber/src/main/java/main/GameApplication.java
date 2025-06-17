package main;

import javafx.application.Application;
import javafx.stage.Stage;
import managers.GameManager;
import managers.SaveManager;

/**
 * Головний клас-запускач гри, який наслідує {@link Application} від JavaFX.
 * Цей клас відповідає за ініціалізацію головного вікна, ігрових менеджерів
 * та життєвого циклу додатку.
 */
public class GameApplication extends Application {
    private GameWindow gameWindow; // Об'єкт, що керує ігровим вікном та рендерингом.
    private Stage primaryStage; // Головне вікно (сцена), яке надається фреймворком JavaFX.
    private GameManager gameManager; // Центральний менеджер, що керує логікою гри.
    private SaveManager saveManager; // Менеджер, відповідальний за збереження та завантаження.

    /**
     * Точка входу в програму.
     * Цей метод викликає {@link #launch(String...)}, який ініціалізує JavaFX runtime
     * і викликає метод {@link #start(Stage)}.
     * @param args Аргументи командного рядка (не використовуються в цьому додатку).
     */
    public static void main(String[] args) {
        launch(args);
    }


    /**
     * Ініціалізує та запускає гру. Цей метод викликається фреймворком JavaFX
     * після виклику {@code launch()}.
     * Він створює та налаштовує всі основні компоненти гри.
     * @param primaryStage Головна сцена (вікно), що надається JavaFX.
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage; // Зберігаємо посилання на головне вікно

        // Ініціалізуємо ключові менеджери гри
        this.gameManager = GameManager.getInstance(); // Отримуємо єдиний екземпляр GameManager
        this.saveManager = new SaveManager(); // Створюємо новий екземпляр SaveManager

        // Створюємо ігрове вікно, передаючи йому посилання на менеджери
        this.gameWindow = new GameWindow(primaryStage, gameManager, saveManager);

        // Ініціалізуємо саме вікно (створення сцени, canvas, UI-елементів)
        gameWindow.initialize();

        // Запускаємо головний ігровий цикл (AnimationTimer)
        gameWindow.startGameLoop();
    }

    /**
     * Викликається при закритті програми.
     * Цей метод забезпечує коректне завершення роботи, зокрема, збереження
     * ігрового прогресу та очищення ресурсів.
     */
    @Override
    public void stop() {
        // Зберігаємо гру перед виходом
        if (gameManager != null) {
            gameManager.saveGame();
        }

        // Очищаємо ресурси, пов'язані з ігровим вікном (наприклад, зупиняємо звуки)
        if (gameWindow != null) {
            gameWindow.cleanup();
        }
    }
}