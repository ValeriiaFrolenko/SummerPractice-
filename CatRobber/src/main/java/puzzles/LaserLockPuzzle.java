package puzzles;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Background;
import javafx.util.Duration;
import main.GameWindow;
import managers.GameManager;
import org.json.JSONObject;
import utils.GameLoader;

import java.io.FileWriter;
import java.io.IOException;

public class LaserLockPuzzle extends Puzzle {
    private ImageView cutterView;
    private final GameManager manager = GameManager.getInstance();

    public LaserLockPuzzle(JSONObject defaultData) {
        super(defaultData);
    }

    @Override
    public void solve(Object input) {
        if (input.equals(solution)) {
            state = PuzzleState.SOLVED;
            if (callback != null) {
                callback.onPuzzleSolved(true, linkedDoor);
            }
        }
    }

    @Override
    public Node getUI() {
        if (state == PuzzleState.SOLVED) {
            return null;
        }

        Pane pane = new Pane();
        pane.setPrefSize(512, 640);
        pane.setBackground(Background.EMPTY);
        pane.setFocusTraversable(true);
        pane.setMouseTransparent(false);

        pane.setOnMouseClicked(e -> {
            System.out.println("LaserLockPuzzle main pane clicked");
            pane.requestFocus();
            e.consume();
        });

        GameLoader gameLoader = new GameLoader();
        Image backgroundImage = gameLoader.loadImage("puzzles/laserLock/shield.png");
        if (backgroundImage != null) {
            ImageView background = new ImageView(backgroundImage);
            background.setFitWidth(512);
            background.setFitHeight(576);
            background.setMouseTransparent(true);
            pane.getChildren().add(background);
        }

        // Кнопка закриття
        Button closeButton = new Button("✖");
        closeButton.setStyle("-fx-font-size: 13; -fx-background-color: red; -fx-text-fill: white;");
        closeButton.setLayoutX(512 - 35);
        closeButton.setLayoutY(4);
        closeButton.setOnAction(e -> GameWindow.getInstance().getUIManager().hidePuzzleUI());
        pane.getChildren().add(closeButton);

        Image cutterOpenImage = gameLoader.loadImage("puzzles/laserLock/cutter_opened.png");
        Image cutterClosedImage = gameLoader.loadImage("puzzles/laserLock/cutter_closed.png");

        if(cutterOpenImage != null) {
            cutterView = new ImageView(cutterOpenImage);
            cutterView.setFitWidth(770);
            cutterView.setFitHeight(770);
            cutterView.setLayoutX(-270);
            cutterView.setLayoutY(270);
            cutterView.setMouseTransparent(true);
            pane.getChildren().add(cutterView);
        }

        ImageView[] wireViews = new ImageView[3];
        Image[] cutWireImages = new Image[3];

        for (int i = 0; i < 3; i++) {
            int wireIndex = i + 1;
            String wirePath = "puzzles/laserLock/level" + manager.getCurrentLevelId() + "/wire" + wireIndex + ".png";
            String cutWirePath = "puzzles/laserLock/level" + manager.getCurrentLevelId() + "/wire" + wireIndex + "_cut.png";

            Image wireImage = gameLoader.loadImage(wirePath);
            Image cutWireImage = gameLoader.loadImage(cutWirePath);

            if (wireImage != null && cutWireImage != null) {
                ImageView wireView = new ImageView(wireImage);
                wireView.setFitWidth(512);
                wireView.setFitHeight(576);
                wireViews[i] = wireView;
                cutWireImages[i] = cutWireImage;

                final int cutterIndexPosition = i;

                wireView.setOnMouseEntered(e -> {
                    cutterView.setLayoutX(-270 + cutterIndexPosition * 144);
                    cutterView.setLayoutY(270);
                    cutterView.toFront();
                });

                wireView.setOnMouseClicked(e -> {
                    wireView.setImage(cutWireImages[cutterIndexPosition]);
                    cutterView.setImage(cutterClosedImage);
                    cutterView.toFront();

                    if (wireView == wireViews[(int) solution - 1]) {
                        handleWireCut(true);
                    } else {
                        handleWireCut(false);
                    }
                    e.consume();

                });

                pane.getChildren().add(wireView);
            }
        }

        if (cutterView != null) {
            cutterView.toFront();
        }

        javafx.application.Platform.runLater(() -> {
            pane.requestFocus();
            System.out.println("LaserLockPuzzle focus requested");
        });

        return pane;
    }

    private void handleWireCut(boolean correct) {
        if (correct) {
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(e -> {
                solve(solution);
            });
            pause.play();
        } else {
            manager.alert();
            PauseTransition pause = new PauseTransition(Duration.millis(1000));
            pause.setOnFinished(e -> {
                solve(solution);
            });
            pause.play();
        }
    }
}
