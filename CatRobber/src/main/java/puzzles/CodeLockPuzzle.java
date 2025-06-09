package puzzles;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import main.GameWindow;
import org.json.JSONObject;

public class CodeLockPuzzle extends Puzzle {
    public CodeLockPuzzle(JSONObject defaultData) {
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
        Pane pane = new Pane();
        // Встановлюємо білий фон
        pane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        // Встановлюємо розміри для видимості
        pane.setPrefSize(300, 200);

        Label label = new Label("CodeLock Puzzle (Test)");
        label.setLayoutX(20);
        label.setLayoutY(20);

        Button solveButton = new Button("Solve");
        solveButton.setLayoutX(20);
        solveButton.setLayoutY(50);
        solveButton.setOnAction(e -> solve("1234"));

        Button closeButton = new Button("Close");
        closeButton.setLayoutX(20);
        closeButton.setLayoutY(80);
        closeButton.setOnAction(e -> GameWindow.getInstance().getUIManager().hidePuzzleUI());

        pane.getChildren().addAll(label, solveButton, closeButton);
        return pane;
    }
}