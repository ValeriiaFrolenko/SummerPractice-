package puzzles;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import org.json.JSONObject;

public class CodeLockPuzzle extends Puzzle {
    private String imagePath = "assets/images/puzzles/codeLock/codeLock.png";
    private String solution; // Код із нотатки
    private StringBuilder enteredCode; // Введений код
    private Label codeDisplay; // Відображення введених цифр
    private Label attemptsLabel; // Відображення спроб
    private Pane pane; // Зберігаємо для фокусу
    private static int globalAttempts = 3; // Статична змінна для збереження спроб між відкриттями
    private int imageWidth = 400;
    private int imageHeight = 300;

    public CodeLockPuzzle(JSONObject defaultData) {
        super(defaultData);
        String code = GameManager.getInstance().getNoteCode();
        this.solution = (code != null) ? code : String.valueOf(0000);
        this.enteredCode = new StringBuilder();
    }

    @Override
    public void solve(Object input) {
        if (input.equals(solution)) {
            state = PuzzleState.SOLVED;
            if (callback != null) {
                callback.onPuzzleSolved(true, linkedDoor);
            }
            System.out.println("CodeLockPuzzle solved with code: " + input);
        } else {
            globalAttempts--;
            updateAttemptsDisplay();
            enteredCode.setLength(0); // Очищаємо при невірному коді
            updateCodeDisplay();
            System.out.println("Incorrect code entered: " + input + ", attempts left: " + globalAttempts);

            if (globalAttempts <= 0) {
                triggerGlobalAlarm();
                // Скидаємо спроби після тривоги
                globalAttempts = 3;
                // Закриваємо вікно після спрацювання тривоги
                GameWindow.getInstance().getUIManager().hidePuzzleUI();
            }
        }
    }

    private void triggerGlobalAlarm() {
        GameManager.getInstance().alert();
    }

    // Метод для ручного скидання спроб (якщо потрібно з інших частин гри)
    public static void resetAttempts() {
        globalAttempts = 3;
        System.out.println("Спроби скинуто вручну");
    }

    private void updateCodeDisplay() {
        // Відображаємо введені цифри з підкресленнями для порожніх позицій
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

    private void updateAttemptsDisplay() {
        attemptsLabel.setText("Спроби: " + globalAttempts);
    }

    @Override
    public Node getUI() {
        pane = new Pane();
        pane.setPrefSize(imageWidth, imageHeight); // Розмір точно як зображення

        // Завантажуємо зображення кодового замка
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
        lockView.setPreserveRatio(false); // Точно відповідає розміру
        pane.getChildren().add(lockView);

        // Хрестик для закриття - в межах зображення
        Button closeButton = new Button("✖");
        closeButton.setStyle("-fx-font-size: 14; -fx-background-color: red; -fx-text-fill: white;");
        closeButton.setLayoutX(imageWidth - 34); // В межах зображення
        closeButton.setLayoutY(2);
        closeButton.setOnAction(e -> {
            System.out.println("CodeLockPuzzle closed");
            GameWindow.getInstance().getUIManager().hidePuzzleUI();
        });
        pane.getChildren().add(closeButton);

        // Дисплей для введених цифр - зверху з відступом 50 пікселів
        codeDisplay = new Label("");
        codeDisplay.setFont(FontManager.getInstance().getFont("DS-Digital", 36));
        codeDisplay.setTextFill(Color.RED);
        codeDisplay.setStyle("-fx-font-weight: bold;");
        codeDisplay.setLayoutX(imageWidth / 2 - 80); // Центруємо по горизонталі
        codeDisplay.setLayoutY(50); // Відступ зверху 50 пікселів
        updateCodeDisplay(); // Ініціалізуємо відображення
        pane.getChildren().add(codeDisplay);

        // Лічильник спроб
        attemptsLabel = new Label("Спроби: " + globalAttempts);
        attemptsLabel.setFont(FontManager.getInstance().getFont("DS-Digital", 20));
        attemptsLabel.setTextFill(Color.YELLOW);
        attemptsLabel.setStyle("-fx-font-weight: bold;");
        attemptsLabel.setLayoutX(10);
        attemptsLabel.setLayoutY(imageHeight - 30); // Внизу зліва
        pane.getChildren().add(attemptsLabel);

        // Очищуємо введений код при кожному відкритті (але зберігаємо спроби)
        enteredCode.setLength(0);
        updateCodeDisplay();

        // Гарантуємо фокус
        pane.setFocusTraversable(true);
        pane.requestFocus();
        pane.setMouseTransparent(false); // Забезпечуємо, що pane приймає події миші
        pane.setOnMouseClicked(e -> {
            System.out.println("Mouse clicked on CodeLockPuzzle pane at (" + e.getX() + ", " + e.getY() + ")");
            pane.requestFocus(); // Повертаємо фокус при кліку
        });
        pane.setOnMouseMoved(e -> {
            System.out.println("Mouse moved on CodeLockPuzzle pane at (" + e.getX() + ", " + e.getY() + ")");
        });
        // Обробка клавіатури
        pane.setOnKeyPressed(event -> {

            KeyCode key = event.getCode();

            if (key.isDigitKey() && enteredCode.length() < 4) {
                String digit = event.getText();
                enteredCode.append(digit);
                updateCodeDisplay();
                System.out.println("Введено цифру: " + digit + ", поточний код: " + enteredCode);

                // Перевіряємо код лише після затримки, щоб 4-та цифра встигла відобразитися
                if (enteredCode.length() == 4) {
                    // Використовуємо Timeline для затримки без блокування UI
                    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(300), e -> {
                        solve(enteredCode.toString());
                    }));
                    timeline.play();
                }
            } else if (key == KeyCode.BACK_SPACE && enteredCode.length() > 0) {
                enteredCode.deleteCharAt(enteredCode.length() - 1);
                updateCodeDisplay();
                System.out.println("Видалено цифру, поточний код: " + enteredCode);
            }
        });

        // Додаємо слухач для гарантії фокусу після відображення
        pane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Встановлюємо фокус відразу після додавання до сцени
                javafx.application.Platform.runLater(() -> {
                    pane.requestFocus();
                    System.out.println("Фокус встановлено на pane автоматично");
                });

                newScene.getWindow().showingProperty().addListener((showObs, wasShowing, isShowing) -> {
                    if (isShowing) {
                        javafx.application.Platform.runLater(() -> {
                            pane.requestFocus();
                            System.out.println("Фокус встановлено на pane після показу вікна");
                        });
                    }
                });
            }
        });

        return pane;
    }
}