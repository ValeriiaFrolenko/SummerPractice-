package main;

import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import managers.GameManager;
import managers.UIManager;
import managers.SoundManager;
import managers.SaveManager;
import utils.InputHandler;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.animation.AnimationTimer;

// Керує вікном гри та ігровим циклом
public class GameWindow {
    // Поля
    private Stage primaryStage; // Головне вікно
    private Scene scene; // Сцена JavaFX
    private Canvas canvas; // Полотно для рендерингу
    private GraphicsContext graphicsContext; // Контекст для малювання
    private GameManager gameManager; // Менеджер гри
    private UIManager uiManager; // Менеджер UI
    private SoundManager soundManager; // Менеджер звуків
    private SaveManager saveManager; // Менеджер збережень
    private InputHandler inputHandler; // Обробник вводу
    private AnimationTimer animationTimer; // Ігровий цикл
    private double targetFPS; // Цільова частота кадрів
    private boolean isRunning; // Стан гри
    private double windowWidth; // Ширина вікна
    private double windowHeight; // Висота вікна
    private long lastFrameTime; // Час останнього кадру

    // Конструктор
    // Отримує primaryStage від GameApplication
    public GameWindow(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // Налаштовує Stage, Scene, Canvas
    // Ініціалізує розміри вікна
    public void initialize() {
            this.windowWidth = 1280;
            this.windowHeight = 680;
            // Створення полотна
            canvas = new Canvas(windowWidth, windowHeight);
            graphicsContext = canvas.getGraphicsContext2D();

            // Створення кореня (Group) для сцени
            Group root = new Group();
            root.getChildren().add(canvas); // Додаємо Canvas до Group

            // Створення сцени
            scene = new Scene(root, windowWidth, windowHeight);

            // Налаштування Stage
            primaryStage.setTitle("CatRobber");
            primaryStage.setScene(scene);
            primaryStage.setWidth(windowWidth);
            primaryStage.setHeight(windowHeight);

            // Ініціалізація менеджерів
            initializeManagers();
            handleWindowEvents(); // Налаштовуємо обробку подій вікна
            // Показати вікно
            primaryStage.show();
    }

    // Створює менеджери
    // Ініціалізує GameManager, UIManager, SoundManager, SaveManager, InputHandler
    public void initializeManagers() {
        gameManager = new GameManager();
        uiManager = new UIManager(canvas);
        soundManager = new SoundManager();
        saveManager = new SaveManager();
        inputHandler = new InputHandler(scene);
        inputHandler.setupKeyHandlers();
        inputHandler.setupMouseHandlers();
    }

    // Запускає ігровий цикл
    // Створює AnimationTimer, обчислює deltaTime
    public void startGameLoop() {
        isRunning = true;
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                if (!isRunning) return;
                double deltaTime = (currentNanoTime - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = currentNanoTime;
                update(deltaTime);
                render();
            }
        };
        lastFrameTime = System.nanoTime();
        animationTimer.start();
    }

    // Оновлює гру
    // Отримує deltaTime, передає в GameManager і UIManager
    public void update(double deltaTime) {
        gameManager.update(deltaTime); // Оновлення логіки гри (персонажі, колізії тощо)
    }

    // Рендерить гру
    // Передає GraphicsContext в GameManager і UIManager
    public void render() {
        gameManager.render(graphicsContext);
        uiManager.render(graphicsContext);
    }

    // Обробляє події вікна
    // Налаштовує закриття Stage
    public void handleWindowEvents() {
        primaryStage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Вийти з гри?");
            if (alert.showAndWait().get() != ButtonType.OK) {
                event.consume(); // Скасовуємо закриття, якщо гравець натиснув "Cancel"
            } else {
                cleanup(); // Викликаємо cleanup при підтвердженні виходу
            }
        });
    }

    // Зупиняє гру, очищає ресурси
    // Викликається при закритті
    public void cleanup() {
        isRunning = false; // Зупиняємо ігровий цикл
        animationTimer.stop(); // Зупиняємо AnimationTimer
        gameManager.saveGame(); // Зберігаємо прогрес гри
        soundManager.stopAllSounds(); // Зупиняємо всі звуки
    }
}