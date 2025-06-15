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
import org.json.JSONObject;
import utils.GameLoader;

public class LockPickPuzzle extends Puzzle {

    private Pane puzzlePane;
    private ImageView lockBackgroundView;
    private ImageView cylinderView;
    private ImageView lockpickView;

    private boolean isPicking = false;
    private final double cylinderSpeed = 2.0;

    private double successZoneMinX;
    private double successZoneMaxX;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private Timeline inputTimeline;

    private EventHandler<KeyEvent> keyPressedHandler;
    private EventHandler<KeyEvent> keyReleasedHandler;

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
            state = PuzzleState.SOLVED;
            if (callback != null) {
                callback.onPuzzleSolved(true, linkedDoor);
            }
            System.out.println("LockPickPuzzle: Solved!");
        } else {
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
        puzzlePane.setPrefSize(800, 600);
        puzzlePane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

        GameLoader loader = new GameLoader();
        Image lockImage = loader.loadImage("puzzles/pickLock/Lock.png");
        Image cylinderImage = loader.loadImage("puzzles/pickLock/InnerPart.png");
        Image lockpickImage = loader.loadImage("puzzles/pickLock/jiggler.png");

        if (lockImage == null || cylinderImage == null || lockpickImage == null) {
            System.err.println("Не вдалося завантажити зображення для LockPickPuzzle.");
            return new Pane(new javafx.scene.control.Label("Помилка завантаження ресурсів головоломки"));
        }

        lockBackgroundView = new ImageView(lockImage);
        lockBackgroundView.setFitWidth(600);
        lockBackgroundView.setFitHeight(300);
        lockBackgroundView.setLayoutX((puzzlePane.getPrefWidth() - lockBackgroundView.getFitWidth()) / 2);
        lockBackgroundView.setLayoutY((puzzlePane.getPrefHeight() - lockBackgroundView.getFitHeight()) / 2 - 50);

        cylinderView = new ImageView(cylinderImage);
        // <--- ЗМІНЕНО: Ширину зменшено вдвічі, як ви просили
        cylinderView.setFitWidth(150);
        cylinderView.setFitHeight(81);
        cylinderView.setLayoutX(lockBackgroundView.getLayoutX() + 182);
        cylinderView.setLayoutY(lockBackgroundView.getLayoutY() + 145);

        lockpickView = new ImageView(lockpickImage);
        lockpickView.setFitWidth(40);
        lockpickView.setFitHeight(200);
        lockpickView.setLayoutX(((puzzlePane.getPrefWidth() - lockpickView.getFitWidth()) / 2)+20);
        lockpickView.setLayoutY(lockBackgroundView.getLayoutY() + lockBackgroundView.getFitHeight() - 65);

        double lockCenterX = lockBackgroundView.getLayoutX() + lockBackgroundView.getFitWidth() / 2;
        double successZoneWidth = 30;
        successZoneMinX = lockCenterX - successZoneWidth / 2;
        successZoneMaxX = lockCenterX + successZoneWidth / 2;

        puzzlePane.getChildren().addAll(lockBackgroundView, cylinderView, lockpickView);
        setupInputHandlers();
        return puzzlePane;
    }


    private void setupInputHandlers() {
        puzzlePane.setFocusTraversable(true);

        keyPressedHandler = event -> {
            if (isPicking) return; // Ігноруємо рух під час анімації спроби

            if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) {
                leftPressed = true;
            } else if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) {
                rightPressed = true;
            } else if (event.getCode() == KeyCode.SPACE) {
                isPicking = true; // Блокуємо рух циліндра
                leftPressed = false;
                rightPressed = false;
                animateLockpick(); // Починаємо анімацію спроби
            } else if (event.getCode() == KeyCode.ESCAPE) {
                if (inputTimeline != null) inputTimeline.stop();
                solve("FAILED_BY_ESCAPE");
            }
        };

        keyReleasedHandler = event -> {
            if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) {
                leftPressed = false;
            } else if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) {
                rightPressed = false;
            }
        };

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
        if (isPicking) return; // Не рухаємо, якщо йде спроба (натиснуто Space)

        double spaceStartX = lockBackgroundView.getLayoutX() + 182; // Початок "вікна" всередині замка
        double spaceEndX = lockBackgroundView.getLayoutX() + lockBackgroundView.getFitWidth() - 182; // Кінець "вікна"

        // minX - це позиція, коли лівий край циліндра торкається лівої межі простору.
        double minX = spaceStartX;
        // maxX - це позиція, коли правий край циліндра торкається правої межі простору.
        double maxX = spaceEndX - cylinderView.getFitWidth()+32;

        // рух
        double currentX = cylinderView.getLayoutX();

        if (leftPressed && !rightPressed) {
            currentX -= cylinderSpeed;
        } else if (rightPressed && !leftPressed) {
            currentX += cylinderSpeed;
        }

        // Обмежуємо рух, щоб циліндр не виходив за межі "вікна".
        currentX = Math.max(minX, Math.min(currentX, maxX));

        cylinderView.setLayoutX(currentX);
    }




    private void animateLockpick() {
        // Створюємо анімацію руху вгору (0.2 секунди)
        javafx.animation.TranslateTransition moveUp = new javafx.animation.TranslateTransition(Duration.millis(200), lockpickView);
        moveUp.setByY(-100); // Рухаємо на 80 пікселів ВГОРУ

        // Створюємо КОРОТКУ паузу на 0.2 секунди
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(200)); // <-- ЗМІНЕНО З 3000 на 200

        // Об'єднуємо рух і паузу в одну послідовність.
        javafx.animation.SequentialTransition sequence = new javafx.animation.SequentialTransition(moveUp, pause);

        // Встановлюємо дію, яка виконається ПІСЛЯ завершення ВСІЄЇ послідовності
        sequence.setOnFinished(event -> {
            // Перевіряємо результат.
            checkResult();
        });

        // Запускаємо всю послідовність анімацій.
        sequence.play();
    }


    private void checkResult() {
        double cylinderCenterX = cylinderView.getLayoutX() + cylinderView.getFitWidth() / 2;

        if (cylinderCenterX >= successZoneMinX && cylinderCenterX <= successZoneMaxX) {
            solve("SUCCESS");
        } else {
            solve("FAILURE");
        }
    }
}