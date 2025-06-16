package puzzles;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import main.GameWindow;
import managers.SoundManager;
import org.json.JSONObject;
import utils.GameLoader;

public class LockPickPuzzle extends Puzzle {

    private Pane puzzlePane;
    private ImageView lockBackgroundView;
    private ImageView cylinderView;
    private ImageView lockpickView;

    private boolean isPicking = false;
    private final double cylinderSpeed = 7.0;

    private double successZoneMinX;
    private double successZoneMaxX;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private Timeline inputTimeline;

    private int cylinderDirection = 1; // 1 = вправо, -1 = вліво


    private EventHandler<KeyEvent> keyPressedHandler;
    private EventHandler<KeyEvent> keyReleasedHandler;

    private final SoundManager soundManager = SoundManager.getInstance();

    public LockPickPuzzle(JSONObject defaultData) {
        super(defaultData);
        this.solution = "SUCCESS";
    }

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
            System.out.println("LockPickPuzzle: Solved!");
        } else {
            soundManager.playSound(SoundManager.SoundType.PICK_LOCK_CLOSED);
            state = PuzzleState.UNSOLVED;
            if (callback != null) {
                callback.onPuzzleSolved(false, linkedDoor);
            }
            System.out.println("LockPickPuzzle: Failed!");
        }
        Platform.runLater(() -> GameWindow.getInstance().getUIManager().hidePuzzleUI());
    }



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

        // Розраховуємо крайні точки для позиції циліндра
        double minX = spaceStartX;
        double maxX = spaceEndX - cylinderView.getFitWidth();

        //  Знаходимо ЦЕНТР
        double movementCorridorCenter = (minX + maxX) / 2.0;

        // Розраховуємо зону успіху відносно ЦЕНТРУ РУХУ
        double successZoneWidth = 40.0;
        double cylinderCenterOffset = cylinderView.getFitWidth() / 2.0;
        successZoneMinX = (movementCorridorCenter + cylinderCenterOffset) - (successZoneWidth / 2.0);
        successZoneMaxX = (movementCorridorCenter + cylinderCenterOffset) + (successZoneWidth / 2.0);

        puzzlePane.getChildren().addAll(lockBackgroundView, cylinderView, lockpickView);
        setupInputHandlers();
        return puzzlePane;
    }


    private void setupInputHandlers() {
        puzzlePane.setFocusTraversable(true);

        keyPressedHandler = event -> {


            if (event.getCode() == KeyCode.SPACE) {
                isPicking = true; // Зупиняємо рух циліндра на час анімації
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



    private void moveCylinder() {
        if (isPicking) return;

        // Межі "віконця" всередині замка
        double spaceStartX = lockBackgroundView.getLayoutX() + 364;
        double spaceEndX = lockBackgroundView.getLayoutX() + lockBackgroundView.getFitWidth() - 305;

        // Правильний розрахунок крайніх точок для позиції (лівого краю) циліндра
        double minX = spaceStartX;
        double maxX = spaceEndX - cylinderView.getFitWidth(); // Кінець вікна мінус ширина циліндра

        // Рух циліндра
        double currentX = cylinderView.getLayoutX();
        currentX += cylinderSpeed * cylinderDirection;

        // Перевірка на зіткнення зі стінками
        if (currentX >= maxX) {
            currentX = maxX;
            cylinderDirection = -1; // Змінюємо напрямок
        } else if (currentX <= minX) {
            currentX = minX;
            cylinderDirection = 1; // Змінюємо напрямок
        }

        cylinderView.setLayoutX(currentX);
    }

    private void animateLockpick() {
        // Створюємо анімацію руху вгору
        javafx.animation.TranslateTransition moveUp = new javafx.animation.TranslateTransition(Duration.millis(200), lockpickView);
        moveUp.setByY(-150); // Рухаємо на 150 пікселів ВГОРУ

        // Створюємо КОРОТКУ паузу на 0.2 секунди
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(200)); // <-- ЗМІНЕНО З 3000 на 200

        // Об'єднуємо рух і паузу в одну послідовність.
        javafx.animation.SequentialTransition sequence = new javafx.animation.SequentialTransition(moveUp, pause);

        // Встановлюємо дію, яка виконається ПІСЛЯ завершення ВСІЄЇ послідовності
        sequence.setOnFinished(event -> {
            checkResult();
        });

        // Запускаємо всю послідовність анімацій.
        sequence.play();
    }



    private void checkResult() {
        double cylinderCenterX = cylinderView.getLayoutX() + cylinderView.getFitWidth() / 2.0;

        System.out.println("Спроба! Центр циліндра: " + cylinderCenterX);

        if (cylinderCenterX >= successZoneMinX && cylinderCenterX <= successZoneMaxX) {
            solve("SUCCESS");
        } else {
            isPicking = false; // Дозволяємо циліндру рухатись знову
            solve("FAILURE");
        }
    }
}