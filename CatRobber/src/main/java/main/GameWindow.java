package main;

import entities.Player;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
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

public class GameWindow {
    // Поля
    private Stage primaryStage; // Головне вікно JavaFX, отримане від GameApplication
    private Scene scene; // Сцена JavaFX, містить canvas для рендерингу
    private Canvas canvas; // Полотно для малювання гри
    private GraphicsContext graphicsContext; // Контекст для рендерингу на canvas
    private GameManager gameManager; // Менеджер гри (логіка, рівні, сутності)
    private UIManager uiManager; // Менеджер UI (інтерфейс, меню)
    private SoundManager soundManager; // Менеджер звуку
    private SaveManager saveManager; // Менеджер збереження гри
    private InputHandler inputHandler; // Обробник вводу (клавіатура, миша)
    private AnimationTimer animationTimer; // Таймер для ігрового циклу
    private boolean isRunning; // Прапорець роботи ігрового циклу
    private long lastFrameTime; // Час останнього кадру для обчислення deltaTime

    // Конструктор, ініціалізує primaryStage
    public GameWindow(Stage primaryStage) {
        this.primaryStage = primaryStage; // Зберігаємо головне вікно
        primaryStage.setResizable(false); // Вимикаємо зміну розміру вікна
    }

    // Налаштовує вікно, сцену, canvas і менеджери
    public void initialize() {
        // Створюємо canvas із фіксованими розмірами 1280x640
        canvas = new Canvas(1280, 640);
        graphicsContext = canvas.getGraphicsContext2D(); // Отримуємо контекст для малювання
        graphicsContext.setImageSmoothing(false); // Вимикаємо згладжування для піксельної графіки

        // Створюємо корінь (Group) для сцени
        Group root = new Group();
        root.getChildren().add(canvas); // Додаємо canvas до кореня

        // Створюємо сцену з розмірами 1280x640
        scene = new Scene(root, 1280, 640);
        primaryStage.setTitle("CatRobber"); // Встановлюємо заголовок вікна
        primaryStage.setScene(scene); // Встановлюємо сцену
        primaryStage.show(); // Показуємо вікно

        // Прив'язуємо розміри canvas до розмірів сцени
        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());

        // Оновлюємо масштаб фону при зміні розміру сцени
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            gameManager.updateBackgroundScale(newVal.doubleValue(), scene.getHeight()); // Оновлюємо масштаб фону
        });
        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            gameManager.updateBackgroundScale(scene.getWidth(), newVal.doubleValue()); // Оновлюємо масштаб фону
        });

        // Ініціалізуємо менеджери
        initializeManagers();
        handleWindowEvents();

        // Починаємо нову гру з рівнем 1
        startNewGame(1);

        // Оновлюємо масштаб фону для початкових розмірів сцени
        gameManager.updateBackgroundScale(scene.getWidth(), scene.getHeight());
    }

    // Ініціалізує менеджери гри
    public void initializeManagers() {
        gameManager = GameManager.getInstance(); // Отримуємо singleton-екземпляр GameManager
        uiManager = new UIManager(canvas); // Створюємо UIManager, передаючи canvas
        soundManager = new SoundManager(); // Створюємо SoundManager
        saveManager = new SaveManager(); // Створюємо SaveManager
        inputHandler = new InputHandler(scene); // Створюємо InputHandler, передаючи сцену
        inputHandler.setupKeyHandlers(); // Налаштовуємо обробники клавіатури
        inputHandler.setupMouseHandlers(); // Налаштовуємо обробники миші
    }

    // Запускає ігровий цикл
    public void startGameLoop() {
        isRunning = true; // Встановлюємо прапорець роботи
        animationTimer = new AnimationTimer() { // Створюємо таймер для циклу
            @Override
            public void handle(long currentNanoTime) {
                if (!isRunning) return; // Виходимо, якщо гра зупинена
                double deltaTime = (currentNanoTime - lastFrameTime) / 1_000_000_000.0; // Обчислюємо час між кадрами
                lastFrameTime = currentNanoTime; // Оновлюємо час останнього кадру
                update(deltaTime); // Оновлюємо логіку гри
                render(); // Рендеримо кадр
            }
        };
        lastFrameTime = System.nanoTime(); // Ініціалізуємо початковий час
        animationTimer.start(); // Запускаємо таймер
    }

    // Оновлює логіку гри
    public void update(double deltaTime) {
        gameManager.update(deltaTime); // Оновлюємо стан гри (сутності, логіку)
        gameManager.handleInput(inputHandler, deltaTime); // Обробляємо ввід користувача
    }

    // Рендерить кадр
    public void render() {
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Очищаємо canvas
        gameManager.render(graphicsContext); // Рендеримо гру (фон, сутності)
        gameManager.drawRoomOutlines(graphicsContext); // Малюємо контури кімнат
        uiManager.render(graphicsContext); // Рендеримо UI
    }

    // Налаштовує обробку подій вікна
    public void handleWindowEvents() {
        primaryStage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Вийти з гри?"); // Показуємо діалог підтвердження
            if (alert.showAndWait().get() != ButtonType.OK) {
                event.consume(); // Скасовуємо закриття, якщо користувач не підтвердив
            } else {
                cleanup(); // Очищаємо ресурси при підтвердженні
            }
        });
    }

    // Очищає ресурси гри
    public void cleanup() {
        isRunning = false; // Зупиняємо ігровий цикл
        animationTimer.stop(); // Зупиняємо таймер
        gameManager.saveGame(); // Зберігаємо гру
        soundManager.stopAllSounds(); // Зупиняємо всі звуки
    }

    // Починає нову гру
    private void startNewGame(int levelId) {
        gameManager.loadLevel(levelId, true); // Завантажуємо рівень як нову гру
    }

    // Завантажує збережену гру
    private void loadSavedGame(int levelId) {
        gameManager.loadLevel(levelId, false); // Завантажуємо рівень зі збереження
    }
}