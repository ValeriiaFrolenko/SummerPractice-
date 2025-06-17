package main;

import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.Cursor;
import javafx.stage.StageStyle;
import managers.*;
import org.json.JSONObject;
import utils.GameLoader;
import utils.InputHandler;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.animation.AnimationTimer;


/**
 * Керує головним вікном гри, сценою, рендерингом на Canvas та ігровим циклом.
 * Реалізує власний заголовок вікна та обробляє його події.
 */
public class GameWindow {
    private static GameWindow instance; // Singleton-екземпляр
    private Stage primaryStage;
    private Scene scene;
    private Canvas canvas;
    private GraphicsContext graphicsContext;
    private GameManager gameManager;
    private UIManager uiManager;
    private final SoundManager soundManager = SoundManager.getInstance();
    private SaveManager saveManager;
    private InputHandler inputHandler;
    private AnimationTimer animationTimer;
    private boolean isRunning;
    private long lastFrameTime;

    // Нові поля для власного заголовка
    private Rectangle titleBar;
    private Rectangle closeButton;
    private Line closeLine1, closeLine2;
    private Group titleBarGroup; // Group for title bar elements
    private double xOffset = 0;
    private double yOffset = 0;

    /**
     * Конструктор для створення ігрового вікна.
     * @param primaryStage Головна сцена, надана JavaFX.
     * @param gameManager Менеджер ігрової логіки.
     * @param saveManager Менеджер збережень.
     */    public GameWindow(Stage primaryStage, GameManager gameManager, SaveManager saveManager) {
        this.primaryStage = primaryStage;
        this.gameManager = gameManager;
        this.saveManager = saveManager;
        primaryStage.setResizable(false);

        // Прибираємо системний заголовок
        primaryStage.initStyle(StageStyle.UNDECORATED);

        instance = this; // Зберігаємо екземпляр
    }


    /**
     * Повертає єдиний екземпляр класу GameWindow (патерн Singleton).
     * @return Екземпляр GameWindow.
     * @throws IllegalStateException якщо екземпляр ще не було ініціалізовано.
     */
    public static GameWindow getInstance() {
        if (instance == null) {
            throw new IllegalStateException("GameWindow не ініціалізовано");
        }
        return instance;
    }

    /**
     * Створює та налаштовує власний заголовок вікна з кнопкою закриття та можливістю перетягування.
     * @param root Коренева група, до якої буде додано заголовок.
     */
    private void createCustomTitleBar(Group root) {
        titleBarGroup = new Group(); // Окрема група для елементів заголовка
        titleBar = new Rectangle(1280, 30);
        titleBar.setFill(Color.rgb(101, 67, 33));
        titleBar.setStroke(Color.rgb(139, 90, 43));

        closeButton = new Rectangle(1250, 5, 20, 20);
        closeButton.setFill(Color.rgb(139, 69, 19));
        closeButton.setStroke(Color.RED);
        closeButton.setStrokeWidth(1);

        closeLine1 = new Line(1255, 10, 1265, 20);
        closeLine1.setStroke(Color.RED);
        closeLine1.setStrokeWidth(2);

        closeLine2 = new Line(1265, 10, 1255, 20);
        closeLine2.setStroke(Color.RED);
        closeLine2.setStrokeWidth(2);

        closeButton.setOnMouseEntered(e -> {
            closeButton.setFill(Color.rgb(160, 82, 45));
            scene.setCursor(Cursor.HAND);
        });

        closeButton.setOnMouseExited(e -> {
            closeButton.setFill(Color.rgb(139, 69, 19));
            scene.setCursor(Cursor.DEFAULT);
        });

        closeButton.setOnMouseClicked(e -> {
            soundManager.playSound(SoundManager.SoundType.BUTTON_CLICK);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Вийти з гри?");
            if (alert.showAndWait().get() == ButtonType.OK) {
                cleanup();
                primaryStage.close();
            }
            e.consume();
        });

        closeLine1.setOnMouseClicked(e -> {
            soundManager.playSound(SoundManager.SoundType.BUTTON_CLICK);
            closeButton.getOnMouseClicked().handle(e);
        });
        closeLine2.setOnMouseClicked(e -> {
            soundManager.playSound(SoundManager.SoundType.BUTTON_CLICK);
            closeButton.getOnMouseClicked().handle(e);
        });

        titleBar.setOnMousePressed(e -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });

        titleBar.setOnMouseDragged(e -> {
            primaryStage.setX(e.getScreenX() - xOffset);
            primaryStage.setY(e.getScreenY() - yOffset);
        });

        titleBarGroup.getChildren().addAll(titleBar, closeButton, closeLine1, closeLine2);
        root.getChildren().add(titleBarGroup);
    }


    /**
     * Ховає власний заголовок вікна.
     */
    public void hideTitleBar() {
        titleBarGroup.setVisible(false);
        titleBarGroup.setMouseTransparent(true);
    }

    /**
     * Показує власний заголовок вікна.
     */
    public void showTitleBar() {
        titleBarGroup.setVisible(true);
        titleBarGroup.setMouseTransparent(false);
    }

    /**
     * Ініціалізує всі основні компоненти гри: сцену, canvas, менеджери та обробники подій.
     */
    public void initialize() {
        canvas = new Canvas(1280, 640); // Залишаємо оригінальний розмір для фонового зображення
        canvas.setLayoutY(30); // Зміщуємо canvas вниз на 30px для заголовка
        graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setImageSmoothing(false);

        FontManager.getInstance().initializeFonts();

        uiManager = new UIManager(canvas);

        Group root = new Group();

        // Створюємо коричневий фон під всім вікном
        Rectangle background = new Rectangle(1280, 670); // 640 + 30 для заголовка
        background.setFill(Color.rgb(139, 90, 43)); // Коричневий фон
        root.getChildren().add(background);

        root.getChildren().add(canvas);
        createCustomTitleBar(root); // Додаємо заголовок після canvas
        root.getChildren().add(uiManager.getOverlayPane());
        root.getChildren().add(uiManager.getMenuPane());
        root.getChildren().add(UIManager.getInstance().getMenuButtonPane());



        // Створюємо власний заголовок
        createCustomTitleBar(root);

        scene = new Scene(root, 1280, 670); // Збільшуємо висоту сцени на 30px для заголовка
        scene.setFill(Color.rgb(139, 90, 43)); // Коричневий фон сцени
        root.setMouseTransparent(false);
        root.setFocusTraversable(true);
        root.requestFocus();
        primaryStage.setTitle("CatRobber");
        primaryStage.setScene(scene);
        primaryStage.show();

        inputHandler = new InputHandler(scene);
        inputHandler.setupKeyHandlers();
        inputHandler.setupMouseHandlers();
        gameManager.registerInteractionCallback(inputHandler);

        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty().subtract(30)); // Віднімаємо висоту заголовка, але canvas залишається 640px для зображення

        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            gameManager.updateBackgroundScale(newVal.doubleValue(), 640); // Передаємо оригінальну висоту зображення
            // Оновлюємо позицію кнопки закриття
            closeButton.setX(newVal.doubleValue() - 30);
            closeLine1.setStartX(newVal.doubleValue() - 25);
            closeLine1.setEndX(newVal.doubleValue() - 15);
            closeLine2.setStartX(newVal.doubleValue() - 15);
            closeLine2.setEndX(newVal.doubleValue() - 25);
            titleBar.setWidth(newVal.doubleValue());
            background.setWidth(newVal.doubleValue());
        });
        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            gameManager.updateBackgroundScale(scene.getWidth(), 640); // Передаємо оригінальну висоту зображення
            background.setHeight(newVal.doubleValue());
        });

        handleWindowEvents();

        // Початково приховуємо menuPane і overlayPane
        uiManager.getMenuPane().setVisible(false);
        uiManager.getMenuPane().setMouseTransparent(true);
        uiManager.getOverlayPane().setVisible(false);
        uiManager.getOverlayPane().setMouseTransparent(true);

        showMainMenu();
        gameManager.updateBackgroundScale(scene.getWidth(), 640); // Передаємо оригінальну висоту зображення
    }

    /**
     * Повертає менеджер інтерфейсу.
     * @return Екземпляр UIManager.
     */
    public UIManager getUIManager() {
        return uiManager;
    }


    /**
     * Запускає ігровий цикл за допомогою AnimationTimer.
     * Цей цикл викликає методи update() та render() на кожному кадрі.
     */
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

    /**
     * Оновлює логіку гри на кожному кадрі.
     * @param deltaTime Час, що минув з попереднього кадру, в секундах.
     */
    public void update(double deltaTime) {
        gameManager.update(deltaTime);
        gameManager.handleInput(inputHandler, deltaTime);
    }

    /**
     * Рендерить ігровий світ та інтерфейс на canvas.
     */
    public void render() {
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gameManager.render(graphicsContext);
        uiManager.render(graphicsContext);
    }

    public void handleWindowEvents() {
        // Видаляємо стандартний обробник закриття, тепер він в кнопці
    }

    /**
     * Виконує очищення ресурсів при закритті гри.
     * Зупиняє ігровий цикл, зберігає гру та зупиняє всі звуки.
     */
    public void cleanup() {
        isRunning = false;
        if (animationTimer != null) {
            animationTimer.stop();
        }
        gameManager.saveGame();
        soundManager.stopAllSounds();
    }

    private void startNewGame(int levelId) {
        showMainMenu();
        gameManager.setGameState(GameManager.GameState.PLAYING); // Явно встановлюємо PLAYING
        gameManager.saveGame(); // Зберігаємо після створення нового рівня
    }

    private void showMainMenu() {
        JSONObject menuConfig = new JSONObject();
        primaryStage.requestFocus();
        uiManager.createWindow(UIManager.WindowType.MENU, menuConfig);
    }

    private void loadSavedGame(int levelId) {
        gameManager.loadLevel(levelId, false);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Повертає об'єкт сцени JavaFX.
     * @return Екземпляр Scene.
     */
    public Scene getScene() {
        return scene;
    }
}