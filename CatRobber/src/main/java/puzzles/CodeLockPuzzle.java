package puzzles;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import managers.FontManager;
import main.GameWindow;
import managers.GameManager;
import managers.SoundManager;
import org.json.JSONObject;

/**
 * Клас для реалізації головоломки з кодовим замком. Наслідує клас Puzzle.
 */
public class CodeLockPuzzle extends Puzzle {
    /** Шлях до зображення кодового замка. */
    private String imagePath = "assets/images/puzzles/codeLock/codeLock.png";

    /** Рішення головоломки (правильний код). */
    private String solution;

    /** Введений користувачем код. */
    private StringBuilder enteredCode;

    /** Мітка для відображення введених цифр. */
    private Label codeDisplay;

    /** Мітка для відображення кількості спроб. */
    private Label attemptsLabel;

    /** Панель для інтерфейсу головоломки. */
    private Pane pane;

    /** Статична змінна для збереження кількості спроб між відкриттями. */
    private static int globalAttempts = 3;

    /** Ширина зображення кодового замка. */
    private int imageWidth = 400;

    /** Висота зображення кодового замка. */
    private int imageHeight = 300;

    /** Менеджер звуків для відтворення звукових ефектів. */
    private final SoundManager soundManager = SoundManager.getInstance();

    /**
     * Конструктор для ініціалізації головоломки з кодовим замком.
     *
     * @param defaultData JSON-об’єкт із початковими даними
     */
    public CodeLockPuzzle(JSONObject defaultData) {
        super(defaultData);
        String code = GameManager.getInstance().getCode();
        this.solution = (code != null) ? code : String.valueOf(0000);
        this.enteredCode = new StringBuilder();
    }

    /**
     * Перевіряє введений код і оновлює стан головоломки.
     *
     * @param input введений код
     */
    @Override
    public void solve(Object input) {
        if (input.equals(solution)) {
            soundManager.playSound(SoundManager.SoundType.CODE_LOCK_OPEN);
            state = PuzzleState.SOLVED;
            if (callback != null) {
                callback.onPuzzleSolved(true, linkedDoor);
            }
        } else {
            soundManager.playSound(SoundManager.SoundType.CODE_LOCK_CLOSED);
            globalAttempts--;
            updateAttemptsDisplay();
            enteredCode.setLength(0);
            updateCodeDisplay();
            if (globalAttempts <= 0) {
                triggerGlobalAlarm();
                globalAttempts = 3;
                GameWindow.getInstance().getUIManager().hidePuzzleUI();
            }
        }
    }

    /**
     * Викликає глобальну тривогу в грі.
     */
    private void triggerGlobalAlarm() {
        GameManager.getInstance().alert();
    }

    /**
     * Скидає кількість спроб до початкового значення (3).
     */
    public static void resetAttempts() {
        globalAttempts = 3;
    }

    /**
     * Оновлює відображення введеного коду на екрані.
     */
    private void updateCodeDisplay() {
        StringBuilder display = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (i < enteredCode.length()) {
                display.append(enteredCode.charAt(i));
            } else {
                display.append("_");
            }
            if (i < 3) display.append(" ");
        }
        codeDisplay.setText(display.toString());
    }

    /**
     * Оновлює відображення кількості спроб.
     */
    private void updateAttemptsDisplay() {
        attemptsLabel.setText("Спроби: " + globalAttempts);
    }

    /**
     * Забезпечує фокус на панелі головоломки.
     */
    private void ensureFocus() {
        if (pane != null) {
            javafx.application.Platform.runLater(() -> {
                pane.requestFocus();
            });
        }
    }

    /**
     * Повертає вузол інтерфейсу для головоломки.
     *
     * @return вузол інтерфейсу (Pane)
     */
    @Override
    public Node getUI() {
        pane = new Pane();
        pane.setPrefSize(imageWidth, imageHeight);
        Image lockImage;
        try {
            lockImage = new Image("file:assets/images/puzzles/codeLock/codeLock.png");
            if (lockImage.getWidth() > 0) {
                imageWidth = (int) lockImage.getWidth();
                imageHeight = (int) lockImage.getHeight();
                pane.setPrefSize(imageWidth, imageHeight);
            }
        } catch (Exception e) {
            System.err.println("Не вдалося завантажити зображення: " + imagePath + ": " + e.getMessage());
            lockImage = null;
        }
        ImageView lockView = new ImageView(lockImage);
        lockView.setFitWidth(imageWidth);
        lockView.setFitHeight(imageHeight);
        lockView.setPreserveRatio(false);
        pane.getChildren().add(lockView);
        Button closeButton = new Button("✖");
        closeButton.setStyle("-fx-font-size: 14; -fx-background-color: red; -fx-text-fill: white;");
        closeButton.setLayoutX(imageWidth - 34);
        closeButton.setLayoutY(2);
        closeButton.setOnAction(e -> {
            GameWindow.getInstance().getUIManager().hidePuzzleUI();
        });
        closeButton.setFocusTraversable(false);
        pane.getChildren().add(closeButton);
        codeDisplay = new Label("");
        codeDisplay.setFont(FontManager.getInstance().getFont("DS-Digital", 36));
        codeDisplay.setTextFill(Color.RED);
        codeDisplay.setStyle("-fx-font-weight: bold;");
        codeDisplay.setLayoutX(imageWidth / 2 - 80);
        codeDisplay.setLayoutY(50);
        codeDisplay.setFocusTraversable(false);
        updateCodeDisplay();
        pane.getChildren().add(codeDisplay);
        attemptsLabel = new Label("Спроби: " + globalAttempts);
        attemptsLabel.setFont(FontManager.getInstance().getFont("DS-Digital", 20));
        attemptsLabel.setTextFill(Color.YELLOW);
        attemptsLabel.setStyle("-fx-font-weight: bold;");
        attemptsLabel.setLayoutX(10);
        attemptsLabel.setLayoutY(imageHeight - 30);
        attemptsLabel.setFocusTraversable(false);
        pane.getChildren().add(attemptsLabel);
        enteredCode.setLength(0);
        updateCodeDisplay();
        pane.setFocusTraversable(true);
        pane.setStyle("-fx-background-color: transparent;");
        pane.setOnMouseClicked(e -> {
            pane.requestFocus();
            e.consume();
        });
        pane.setOnKeyPressed(event -> {
            soundManager.playSound(SoundManager.SoundType.CODE_LOCK_CLICK);
            KeyCode key = event.getCode();
            if (key.isDigitKey() && enteredCode.length() < 4) {
                String digit = event.getText();
                enteredCode.append(digit);
                updateCodeDisplay();
                if (enteredCode.length() == 4) {
                    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(300), e -> {
                        solve(enteredCode.toString());
                    }));
                    timeline.play();
                }
            } else if (key == KeyCode.BACK_SPACE && enteredCode.length() > 0) {
                enteredCode.deleteCharAt(enteredCode.length() - 1);
                updateCodeDisplay();
            }
            event.consume();
        });
        pane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Timeline focusTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
                    ensureFocus();
                }));
                focusTimeline.play();
                newScene.getWindow().showingProperty().addListener((showObs, wasShowing, isShowing) -> {
                    if (isShowing) {
                        Timeline windowFocusTimeline = new Timeline(new KeyFrame(Duration.millis(200), e -> {
                            ensureFocus();
                        }));
                        windowFocusTimeline.play();
                    }
                });
            }
        });
        pane.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                Timeline refocusTimeline = new Timeline(new KeyFrame(Duration.millis(50), e -> {
                    if (!pane.isFocused()) {
                        ensureFocus();
                    }
                }));
                refocusTimeline.play();
            }
        });
        Timeline initialFocusTimeline = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            ensureFocus();
        }));
        initialFocusTimeline.play();
        return pane;
    }
}