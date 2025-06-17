package puzzles;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import main.GameWindow;
import managers.FontManager;
import managers.SoundManager;
import org.json.JSONObject;
import utils.GameLoader;

/**
 * Клас для реалізації головоломки зі зламом замка. Наслідує клас Puzzle.
 */
public class LockPickPuzzle extends Puzzle {
    /** Панель для інтерфейсу головоломки. */
    private Pane puzzlePane;

    /** Зображення фону замка. */
    private ImageView lockBackgroundView;

    /** Зображення циліндра замка. */
    private ImageView cylinderView;

    /** Зображення відмички. */
    private ImageView lockpickView;

    /** Прапорець, що вказує, чи відбувається спроба зламу. */
    private boolean isPicking = false;

    /** Швидкість руху циліндра. */
    private final double cylinderSpeed = 7.0;

    /** Мінімальна X-координата зони успіху. */
    private double successZoneMinX;

    /** Максимальна X-координата зони успіху. */
    private double successZoneMaxX;

    /** Прапорець, що вказує, чи натиснута ліва клавіша. */
    private boolean leftPressed = false;

    /** Прапорець, що вказує, чи натиснута права клавіша. */
    private boolean rightPressed = false;

    /** Таймер для обробки руху циліндра. */
    private Timeline inputTimeline;

    /** Напрямок руху циліндра (1 = вправо, -1 = вліво). */
    private int cylinderDirection = 1;

    /** Обробник події натискання клавіші. */
    private EventHandler<KeyEvent> keyPressedHandler;

    /** Обробник події відпускання клавіші. */
    private EventHandler<KeyEvent> keyReleasedHandler;

    /** Менеджер звуків для відтворення звукових ефектів. */
    private final SoundManager soundManager = SoundManager.getInstance();

    /**
     * Конструктор для ініціалізації головоломки зі зламом замка.
     *
     * @param defaultData JSON-об’єкт із початковими даними
     */
    public LockPickPuzzle(JSONObject defaultData) {
        super(defaultData);
        this.solution = "SUCCESS";
    }

    /**
     * Перевіряє результат спроби зламу та оновлює стан головоломки.
     *
     * @param input результат спроби ("SUCCESS", "FAILURE" або "FAILED_BY_ESCAPE")
     */
    @Override
    public void solve(Object input) {
        if (inputTimeline != null) {
            inputTimeline.stop();
        }
        if ("FAILED_BY_ESCAPE".equals(input)) {
            Platform.runLater(() -> GameWindow.getInstance().getUIManager().hidePuzzleUI());
            return;
        }
        if (input.equals(solution)) {
            soundManager.playSound(SoundManager.SoundType.PICK_LOCK_OPEN);
            state = PuzzleState.SOLVED;
            if (callback != null) {
                callback.onPuzzleSolved(true, linkedDoor);
            }
        } else {
            soundManager.playSound(SoundManager.SoundType.PICK_LOCK_CLOSED);
            state = PuzzleState.UNSOLVED;
            if (callback != null) {
                callback.onPuzzleSolved(false, linkedDoor);
            }
        }
        Platform.runLater(() -> GameWindow.getInstance().getUIManager().hidePuzzleUI());
    }

    /**
     * Повертає вузол інтерфейсу для головоломки.
     *
     * @return вузол інтерфейсу (Pane) або панель із повідомленням про помилку
     */
    @Override
    public Node getUI() {
        puzzlePane = new Pane();
        puzzlePane.setPrefSize(1200, 800);
        puzzlePane.setStyle("-fx-background-color: transparent;");
        GameLoader loader = new GameLoader();
        Image lockImage = loader.loadImage("puzzles/pickLock/Lock.png");
        Image cylinderImage = loader.loadImage("puzzles/pickLock/InnerPart.png");
        Image lockpickImage = loader.loadImage("puzzles/pickLock/jiggler.png");
        if (lockImage == null || cylinderImage == null || lockpickImage == null) {
            System.err.println("Не вдалося завантажити зображення для LockPickPuzzle.");
            return new Pane(new javafx.scene.control.Label("Помилка завантаження ресурсів головоломки"));
        }
        lockBackgroundView = new ImageView(lockImage);
        lockBackgroundView.setFitWidth(1200);
        lockBackgroundView.setFitHeight(600);
        lockBackgroundView.setLayoutX((puzzlePane.getPrefWidth() - lockBackgroundView.getFitWidth()) / 2);
        lockBackgroundView.setLayoutY((puzzlePane.getPrefHeight() - lockBackgroundView.getFitHeight()) / 2);
        cylinderView = new ImageView(cylinderImage);
        cylinderView.setFitWidth(300);
        cylinderView.setFitHeight(162);
        cylinderView.setLayoutX(lockBackgroundView.getLayoutX() + 305);
        cylinderView.setLayoutY(lockBackgroundView.getLayoutY() + 290);
        lockpickView = new ImageView(lockpickImage);
        lockpickView.setFitWidth(80);
        lockpickView.setFitHeight(400);
        lockpickView.setLayoutX((puzzlePane.getPrefWidth() - lockpickView.getFitWidth()) / 2 + 40);
        lockpickView.setLayoutY(lockBackgroundView.getLayoutY() + lockBackgroundView.getFitHeight() - 130);
        double spaceStartX = lockBackgroundView.getLayoutX() + 364;
        double spaceEndX = lockBackgroundView.getLayoutX() + lockBackgroundView.getFitWidth() - 305;
        double minX = spaceStartX;
        double maxX = spaceEndX - cylinderView.getFitWidth();
        double movementCorridorCenter = (minX + maxX) / 2.0;
        double successZoneWidth = 40.0;
        double cylinderCenterOffset = cylinderView.getFitWidth() / 2.0;
        successZoneMinX = (movementCorridorCenter + cylinderCenterOffset) - (successZoneWidth / 2.0);
        successZoneMaxX = (movementCorridorCenter + cylinderCenterOffset) + (successZoneWidth / 2.0);
        Label promptLabel = showLockPickPrompt();
        puzzlePane.getChildren().addAll(lockBackgroundView, cylinderView, lockpickView, promptLabel);
        setupInputHandlers();
        return puzzlePane;
    }

    /**
     * Налаштовує обробники вводу для головоломки.
     */
    private void setupInputHandlers() {
        puzzlePane.setFocusTraversable(true);
        keyPressedHandler = event -> {
            if (event.getCode() == KeyCode.SPACE) {
                isPicking = true;
                animateLockpick();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                if (inputTimeline != null) inputTimeline.stop();
                solve("FAILED_BY_ESCAPE");
            }
        };
        keyReleasedHandler = event -> {};
        puzzlePane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
                newScene.addEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
                Platform.runLater(() -> puzzlePane.requestFocus());
            }
            if (oldScene != null) {
                oldScene.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
                oldScene.removeEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
            }
        });
        inputTimeline = new Timeline(new KeyFrame(Duration.millis(16), event -> {
            moveCylinder();
        }));
        inputTimeline.setCycleCount(Timeline.INDEFINITE);
        inputTimeline.play();
    }

    /**
     * Переміщує циліндр замка в межах заданого простору.
     */
    private void moveCylinder() {
        if (isPicking) return;
        double spaceStartX = lockBackgroundView.getLayoutX() + 364;
        double spaceEndX = lockBackgroundView.getLayoutX() + lockBackgroundView.getFitWidth() - 305;
        double minX = spaceStartX;
        double maxX = spaceEndX - cylinderView.getFitWidth();
        double currentX = cylinderView.getLayoutX();
        currentX += cylinderSpeed * cylinderDirection;
        if (currentX >= maxX) {
            currentX = maxX;
            cylinderDirection = -1;
        } else if (currentX <= minX) {
            currentX = minX;
            cylinderDirection = 1;
        }
        cylinderView.setLayoutX(currentX);
    }

    /**
     * Анімує рух відмички для спроби зламу.
     */
    private void animateLockpick() {
        javafx.animation.TranslateTransition moveUp = new javafx.animation.TranslateTransition(Duration.millis(200), lockpickView);
        moveUp.setByY(-150);
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(200));
        javafx.animation.SequentialTransition sequence = new javafx.animation.SequentialTransition(moveUp, pause);
        sequence.setOnFinished(event -> {
            checkResult();
        });
        sequence.play();
    }

    /**
     * Перевіряє результат спроби зламу на основі позиції циліндра.
     */
    private void checkResult() {
        double cylinderCenterX = cylinderView.getLayoutX() + cylinderView.getFitWidth() / 2.0;
        if (cylinderCenterX >= successZoneMinX && cylinderCenterX <= successZoneMaxX) {
            solve("SUCCESS");
        } else {
            isPicking = false;
            solve("FAILURE");
        }
    }

    /**
     * Створює та повертає мітку з підказкою для гравця.
     *
     * @return мітка з текстом підказки
     */
    private Label showLockPickPrompt() {
        Label promptLabel = new Label("Вкиористовуйте пробіл, щоб відкрити");
        promptLabel.setFont(FontManager.getInstance().getFont("Hardpixel", 16));
        promptLabel.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.8); " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 5px;"
        );
        promptLabel.setLayoutX(450);
        promptLabel.setLayoutY(120);
        Platform.runLater(() -> promptLabel.toFront());
        return promptLabel;
    }
}