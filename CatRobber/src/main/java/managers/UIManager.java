package managers;

import interfaces.Renderable;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.Node;
import ui.UIWindow;
import ui.InteractiveObjectWindow;
import ui.Menu;
import ui.Settings;
import ui.Shop;
import org.json.JSONObject;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import main.GameWindow;
import java.util.ArrayList;
import java.util.List;

public class UIManager implements Renderable {
    private Canvas canvas;
    private UIWindow currentWindow;
    private List<String> interactionPrompts;
    private Pane overlayPane;
    private Label interactionLabel;

    public enum WindowType { MENU, SETTINGS, SHOP, NOTE, PICTURE, COMPUTER, VICTORY, GAME_OVER }

    public UIManager(Canvas canvas) {
        this.canvas = canvas;
        this.overlayPane = new Pane();
        this.interactionLabel = new Label();
        this.interactionLabel.setBackground(new Background(new BackgroundFill(
                Color.GRAY,
                new CornerRadii(5),
                new Insets(5)
        )));
        this.interactionLabel.setTextFill(Color.BLACK);
        this.interactionLabel.setPadding(new Insets(5));
        this.interactionLabel.setFont(FontManager.getInstance().getFont("Hardpixel", 16));
        this.interactionPrompts = new ArrayList<>();
        // Додаємо обробку клавіш для overlayPane
        overlayPane.setFocusTraversable(true);
        overlayPane.setOnKeyPressed(this::handleInput);
    }

    public UIWindow createWindow(WindowType type, JSONObject config) {
        if (currentWindow != null) {
            System.out.println("Window creation skipped: existing window = " + currentWindow);
            return currentWindow;
        }
        System.out.println("Creating window: " + type);
        GameManager.getInstance().setGameState(GameManager.GameState.PAUSED);
        switch (type) {
            case MENU:
                currentWindow = new Menu(config);
                // Додаємо меню до overlayPane
                if (currentWindow.getRoot() != null) {
                    overlayPane.getChildren().clear();
                    overlayPane.getChildren().add(currentWindow.getRoot());
                    // Меню займає весь екран
                    currentWindow.getRoot().setLayoutX(0);
                    currentWindow.getRoot().setLayoutY(0);
                }
                break;

            case SETTINGS:
                currentWindow = new Settings(config);
                break;
            case SHOP:
                currentWindow = new Shop(config.optInt("playerMoney", 0));
                break;
            case NOTE:
            case PICTURE:
            case COMPUTER:
            case VICTORY:
            case GAME_OVER:
                currentWindow = new InteractiveObjectWindow(this, type, config);
                break;
            default:
                currentWindow = null;
                break;
        }
        if (currentWindow != null) {
            currentWindow.show();
            System.out.println("Window shown: " + type);
        }
        return currentWindow;
    }

    public UIWindow getCurrentWindow() {
        return currentWindow;
    }

    public void hideCurrentWindow() {
        if (currentWindow != null) {
            currentWindow.hide();
            currentWindow = null;
            overlayPane.getChildren().clear(); // Очищаємо overlay від меню
            GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);
            if (GameWindow.getInstance().getPrimaryStage() != null) {
                GameWindow.getInstance().getPrimaryStage().requestFocus();
                System.out.println("Window closed, game state restored to PLAYING, focus requested");
            } else {
                System.err.println("Primary stage is null");
            }
        } else {
            System.out.println("No window to close");
        }
    }

    public void showPuzzleUI(Node uiNode) {
        if (overlayPane != null && uiNode != null) {
            overlayPane.getChildren().clear();
            overlayPane.getChildren().add(uiNode);
            // Центрування панелі
            uiNode.setLayoutX((canvas.getWidth() - ((Pane) uiNode).getPrefWidth()) / 2);
            uiNode.setLayoutY((canvas.getHeight() - ((Pane) uiNode).getPrefHeight()) / 2);
            GameManager.getInstance().setGameState(GameManager.GameState.PAUSED);

            System.out.println("Puzzle UI shown");
        }
    }

    public void hidePuzzleUI() {
        if (overlayPane != null) {
            overlayPane.getChildren().clear();
            GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);
            if (GameWindow.getInstance().getPrimaryStage() != null) {
                GameWindow.getInstance().getPrimaryStage().requestFocus();
            }
            System.out.println("Puzzle UI hidden");
        }
    }

    public void handleInput(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            System.out.println("Esc pressed, closing window or puzzle UI");
            if (currentWindow != null) {
                hideCurrentWindow();
            } else if (!overlayPane.getChildren().isEmpty()) {
                hidePuzzleUI();
            }
        }
    }

    public void showInteractionPrompt(String prompt) {
        if (overlayPane != null && interactionLabel != null && prompt != null && !prompt.isEmpty()) {
            if (currentWindow == null) {
                interactionLabel.setText(prompt);
                if (!overlayPane.getChildren().contains(interactionLabel)) {
                    overlayPane.getChildren().add(interactionLabel);
                }
                interactionLabel.setLayoutX((canvas.getWidth() - interactionLabel.prefWidth(-1)) / 2);
                interactionLabel.setLayoutY(canvas.getHeight() - 50);
                System.out.println("Showing interaction prompt: " + prompt);
            }
        } else {
            hideInteractionPrompt();
        }
    }

    public void hideInteractionPrompt() {
        if (overlayPane != null && interactionLabel != null) {
            overlayPane.getChildren().remove(interactionLabel);
        }
    }

    public Pane getOverlayPane() {
        return overlayPane;
    }

    @Override
    public void render(GraphicsContext gc) {}

    @Override
    public int getRenderLayer() {
        return 2;
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}