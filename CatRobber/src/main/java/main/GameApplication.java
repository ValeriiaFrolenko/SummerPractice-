package main;

import javafx.application.Application;
import javafx.stage.Stage;

// Точка входу в гру, успадковується від JavaFX Application
public class GameApplication extends Application {
    // Поля
    private GameWindow gameWindow; // Посилання на об'єкт GameWindow для управління вікном гри
    private Stage primaryStage; // Головне вікно JavaFX, отримане від JavaFX runtime

    // Точка входу програми
    public static void main(String[] args) {
        launch(args); // Запускає JavaFX-додаток, викликаючи метод start()
    }

    // Ініціалізує гру, викликається JavaFX після запуску
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage; // Зберігаємо посилання на головне вікно
        this.gameWindow = new GameWindow(primaryStage); // Створюємо об'єкт GameWindow, передаючи йому primaryStage
        gameWindow.initialize(); // Ініціалізуємо вікно гри (налаштування сцени, менеджерів тощо)
        gameWindow.startGameLoop(); // Запускаємо ігровий цикл (оновлення та рендеринг)
    }

    // Викликається при закритті програми для очищення ресурсів
    @Override
    public void stop() {
        gameWindow.cleanup(); // Викликаємо метод очищення GameWindow для завершення гри
    }
}