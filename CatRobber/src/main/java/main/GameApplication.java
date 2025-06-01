package main;

import javafx.application.Application;
import javafx.stage.Stage;

// Точка входу в гру, запускає JavaFX-додаток
public class GameApplication extends Application {
    // Поля
    private GameWindow gameWindow; // Вікно гри
    private Stage primaryStage; // Головне вікно JavaFX

    // Точка входу
    public static void main(String[] args) {
        launch(args); // Запускає JavaFX
    }

    // Ініціалізує гру
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.gameWindow = new GameWindow(primaryStage);
        gameWindow.initialize();
        gameWindow.startGameLoop();
    }

    // Очищає ресурси при закритті
    @Override
    public void stop() {
        gameWindow.cleanup(); // Гарантуємо очищення ресурсів при закритті
    }
}