package ui;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import managers.UIManager;
import org.json.JSONObject;

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
        // Встановлюємо білий фон і розміри
        root.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        root.setPrefSize(400, 300);

        Label title = new Label(windowType.toString());
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        title.setLayoutX(20);
        title.setLayoutY(20);
        root.getChildren().add(title);

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-font-size: 14;");
        closeButton.setLayoutX(320);
        closeButton.setLayoutY(250);
        closeButton.setOnAction(e -> {
            System.out.println("Close button clicked: " + windowType);
            uiManager.hideCurrentWindow();
        });
        root.getChildren().add(closeButton);

        switch (windowType) {
            case NOTE:
                Label noteContent = new Label("This is a note.");
                noteContent.setStyle("-fx-font-size: 14;");
                noteContent.setLayoutX(20);
                noteContent.setLayoutY(50);
                noteContent.setWrapText(true);
                noteContent.setMaxWidth(360);
                root.getChildren().add(noteContent);
                break;
            case PICTURE:
                Label pictureContent = new Label("Picture");
                pictureContent.setStyle("-fx-font-size: 14;");
                pictureContent.setLayoutX(20);
                pictureContent.setLayoutY(50);
                root.getChildren().add(pictureContent);
                break;
            case COMPUTER:
                Label computerContent = new Label("Computer");
                computerContent.setStyle("-fx-font-size: 14;");
                computerContent.setLayoutX(20);
                computerContent.setLayoutY(50);
                root.getChildren().add(computerContent);
                break;
            case VICTORY:
            case GAME_OVER:
                Label endGameContent = new Label(windowType == UIManager.WindowType.VICTORY ? "Victory!" : "Game Over");
                endGameContent.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
                endGameContent.setLayoutX(20);
                endGameContent.setLayoutY(100);
                root.getChildren().add(endGameContent);
                break;
        }
    }

    @Override
    public void show() {
        uiManager.getOverlayPane().getChildren().add(root);
        // Центрування панелі
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