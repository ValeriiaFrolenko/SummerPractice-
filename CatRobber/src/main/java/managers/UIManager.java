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
import managers.FontManager;
import managers.GameManager;
import puzzles.Puzzle;
import org.json.JSONObject;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import main.GameWindow;
import ui.InteractiveObjectWindow;
import ui.Menu;
import ui.ShopPane;
import ui.UIWindow;

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
    private boolean isPuzzleUIShown = false;
    private boolean isInteractiveUIShown = false;

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

        // Налаштування overlayPane (для головоломок та інтерактивних об'єктів)
        this.overlayPane.setStyle("-fx-background-color: transparent;");
        this.overlayPane.setMouseTransparent(false);
        this.overlayPane.setFocusTraversable(true);
        this.overlayPane.setPickOnBounds(false);

        // Налаштування menuPane (для меню та магазину)
        this.menuPane.setStyle("-fx-background-color: transparent;");
        this.menuPane.setMouseTransparent(false);
        this.menuPane.setFocusTraversable(true);
        this.menuPane.setPickOnBounds(false);

        // Налаштування interactionLabel
        this.interactionLabel = new Label();
        this.interactionPrompts = new ArrayList<>();

        // Встановлюємо обробник подій на menuPane
        menuPane.setOnKeyPressed(this::handleInput);

        menuPane.setOnMouseClicked(e -> {
            menuPane.requestFocus();
            e.consume();
        });
    }

    public UIWindow createWindow(WindowType type, JSONObject config) {

        // Тільки для меню та магазину - використовуємо стару логіку
        if (type == WindowType.MENU || type == WindowType.SHOP) {
            GameManager.getInstance().setGameState(GameManager.GameState.PAUSED);

            switch (type) {
                case MENU:
                    if (menu == null) {
                        menu = new Menu(config);
                    }
                    if (currentWindow != null) {
                        currentWindow.hide();
                        currentWindow = null;
                    }
                    menuPane.getChildren().clear();
                    menuPane.getChildren().add(menu.getRoot());
                    menu.show();
                    GameManager.getInstance().setGameState(GameManager.GameState.MENU);
                    menuPane.setMouseTransparent(false);
                    menuPane.setFocusTraversable(true);
                    menuPane.setVisible(true);
                    javafx.application.Platform.runLater(() -> {
                        menuPane.requestFocus();

                    });
                    return null;

                case SHOP:
                    if (currentWindow instanceof ShopPane) {
                        hideCurrentWindowToMenu();
                        return null;
                    }
                    if (currentWindow != null) {
                        currentWindow.hide();
                        currentWindow = null;
                    }
                    menuPane.getChildren().clear();
                    currentWindow = new ShopPane();
                    menuPane.getChildren().add(currentWindow.getRoot());
                    currentWindow.show();
                    menuPane.setMouseTransparent(false);
                    menuPane.setFocusTraversable(true);
                    menuPane.setVisible(true);
                    javafx.application.Platform.runLater(() -> {
                        menuPane.requestFocus();
                    });
                    break;
            }
            return currentWindow;
        }

        // Для інтерактивних об'єктів - використовуємо нову логіку через overlayPane
        else {
            InteractiveObjectWindow interactiveWindow = new InteractiveObjectWindow(type, config);
            showInteractiveObjectUI(interactiveWindow.getUI());
            return null; // Не зберігаємо як currentWindow
        }
    }

    public void setCurrentWindow(UIWindow window) {
        this.currentWindow = window;
    }

    public UIWindow getCurrentWindow() {
        return currentWindow;
    }

    public void hideCurrentWindowToGame() {

        // Ховаємо поточне вікно
        if (currentWindow != null) {
            currentWindow.hide();
            currentWindow = null;
        }

        // Ховаємо меню якщо воно є
        if (menu != null) {
            menu.hide();
        }

        // Повністю очищаємо menuPane
        menuPane.getChildren().clear();
        menuPane.setVisible(false);
        menuPane.setMouseTransparent(true);
        menuPane.setFocusTraversable(false);

        // Повністю очищаємо overlayPane
        overlayPane.getChildren().clear();
        overlayPane.setVisible(false);
        overlayPane.setMouseTransparent(true);
        overlayPane.setFocusTraversable(false);

        // Скидаємо стани UI
        isPuzzleUIShown = false;
        isInteractiveUIShown = false;

        // Встановлюємо стан гри
        GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);

        // Запитуємо фокус для основного вікна
        javafx.application.Platform.runLater(() -> {
            if (GameWindow.getInstance().getPrimaryStage() != null) {
                GameWindow.getInstance().getPrimaryStage().requestFocus();

            }
        });
    }

    public void hideCurrentWindowToMenu() {

        if (currentWindow != null) {
            currentWindow.hide();
            currentWindow = null;
        }

        menuPane.getChildren().clear();
        menuPane.setVisible(false);
        menuPane.setMouseTransparent(true);

        if (menu == null) {
            menu = new Menu(new JSONObject());
        }

        menuPane.getChildren().add(menu.getRoot());
        menu.showWithoutSplash();
        GameManager.getInstance().setGameState(GameManager.GameState.MENU);
        menuPane.setVisible(true);
        menuPane.setMouseTransparent(false);

        javafx.application.Platform.runLater(() -> {
            menuPane.requestFocus();
        });
    }

    public void hideMenu() {
        if (menu != null) {
            menu.hide();
            menuPane.getChildren().clear();
            menuPane.setVisible(false);
            menuPane.setMouseTransparent(true);
            currentWindow = null;
            GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);

            javafx.application.Platform.runLater(() -> {
                if (GameWindow.getInstance().getPrimaryStage() != null) {
                    GameWindow.getInstance().getPrimaryStage().requestFocus();
                }
            });
        }
    }

    public Pane getMenuPane() {
        return menuPane;
    }

    public void showInteractiveObjectUI(Node uiNode) {
            hideInteractionPrompt();
        if (overlayPane != null && uiNode != null) {
            isInteractiveUIShown = true;
            overlayPane.getChildren().clear();

            if (menuPane.isVisible()) {
                menuPane.setVisible(false);
                menuPane.setMouseTransparent(true);
            }

            overlayPane.getChildren().add(uiNode);
            isInteractiveUIShown = true;

            if (uiNode instanceof Pane) {
                Pane interactivePane = (Pane) uiNode;
                double centerX = (canvas.getWidth() - interactivePane.getPrefWidth()) / 2;
                double centerY = (canvas.getHeight() - interactivePane.getPrefHeight()) / 2;
                uiNode.setLayoutX(centerX);
                uiNode.setLayoutY(centerY);
            }

            overlayPane.setVisible(true);
            overlayPane.setMouseTransparent(false);
            overlayPane.setFocusTraversable(true);
            uiNode.setMouseTransparent(false);
            overlayPane.toFront();

            javafx.application.Platform.runLater(() -> {
                if (uiNode instanceof Pane) {
                    Pane interactivePane = (Pane) uiNode;
                    interactivePane.setFocusTraversable(true);
                    interactivePane.requestFocus();
                }
                overlayPane.requestFocus();
            });

            GameManager.getInstance().setGameState(GameManager.GameState.PAUSED);
        } else {
            System.err.println("Failed to show interactive object UI - overlayPane or uiNode is null");
        }
    }

    public void hideInteractiveObjectUI() {
        if (overlayPane != null && isInteractiveUIShown) {

            // Очищуємо дітей
            overlayPane.getChildren().clear();

            // Приховуємо панель
            overlayPane.setVisible(false);

            // Робимо прозорою для миші
            overlayPane.setMouseTransparent(true);

            // Змінюємо стан
            isInteractiveUIShown = false;

            // Змінюємо стан гри
            GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);

            // Повертаємо фокус
            if (GameWindow.getInstance().getPrimaryStage() != null) {
                javafx.application.Platform.runLater(() -> {
                    GameWindow.getInstance().getPrimaryStage().requestFocus();
                });
            } else {
                System.out.println("Primary stage is null, cannot request focus");
            }
        } else {
            System.out.println("Cannot hide UI - conditions not met:");
            if (overlayPane == null) {
                System.out.println("  - overlayPane is null");
            }
            if (!isInteractiveUIShown) {
                System.out.println("  - isInteractiveUIShown is false");
            }
        }
    }

    // Додатковий метод для форсованого закриття
    public void forceHideInteractiveObjectUI() {

        if (overlayPane != null) {
            overlayPane.getChildren().clear();
            overlayPane.setVisible(false);
            overlayPane.setMouseTransparent(true);
        }

        isInteractiveUIShown = false;
        GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);

        if (GameWindow.getInstance().getPrimaryStage() != null) {
            javafx.application.Platform.runLater(() -> {
                GameWindow.getInstance().getPrimaryStage().requestFocus();
            });
        }

    }
    public void showPuzzleUI(Node uiNode) {
        hideInteractionPrompt(); // Додайте цей рядок

        if (overlayPane != null && uiNode != null) {

            overlayPane.getChildren().clear();

            if (menuPane.isVisible()) {
                menuPane.setVisible(false);
                menuPane.setMouseTransparent(true);
            }

            overlayPane.getChildren().add(uiNode);
            isPuzzleUIShown = true;

            if (uiNode instanceof Pane) {
                Pane puzzlePane = (Pane) uiNode;
                double centerX = (canvas.getWidth() - puzzlePane.getPrefWidth()) / 2;
                double centerY = (canvas.getHeight() - puzzlePane.getPrefHeight()) / 2;
                uiNode.setLayoutX(centerX);
                uiNode.setLayoutY(centerY);

            }

            overlayPane.setVisible(true);
            overlayPane.setMouseTransparent(false);
            overlayPane.setFocusTraversable(true);
            uiNode.setMouseTransparent(false);
            overlayPane.toFront();

            javafx.application.Platform.runLater(() -> {
                if (uiNode instanceof Pane) {
                    Pane puzzlePane = (Pane) uiNode;
                    puzzlePane.setFocusTraversable(true);
                    puzzlePane.requestFocus();
                }
                overlayPane.requestFocus();
            });

            GameManager.getInstance().setGameState(GameManager.GameState.PAUSED);
        } else {
        }
    }

    public void hidePuzzleUI() {
        if (overlayPane != null && isPuzzleUIShown) {
            overlayPane.getChildren().clear();
            isPuzzleUIShown = false;

            if (menu != null && !menuPane.getChildren().isEmpty()) {
                menuPane.setVisible(true);
                menuPane.setMouseTransparent(false);
            }

            GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);

            if (GameWindow.getInstance().getPrimaryStage() != null) {
                javafx.application.Platform.runLater(() -> {
                    GameWindow.getInstance().getPrimaryStage().requestFocus();
                });
            }

        }
    }

    public void handleInput(KeyEvent event) {

        // Якщо показана головоломка або інтерактивний об'єкт і натиснуто ESC
        if ((isPuzzleUIShown || isInteractiveUIShown) && event.getCode() == KeyCode.ESCAPE) {
            if (isPuzzleUIShown) {
                hidePuzzleUI();
            } else if (isInteractiveUIShown) {
                hideInteractiveObjectUI();
            }
            event.consume();
            return;
        }

        if (currentWindow != null) {
            return;
        }

        if (!menuPane.getChildren().isEmpty() && menu != null && menuPane.isVisible()) {
            menu.handleInput(event);
            return;
        }

        if (event.getCode() == KeyCode.ESCAPE) {
            if (!menuPane.getChildren().isEmpty() && menuPane.isVisible()) {
                hideMenu();
            } else {
            }
            event.consume();
        }
    }

    public void showInteractionPrompt(String prompt) {
        if (interactionLabel != null && prompt != null && !prompt.isEmpty()) {
            interactionLabel.setText(prompt);

            interactionLabel.setFont(FontManager.getInstance().getFont("Hardpixel", 16));
            interactionLabel.setStyle(
                    "-fx-background-color: rgba(0, 0, 0, 0.8); " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 10px 20px; " +
                            "-fx-background-radius: 5px; " +
                            "-fx-border-color: #333333; " +
                            "-fx-border-width: 2px; " +
                            "-fx-border-radius: 5px;"
            );

            // Переконуємося що overlayPane налаштований правильно для показу підказки
            if (!overlayPane.isVisible()) {
                overlayPane.setVisible(true);
            }

            if (!overlayPane.getChildren().contains(interactionLabel)) {
                overlayPane.getChildren().add(interactionLabel);
            }

            // Переносимо interactionLabel на передній план
            interactionLabel.toFront();

            javafx.application.Platform.runLater(() -> {
                double labelWidth = interactionLabel.prefWidth(-1);
                double labelHeight = interactionLabel.prefHeight(-1);
                interactionLabel.setLayoutX((canvas.getWidth() - labelWidth) / 2);
                interactionLabel.setLayoutY(canvas.getHeight() - 50);
            });

        } else {
            hideInteractionPrompt();
        }
    }

    public void hideInteractionPrompt() {
        if (overlayPane != null && interactionLabel != null) {
            overlayPane.getChildren().remove(interactionLabel);

            // Якщо в overlayPane немає інших дітей (крім підказки),
            // ховаємо його повністю
            if (overlayPane.getChildren().isEmpty() && !isPuzzleUIShown && !isInteractiveUIShown) {
                overlayPane.setVisible(false);
            }
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