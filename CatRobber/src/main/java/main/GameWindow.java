package main;

import entities.Player;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import managers.*;
import utils.InputHandler;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.animation.AnimationTimer;

public class GameWindow {
    // Поля
    private static GameWindow instance; // Singleton-екземпляр
    private Stage primaryStage;
    private Scene scene;
    private Canvas canvas;
    private GraphicsContext graphicsContext;
    private GameManager gameManager;
    private UIManager uiManager;
    private SoundManager soundManager;
    private SaveManager saveManager;
    private InputHandler inputHandler;
    private AnimationTimer animationTimer;
    private boolean isRunning;
    private long lastFrameTime;

    // Конструктор
    public GameWindow(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setResizable(false);
        instance = this; // Зберігаємо екземпляр
    }

    // Повертає єдиний екземпляр GameWindow
    public static GameWindow getInstance() {
        if (instance == null) {
            throw new IllegalStateException("GameWindow не ініціалізовано");
        }
        return instance;
    }

    // Налаштовує вікно, сцену, canvas і менеджери
    public void initialize() {
        canvas = new Canvas(1280, 640);
        graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setImageSmoothing(false);

        FontManager.getInstance().initializeFonts();

        gameManager = GameManager.getInstance();
        uiManager = new UIManager(canvas);
        soundManager = new SoundManager();
        saveManager = new SaveManager();

        Group root = new Group();
        root.getChildren().add(canvas);
        root.getChildren().add(uiManager.getOverlayPane());

        scene = new Scene(root, 1280, 640);
        primaryStage.setTitle("CatRobber");
        primaryStage.setScene(scene);
        primaryStage.show();

        inputHandler = new InputHandler(scene);
        inputHandler.setupKeyHandlers();
        inputHandler.setupMouseHandlers();
        // Реєструємо колбек для взаємодії
        gameManager.registerInteractionCallback(inputHandler);

        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());

        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            gameManager.updateBackgroundScale(newVal.doubleValue(), scene.getHeight());
        });
        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            gameManager.updateBackgroundScale(scene.getWidth(), newVal.doubleValue());
        });

        handleWindowEvents();

        startNewGame(1);

        gameManager.updateBackgroundScale(scene.getWidth(), scene.getHeight());
    }

    // Ініціалізує менеджери гри
    public void initializeManagers() {

        gameManager = GameManager.getInstance();
        uiManager = new UIManager(canvas);
        soundManager = new SoundManager();
        saveManager = new SaveManager();
        inputHandler = new InputHandler(scene);
        inputHandler.setupKeyHandlers();
        inputHandler.setupMouseHandlers();
    }

    // Повертає UIManager
    public UIManager getUIManager() {
        return uiManager;
    }

    // Решта методів без змін
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

    public void update(double deltaTime) {
        gameManager.update(deltaTime);
        gameManager.handleInput(inputHandler, deltaTime);
    }

    public void render() {
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gameManager.render(graphicsContext);
        gameManager.drawRoomOutlines(graphicsContext);
        uiManager.render(graphicsContext);
    }

    public void handleWindowEvents() {
        primaryStage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Вийти з гри?");
            if (alert.showAndWait().get() != ButtonType.OK) {
                event.consume();
            } else {
                cleanup();
            }
        });
    }

    public void cleanup() {
        isRunning = false;
        animationTimer.stop();
        gameManager.saveGame();
        soundManager.stopAllSounds();
    }

    private void startNewGame(int levelId) {
        gameManager.loadLevel(levelId, true);
        gameManager.setGameState(GameManager.GameState.PLAYING); // Явно встановлюємо PLAYING
        // Додаємо збереження після створення нового рівня
        gameManager.saveGame();    }

    private void loadSavedGame(int levelId) {
        gameManager.loadLevel(levelId, false);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}