package puzzles;

import javafx.animation.PauseTransition;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import main.GameWindow;
import managers.GameManager;
import org.json.JSONObject;
import utils.GameLoader;

import java.util.Objects;
import java.util.Random;

public class LaserLockPuzzle extends Puzzle {
    private ImageView cutterView;
    private GameManager manager = GameManager.getInstance();
    private int isCorrectWireNumber = 0;
    private Pane mainPane;
    private boolean isWireCut = false;

    public LaserLockPuzzle(JSONObject defaultData) {
        super(defaultData);

        // ВИПРАВЛЕНО: Використовуємо ID рівня для визначення правильного проводу
        // Замість рандомного, правильний провід буде однаковий для кожного рівня
        int levelId = manager.getCurrentLevelId();
        isCorrectWireNumber = (levelId % 3) + 1; // Буде 1, 2 або 3 в залежності від рівня

        System.out.println("LaserLockPuzzle created for level " + levelId + ", correct wire: " + isCorrectWireNumber);
    }

    @Override
    public void solve(Object input) {
        if (input.equals(isCorrectWireNumber)) {
            state = PuzzleState.SOLVED;
            if (callback != null) {
                callback.onPuzzleSolved(true, linkedDoor);
            }
            System.out.println("LaserLockPuzzle solved correctly");
        } else {
            System.out.println("LaserLockPuzzle solved incorrectly");
        }
    }

    @Override
    public Node getUI() {
        mainPane = new Pane();
        mainPane.setPrefSize(512, 640);
        mainPane.setBackground(Background.EMPTY);
        mainPane.setFocusTraversable(true);
        mainPane.setMouseTransparent(false);

        // Додаємо обробник кліку для фокусу
        mainPane.setOnMouseClicked(e -> {
            System.out.println("LaserLockPuzzle main pane clicked");
            mainPane.requestFocus();
            e.consume();
        });

        GameLoader gameLoader = new GameLoader();

        // Фонове зображення
        Image backgroundImage = gameLoader.loadImage("puzzles/laserLock/shield.png");
        if (backgroundImage != null) {
            ImageView background = new ImageView(backgroundImage);
            background.setFitWidth(512);
            background.setFitHeight(576);
            background.setMouseTransparent(true); // Фон не перехоплює події миші
            mainPane.getChildren().add(background);
            System.out.println("Background image loaded");
        } else {
            System.err.println("Failed to load background image");
        }

        // Кнопка закриття
        Button closeButton = new Button("✖");
        closeButton.setStyle("-fx-font-size: 14; -fx-background-color: red; -fx-text-fill: white;");
        closeButton.setLayoutX(512 - 34);
        closeButton.setLayoutY(2);
        closeButton.setOnAction(e -> {
            System.out.println("LaserLockPuzzle closed");
            GameWindow.getInstance().getUIManager().hidePuzzleUI();
        });
        mainPane.getChildren().add(closeButton);

        // Різак
        Image cutterOpenImage = gameLoader.loadImage("puzzles/laserLock/cutter_opened.png");
        Image cutterClosedImage = gameLoader.loadImage("puzzles/laserLock/cutter_closed.png");

        if (cutterOpenImage != null) {
            cutterView = new ImageView(cutterOpenImage);
            cutterView.setFitWidth(770);
            cutterView.setFitHeight(770);
            cutterView.setLayoutX(-270);
            cutterView.setLayoutY(270);
            cutterView.setMouseTransparent(true); // Різак не перехоплює події миші
            mainPane.getChildren().add(cutterView);
            System.out.println("Cutter image loaded");
        } else {
            System.err.println("Failed to load cutter image");
        }

        // Проводи
        ImageView[] wireViews = new ImageView[3];
        Image[] cutWireImages = new Image[3];

        for (int i = 0; i < 3; i++) {
            int wireIndex = i + 1;
            String wirePath = "puzzles/laserLock/level" + manager.getCurrentLevelId() + "/wire" + wireIndex + ".png";
            String cutWirePath = "puzzles/laserLock/level" + manager.getCurrentLevelId() + "/wire" + wireIndex + "_cut.png";

            Image wireImage = gameLoader.loadImage(wirePath);
            Image cutWireImage = gameLoader.loadImage(cutWirePath);

            // Якщо основні зображення не знайдені, пробуємо альтернативні
            if (wireImage == null || cutWireImage == null) {
                wirePath = "puzzles/laserLock/level" + manager.getCurrentLevelId() + "/wire" + wireIndex + "correct.png";
                cutWirePath = "puzzles/laserLock/level" + manager.getCurrentLevelId() + "/wire" + wireIndex + "correct_cut.png";
                wireImage = gameLoader.loadImage(wirePath);
                cutWireImage = gameLoader.loadImage(cutWirePath);
            }

            if (wireImage != null && cutWireImage != null) {
                ImageView wireView = new ImageView(wireImage);
                wireView.setFitWidth(512);
                wireView.setFitHeight(576);
                wireView.setMouseTransparent(false); // Проводи повинні реагувати на мишку
                wireViews[i] = wireView;
                cutWireImages[i] = cutWireImage;

                final int wireNumber = wireIndex;
                final int cutterPosition = i;

                // Обробник наведення миші
                wireView.setOnMouseEntered(e -> {
                    if (!isWireCut && cutterView != null) {
                        cutterView.setLayoutX(-270 + cutterPosition * 144);
                        cutterView.setLayoutY(270);
                        cutterView.toFront();
                        System.out.println("Mouse entered wire " + wireNumber + ", cutter moved to position " + cutterPosition);
                    }
                });

                // Обробник виходу миші
                wireView.setOnMouseExited(e -> {
                    System.out.println("Mouse exited wire " + wireNumber);
                });

                // ВИПРАВЛЕНИЙ обробник кліку
                wireView.setOnMouseClicked(e -> {
                    if (!isWireCut) {
                        System.out.println("=== WIRE CLICK DEBUG ===");
                        System.out.println("Clicked wire number: " + wireNumber);
                        System.out.println("Correct wire number: " + isCorrectWireNumber);
                        System.out.println("Are they equal? " + (wireNumber == isCorrectWireNumber));
                        System.out.println("=======================");

                        // Змінюємо зображення проводу на перерізаний
                        wireView.setImage(cutWireImages[cutterPosition]);

                        // Змінюємо зображення різака на закритий
                        if (cutterView != null && cutterClosedImage != null) {
                            cutterView.setImage(cutterClosedImage);
                            cutterView.toFront();
                        }

                        isWireCut = true;

                        // ВИПРАВЛЕНО: правильно обчислюємо boolean та передаємо wireNumber
                        boolean isCorrect = (wireNumber == isCorrectWireNumber);
                        handleWireCut(isCorrect, wireNumber);
                        e.consume();
                    }
                });

                mainPane.getChildren().add(wireView);
                System.out.println("Wire " + wireNumber + " added");
            } else {
                System.err.println("Failed to load wire images for wire " + wireIndex);
            }
        }

        // Переміщуємо різак на передній план
        if (cutterView != null) {
            cutterView.toFront();
        }

        // Встановлюємо фокус
        javafx.application.Platform.runLater(() -> {
            mainPane.requestFocus();
            System.out.println("LaserLockPuzzle focus requested");
        });

        System.out.println("LaserLockPuzzle UI created with " + mainPane.getChildren().size() + " children");
        return mainPane;
    }

    private void handleWireCut(boolean correct, int wireNumber) {
        System.out.println("Wire " + wireNumber + " cut - " + (correct ? "CORRECT" : "INCORRECT"));
        System.out.println("Expected correct wire was: " + isCorrectWireNumber);

        if (correct) {
            // Затримка перед розв'язанням для візуального ефекту
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(e -> {
                solve(isCorrectWireNumber);
            });
            pause.play();
        } else {
            // Спрацьовує тривога
            manager.alert();

            // Затримка перед закриттям
            PauseTransition pause = new PauseTransition(Duration.millis(1000));
            pause.setOnFinished(e -> {
                GameWindow.getInstance().getUIManager().hidePuzzleUI();
            });
            pause.play();
        }
    }
}