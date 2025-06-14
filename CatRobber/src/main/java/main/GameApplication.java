package main;

import javafx.application.Application;
import javafx.stage.Stage;
import main.GameWindow;
import managers.GameManager;
import managers.SaveManager;

public class GameApplication extends Application {
    // Поля
    private GameWindow gameWindow; // Посилання на об'єкт GameWindow для управління вікном гри
    private Stage primaryStage; // Головне вікно JavaFX, отримане від JavaFX runtime
    private GameManager gameManager; // Менеджер гри
    private SaveManager saveManager; // Менеджер збереження

    // Точка входу програми
    public static void main(String[] args) {
        launch(args); // Запускає JavaFX-додаток, викликаючи метод start()
    }


    // Ініціалізує гру, викликається JavaFX після запуску
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage; // Зберіг
        // аємо посилання на головне вікно
        this.gameManager = GameManager.getInstance(); // Ініціалізуємо GameManager
        this.saveManager = new SaveManager(); // Ініціалізуємо SaveManager
        this.gameWindow = new GameWindow(primaryStage, gameManager, saveManager); // Передаємо менеджери в GameWindow
        gameWindow.initialize(); // Ініціалізуємо вікно гри
        gameWindow.startGameLoop(); // Запускаємо ігровий цикл
    }

    // Викликається при закритті програми для очищення ресурсів
    @Override
    public void stop() {
        if (gameManager != null && saveManager != null) {
            gameManager.saveGame();
        }
        if (gameWindow != null) {
            gameWindow.cleanup(); // Очищаємо ресурси GameWindow
        }
    }
}
