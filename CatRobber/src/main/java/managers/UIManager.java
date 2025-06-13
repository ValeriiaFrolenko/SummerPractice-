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
import puzzles.Puzzle;
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
    private static UIManager instance;
    private static Canvas canvas;
    private UIWindow currentWindow;
    private List<String> interactionPrompts;
    private Pane overlayPane;
    private Label interactionLabel;
    private Pane menuPane;
    private Menu menu;

    public static UIManager getInstance() {
        if (instance == null) {
            instance = new UIManager(canvas);
        }
        return instance;
    }

    public enum WindowType { MENU, SETTINGS, SHOP, NOTE, PICTURE, COMPUTER, VICTORY, GAME_OVER }

    public UIManager(Canvas canvas) {
        this.canvas = canvas;
        this.overlayPane = new Pane();
        this.menuPane = new Pane();

        // Налаштування overlayPane
        this.overlayPane.setStyle("-fx-background-color: transparent;");
        this.overlayPane.setMouseTransparent(false);
        this.overlayPane.setFocusTraversable(true);
        this.overlayPane.setPickOnBounds(false);

        // Налаштування menuPane
        this.menuPane.setStyle("-fx-background-color: transparent;");
        this.menuPane.setMouseTransparent(false);
        this.menuPane.setFocusTraversable(true);
        this.menuPane.setPickOnBounds(false);

        // Налаштування interactionLabel
        this.interactionLabel = new Label();
        this.interactionPrompts = new ArrayList<>();

        // Встановлюємо обробник подій лише на menuPane
        menuPane.setOnKeyPressed(this::handleInput);

        menuPane.setOnMouseClicked(e -> {
            System.out.println("menuPane clicked at: " + e.getX() + ", " + e.getY());
            menuPane.requestFocus();
            e.consume();
        });
    }

    public UIWindow createWindow(WindowType type, JSONObject config) {
        if (currentWindow != null) {
            System.out.println("Window creation skipped: existing window = " +
                    currentWindow.getClass().getSimpleName() + ", type = " + type);
            return currentWindow;
        }

        System.out.println("Creating window: " + type);
        GameManager.getInstance().setGameState(GameManager.GameState.PAUSED);

        switch (type) {
            case MENU:
                if (menu == null) {
                    menu = new Menu(config);
                }
                menuPane.getChildren().clear();
                menuPane.getChildren().add(menu.getRoot());
                menu.show();
                GameManager.getInstance().setGameState(GameManager.GameState.MENU);

                // Налаштування фокусу для menuPane
                menuPane.setMouseTransparent(false);
                menuPane.setFocusTraversable(true);
                javafx.application.Platform.runLater(() -> {
                    menuPane.requestFocus();
                    System.out.println("MenuPane focus requested in createWindow, has focus: " +
                            menuPane.isFocused());
                    // Додаємо слухач фокусу для дебагу
                    menuPane.focusedProperty().addListener((obs, oldVal, newVal) ->
                            System.out.println("menuPane focus changed: " + newVal));
                });

                System.out.println("Menu shown, menuPane children: " + menuPane.getChildren().size());
                return null; // Меню не є currentWindow

            case SETTINGS:
                currentWindow = new Settings(config);
                break;
            case SHOP:
                currentWindow = new Shop(GameManager.getInstance().getPlayer());
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
            overlayPane.setMouseTransparent(false);
            overlayPane.requestFocus();
            System.out.println("Window shown: " + type + ", overlayPane children: " +
                    overlayPane.getChildren().size());
        }
        return currentWindow;
    }

    public UIWindow getCurrentWindow() {
        return currentWindow;
    }

    public void hideCurrentWindow() {
        if (currentWindow != null) {
            currentWindow.hide();
            overlayPane.getChildren().remove(currentWindow.getRoot());
            currentWindow = null;
            GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);

            // Повертаємо фокус на основне вікно
            if (GameWindow.getInstance().getPrimaryStage() != null) {
                javafx.application.Platform.runLater(() -> {
                    GameWindow.getInstance().getPrimaryStage().requestFocus();
                });
            }

            System.out.println("Current window hidden, overlayPane children: " + overlayPane.getChildren().size());
        }
    }

    public void hideMenu() {
        if (menu != null) {
            menu.hide();
            menuPane.getChildren().clear();
            GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);

            // Повертаємо фокус на основне вікно
            if (GameWindow.getInstance().getPrimaryStage() != null) {
                javafx.application.Platform.runLater(() -> {
                    GameWindow.getInstance().getPrimaryStage().requestFocus();
                    System.out.println("Primary stage focus requested after hiding menu");
                });
            }

            System.out.println("Menu hidden, menuPane children: " + menuPane.getChildren().size());
        }
    }
    public Pane getMenuPane() {
        return menuPane;
    }

    public void showPuzzleUI(Node uiNode) {
        if (overlayPane != null && uiNode != null) {
            overlayPane.getChildren().clear();
            overlayPane.getChildren().add(uiNode);

            // Центрування UI елемента
            if (uiNode instanceof Pane) {
                Pane puzzlePane = (Pane) uiNode;
                double centerX = (canvas.getWidth() - puzzlePane.getPrefWidth()) / 2;
                double centerY = (canvas.getHeight() - puzzlePane.getPrefHeight()) / 2;
                uiNode.setLayoutX(centerX);
                uiNode.setLayoutY(centerY);
            }

            // Налаштування для puzzle UI
            overlayPane.setMouseTransparent(false);
            uiNode.setMouseTransparent(false);

            if (uiNode instanceof Pane) {
                ((Pane) uiNode).setFocusTraversable(true);
                javafx.application.Platform.runLater(() -> {
                    ((Pane) uiNode).requestFocus();
                });
            }

            GameManager.getInstance().setGameState(GameManager.GameState.PAUSED);
            System.out.println("Puzzle UI shown, centered at: " + uiNode.getLayoutX() + ", " + uiNode.getLayoutY());
        }
    }

    public void hidePuzzleUI() {
        if (overlayPane != null) {
            overlayPane.getChildren().clear();
            GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);

            // Повертаємо фокус на основне вікно
            if (GameWindow.getInstance().getPrimaryStage() != null) {
                javafx.application.Platform.runLater(() -> {
                    GameWindow.getInstance().getPrimaryStage().requestFocus();
                });
            }

            System.out.println("Puzzle UI hidden");
        }
    }

    public void handleInput(KeyEvent event) {
        System.out.println("UIManager handleInput: " + event.getCode() +
                ", currentWindow: " +
                (currentWindow != null ? currentWindow.getClass().getSimpleName() : "null") +
                ", menuChildren: " + menuPane.getChildren().size());

        // Передаємо подію до Menu, якщо воно активне
        if (!menuPane.getChildren().isEmpty() && menu != null) {
            System.out.println("Forwarding key event to Menu");
            menu.handleInput(event);
            return; // Виходимо, щоб уникнути подальшої обробки
        }

        // Обробка ESC для інших вікон або UI
        if (event.getCode() == KeyCode.ESCAPE) {
            if (currentWindow != null) {
                System.out.println("Closing current window");
                hideCurrentWindow();
            } else if (!overlayPane.getChildren().isEmpty()) {
                System.out.println("Hiding puzzle UI");
                hidePuzzleUI();
            } else if (!menuPane.getChildren().isEmpty()) {
                System.out.println("Hiding menu");
                hideMenu();
            } else {
                System.out.println("No UI to close");
            }
            event.consume();
        }
    }

    public void showInteractionPrompt(String prompt) {
        if (overlayPane != null && interactionLabel != null && prompt != null && !prompt.isEmpty()) {
            interactionLabel.setText(prompt);
            if (!overlayPane.getChildren().contains(interactionLabel)) {
                overlayPane.getChildren().add(interactionLabel);
            }

            // Позиціонування внизу екрану
            javafx.application.Platform.runLater(() -> {
                double labelWidth = interactionLabel.prefWidth(-1);
                interactionLabel.setLayoutX((canvas.getWidth() - labelWidth) / 2);
                interactionLabel.setLayoutY(canvas.getHeight() - 50);
            });

            System.out.println("Interaction prompt shown: " + prompt);
        } else {
            hideInteractionPrompt();
        }
    }

    public void hideInteractionPrompt() {
        if (overlayPane != null && interactionLabel != null) {
            overlayPane.getChildren().remove(interactionLabel);
            System.out.println("Interaction prompt hidden");
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