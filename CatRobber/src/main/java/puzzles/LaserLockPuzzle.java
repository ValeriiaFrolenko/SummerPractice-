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

public class LaserLockPuzzle extends Puzzle {
    private ImageView cutterView;
    GameManager manager = GameManager.getInstance();
    private int isCorrectWireNumber = 0;

    public LaserLockPuzzle(JSONObject defaultData) {
        super(defaultData);
        isCorrectWireNumber = 1;
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
        Pane pane = new Pane();
        pane.setPrefSize(512, 640);
        pane.setBackground(Background.EMPTY);

        GameLoader gameLoader = new GameLoader();
        Image backgroundImage = gameLoader.loadImage("puzzles/shield.png");
        if (backgroundImage != null) {
            ImageView background = new ImageView(backgroundImage);
            background.setFitWidth(512);
            background.setFitHeight(576);
            pane.getChildren().add(background);
        }

        Image cutterOpenImage = gameLoader.loadImage("puzzles/cutter_opened.png");
        Image cutterClosedImage = gameLoader.loadImage("puzzles/cutter_closed.png");
        cutterView = new ImageView(cutterOpenImage);
        cutterView.setFitWidth(770);
        cutterView.setFitHeight(770);
        cutterView.setLayoutX(-270); // Початкова позиція
        cutterView.setLayoutY(270);
        pane.getChildren().add(cutterView);

        ImageView[] wireViews = new ImageView[3];
        Image[] cutWireImages = new Image[3];

        for (int i = 0; i < 3; i++) {
            int wireIndex = i + 1;
            String wirePath = "puzzles/level" + manager.getCurrentLevelId() + "/lazerLock/wire" + wireIndex + ".png";
            String cutWirePath = "puzzles/level" + manager.getCurrentLevelId() + "/lazerLock/wire" + wireIndex + "_cut.png";

            Image wireImage = gameLoader.loadImage(wirePath);
            Image cutWireImage = gameLoader.loadImage(cutWirePath);

            if (wireImage == null || cutWireImage == null) {
                wirePath = "puzzles/level" + manager.getCurrentLevelId() + "/lazerLock/wire" + wireIndex + "correct.png";
                cutWirePath = "puzzles/level" + manager.getCurrentLevelId() + "/lazerLock/wire" + wireIndex + "correct_cut.png";
                wireImage = gameLoader.loadImage(wirePath);
                cutWireImage = gameLoader.loadImage(cutWirePath);

            }

            ImageView wireView = new ImageView(wireImage);
            wireView.setFitWidth(512);
            wireView.setFitHeight(576);
            wireViews[i] = wireView;
            cutWireImages[i] = cutWireImage;

            final int cutterIndexPosition = i;

            cutterView.toFront();

            wireView.setOnMouseEntered(e -> {
                cutterView.setLayoutX(-270 + cutterIndexPosition * 144); // координати
                cutterView.setLayoutY(270);
                cutterView.toFront();
            });

            wireView.setOnMouseClicked(e -> {
                    wireView.setImage(cutWireImages[cutterIndexPosition]);
                    cutterView.setImage(cutterClosedImage);
                    cutterView.toFront();
                    if (wireView == wireViews[isCorrectWireNumber - 1]) {
                        handleWireCut(true);
                    }
                    else{
                        handleWireCut(false);
                    }

                });
                pane.getChildren().add(wireView);
            }
        return pane;
    }

    private void handleWireCut(boolean correct) {
        if (correct) {
            solve(solution); // або solve("ok")
        } else {
            manager.alert();
            solve(solution);
        }

    }
}