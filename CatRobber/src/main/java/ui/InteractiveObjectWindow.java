package ui;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.Border;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.geometry.Insets;
import managers.FontManager;
import managers.GameManager;
import managers.UIManager;
import org.json.JSONObject;
import java.util.Random;

public class InteractiveObjectWindow implements UIWindow {
    private Pane root;
    private UIManager uiManager;
    private UIManager.WindowType windowType;

    public InteractiveObjectWindow(UIManager uiManager, UIManager.WindowType windowType, JSONObject config) {
        this.uiManager = uiManager;
        this.windowType = windowType;
        this.root = new Pane();
        initializeUI(config);
        System.out.println("InteractiveObjectWindow created: " + windowType);
    }

    private void initializeUI(JSONObject config) {
        // БАЗОВЕ ВІКНО БЕЗ НІЧОГО
        root.setPrefSize(400, 300);

        // Заголовок
        Label title = new Label(windowType.toString());
        title.setLayoutX(20);
        title.setLayoutY(20);
        root.getChildren().add(title);

        // Кнопка закриття
        Button closeButton = new Button("Close");
        closeButton.setLayoutX(320);
        closeButton.setLayoutY(250);
        closeButton.setOnAction(e -> {
            System.out.println("Close button clicked: " + windowType);
            uiManager.hideCurrentWindow();
        });
        root.getChildren().add(closeButton);

        // Базовий контент для всіх вікон
        switch (windowType) {
            case NOTE:
                root.setStyle("-fx-background-color: #F5E050; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 3, 3); " +
                        "-fx-border-color: #D4B82A; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5;");

                String randomCode = generateRandomCode();
                GameManager.getInstance().setNoteCode(randomCode); // Store the code
                Label noteContent = new Label(randomCode);
                noteContent.setFont(FontManager.getInstance().getFont("EpsilonCTT", 36));
                noteContent.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C1810;");
                noteContent.setLayoutX(125);
                noteContent.setLayoutY(132);
                root.getChildren().add(noteContent);
                break;

            case PICTURE:
                Label pictureContent = new Label("Picture");
                pictureContent.setLayoutX(20);
                pictureContent.setLayoutY(50);
                root.getChildren().add(pictureContent);
                break;

            case COMPUTER:
                Label computerContent = new Label("Computer");
                computerContent.setLayoutX(20);
                computerContent.setLayoutY(50);
                root.getChildren().add(computerContent);
                break;

            case VICTORY:
            case GAME_OVER:
                Label endGameContent = new Label(windowType == UIManager.WindowType.VICTORY ? "Victory!" : "Game Over");
                endGameContent.setLayoutX(20);
                endGameContent.setLayoutY(100);
                root.getChildren().add(endGameContent);
                break;
        }
    }

    private String generateRandomCode() {
        Random random = new Random();
        int code = random.nextInt(9000) + 1000;
        return String.valueOf(code);
    }

    @Override
    public void show() {
        uiManager.getOverlayPane().getChildren().add(root);
        root.setLayoutX((uiManager.getOverlayPane().getWidth() - root.getPrefWidth()) / 2);
        root.setLayoutY((uiManager.getOverlayPane().getHeight() - root.getPrefHeight()) / 2);
        System.out.println("Window shown: " + windowType);
    }

    @Override
    public void hide() {
        uiManager.getOverlayPane().getChildren().remove(root);
        System.out.println("Window hidden: " + windowType);
    }

    @Override
    public Node getRoot() {
        return root;
    }
}