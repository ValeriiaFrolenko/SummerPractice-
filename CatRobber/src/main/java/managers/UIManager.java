package managers;

import entities.Player;
import interfaces.Renderable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONObject;
import main.GameWindow;
import ui.*;
import utils.GameLoader;

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
    private Pane menuButtonPane;
    private Button menuButton;
    private boolean isMenuButtonVisible = false;
    private Pane boostButtonPane;
    private Button boostButton;
    private boolean isBoostButtonVisible = false;
    private Pane boostPane;
    private boolean isBoostPaneVisible = false;
    private GameLoader gameLoader;

    public static UIManager getInstance() {
        if (instance == null) {
            instance = new UIManager(canvas);
        }
        return instance;
    }

    public Pane getOverlayPane() {
        return overlayPane;
    }

    public Pane getMenuButtonPane() {
        return menuButtonPane;
    }

    public Pane getBoostPane() {
        return boostPane;
    }

    public enum WindowType { MENU, SETTINGS, SHOP, NOTE, PICTURE, COMPUTER, VICTORY, GAME_OVER }

    public UIManager(Canvas canvas) {
        this.gameLoader = new GameLoader();
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

        // Обробник подій для menuPane
        menuPane.setOnKeyPressed(this::handleInput);
        menuPane.setOnMouseClicked(e -> {
            menuPane.requestFocus();
            e.consume();
        });

        // Налаштування menuButtonPane - тепер містить всі ігрові кнопки
        this.menuButtonPane = new Pane();
        this.menuButtonPane.setStyle("-fx-background-color: transparent;");
        this.menuButtonPane.setMouseTransparent(false);
        this.menuButtonPane.setFocusTraversable(false);
        this.menuButtonPane.setPickOnBounds(false);
        this.menuButtonPane.setVisible(false);

        // Видаляємо окремий boostButtonPane - не потрібен
        // this.boostButtonPane = new Pane();

        // Налаштування boostPane - тепер буде дочірнім елементом menuButtonPane
        this.boostPane = new Pane();
        this.boostPane.setStyle("-fx-background-color: rgba(101, 67, 33, 0.9); -fx-background-radius: 8px; -fx-border-color: rgba(139, 90, 43, 0.8); -fx-border-width: 2px; -fx-border-radius: 8px;");
        this.boostPane.setPrefSize(200, 80);
        this.boostPane.setLayoutX(20);
        this.boostPane.setLayoutY(100);
        this.boostPane.setVisible(false);
        this.boostPane.setMouseTransparent(true);

        // Створюємо кнопки та панель
        createMenuButton();
        createBoostButton();
        createBoostPane();

        // Додаємо boost панель до menuButtonPane
        this.menuButtonPane.getChildren().add(boostPane);
    }

    private void createMenuButton() {
        menuButton = new Button("☰");
        menuButton.setStyle(
                "-fx-background-color: rgba(101, 67, 33, 0.9); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 8px 12px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-border-color: rgba(139, 90, 43, 0.8); " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-cursor: hand;"
        );

        menuButton.setOnMouseEntered(e -> {
            menuButton.setStyle(
                    "-fx-background-color: rgba(139, 90, 43, 0.9); " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 20px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8px 12px; " +
                            "-fx-background-radius: 8px; " +
                            "-fx-border-color: rgba(160, 120, 80, 0.9); " +
                            "-fx-border-width: 2px; " +
                            "-fx-border-radius: 8px; " +
                            "-fx-cursor: hand;"
            );
        });

        menuButton.setOnMouseExited(e -> {
            menuButton.setStyle(
                    "-fx-background-color: rgba(101, 67, 33, 0.9); " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 20px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8px 12px; " +
                            "-fx-background-radius: 8px; " +
                            "-fx-border-color: rgba(139, 90, 43, 0.8); " +
                            "-fx-border-width: 2px; " +
                            "-fx-border-radius: 8px; " +
                            "-fx-cursor: hand;"
            );
        });

        menuButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.SoundType.BUTTON_CLICK);
            hideMenuButton();
            GameManager.getInstance().stopGameAndGoToMenu();
            e.consume();
        });

        menuButton.setLayoutX(canvas.getWidth() - 80);
        menuButton.setLayoutY(40);
        menuButton.setFocusTraversable(false);
        menuButtonPane.getChildren().add(menuButton);
    }

    private void createBoostButton() {
        boostButton = new Button("⚡");
        boostButton.setStyle(
                "-fx-background-color: rgba(101, 67, 33, 0.9); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 8px 12px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-border-color: rgba(139, 90, 43, 0.8); " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-cursor: hand;"
        );

        boostButton.setOnMouseEntered(e -> {
            boostButton.setStyle(
                    "-fx-background-color: rgba(139, 90, 43, 0.9); " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 20px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8px 12px; " +
                            "-fx-background-radius: 8px; " +
                            "-fx-border-color: rgba(160, 120, 80, 0.9); " +
                            "-fx-border-width: 2px; " +
                            "-fx-border-radius: 8px; " +
                            "-fx-cursor: hand;"
            );
        });

        boostButton.setOnMouseExited(e -> {
            boostButton.setStyle(
                    "-fx-background-color: rgba(101, 67, 33, 0.9); " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 20px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8px 12px; " +
                            "-fx-background-radius: 8px; " +
                            "-fx-border-color: rgba(139, 90, 43, 0.8); " +
                            "-fx-border-width: 2px; " +
                            "-fx-border-radius: 8px; " +
                            "-fx-cursor: hand;"
            );
        });

        boostButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.SoundType.BUTTON_CLICK);
            if (isBoostPaneVisible) {
                hideBoostPane();
            } else {
                showBoostPane();
            }
            e.consume();
        });

        boostButton.setLayoutX(canvas.getWidth() - 120);;
        boostButton.setLayoutY(40);
        boostButton.setFocusTraversable(false);
        // Додаємо boost кнопку до menuButtonPane замість окремогоPane
        menuButtonPane.getChildren().add(boostButton);
    }


    private void createBoostPane() {
        HBox boostButtons = new HBox(8);
        boostButtons.setLayoutX(8);
        boostButtons.setLayoutY(8);

        List<ShopItem> shopItems = ShopPane.getItems();
        Player player = GameManager.getInstance().getPlayer(); // Отримуємо гравця

        for (int i = 0; i < 4; i++) {
            VBox boostContainer = new VBox(3);
            final int index = i;
            ShopItem item = shopItems.get(i);

            Button boostButton = new Button();
            ImageView icon = new ImageView(gameLoader.loadImage(item.getSpritePath()));
            icon.setFitWidth(30);
            icon.setFitHeight(30);
            boostButton.setGraphic(icon);
            boostButton.setPrefSize(40, 40);
            boostButton.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-padding: 4px; " +
                            "-fx-background-radius: 5px; " +
                            "-fx-border-color: rgba(139, 90, 43, 0.8); " +
                            "-fx-border-width: 1px; " +
                            "-fx-border-radius: 5px; " +
                            "-fx-cursor: hand;"
            );

            boostButton.setOnMouseEntered(e -> {
                boostButton.setStyle(
                        "-fx-background-color: rgba(139, 90, 43, 0.3); " +
                                "-fx-padding: 4px; " +
                                "-fx-background-radius: 5px; " +
                                "-fx-border-color: rgba(160, 120, 80, 0.9); " +
                                "-fx-border-width: 1px; " + // Виправлено -px-border-width
                                "-fx-border-radius: 5px; " +
                                "-fx-cursor: hand;"
                );
            });

            boostButton.setOnMouseExited(e -> {
                boostButton.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-padding: 4px; " +
                                "-fx-background-radius: 5px; " +
                                "-fx-border-color: rgba(139, 90, 43, 0.8); " +
                                "-fx-border-width: 1px; " +
                                "-fx-border-radius: 5px; " +
                                "-fx-cursor: hand;"
                );
            });

            boostButton.setOnAction(e -> {
                // Тепер просто викликаємо activateBoost, логіка перевірки кількості буде там
                SoundManager.getInstance().playSound(SoundManager.SoundType.BUTTON_CLICK);
                activateBoost(index);
                e.consume();
            });

            boostButton.setFocusTraversable(false);

            // Отримуємо початкову кількість з інвентарю гравця
            int initialCount = (player != null) ? player.getInventory().getOrDefault(item, 0) : 0;
            Label countLabel = new Label(String.valueOf(initialCount)); // Встановлюємо реальну кількість
            countLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
            countLabel.setUserData(index);

            boostContainer.getChildren().addAll(boostButton, countLabel);
            boostButtons.getChildren().add(boostContainer);
        }

        boostPane.getChildren().clear();
        boostPane.getChildren().add(boostButtons);
    }




    private void activateBoost(int index) {
        Player player = GameManager.getInstance().getPlayer();
        if (player == null) return;

        List<ShopItem> shopItems = ShopPane.getItems();
        if (index >= shopItems.size()) return;

        ShopItem itemToUse = shopItems.get(index);

        // ЗМІНЮЄМО УМОВУ: ТЕПЕР КЛЮЧ ТАКОЖ ВИТРАЧАЄТЬСЯ ОДРАЗУ
        if (itemToUse.getItemType() == ShopItem.ItemType.SPEED_BOOST ||
                itemToUse.getItemType() == ShopItem.ItemType.INVISIBILITY ||
                itemToUse.getItemType() == ShopItem.ItemType.KEY) { // <--- ДОДАЄМО КЛЮЧ ДО УМОВИ

            if (!player.useItem(itemToUse)) {
                System.out.println("Не вдалося активувати: " + itemToUse.getName() + ", немає в наявності.");
                return;
            }
            GameManager.getInstance().updateInventoryFromPlayer(); // Оновлюємо інвентар

        }

        updateAllBoostCounts();
        System.out.println("Активовано покращення: " + itemToUse.getName());

        switch (itemToUse.getItemType()) {
            case INVISIBILITY:
                player.applyInvisibility(10.0);
                break;
            case SPEED_BOOST:
                player.applySpeedBoost(8.0);
                break;
            case KEY:
                player.giveUniversalKey(); // Гравець "бере ключ в руки"
                break;
            case GUN:
                // Для пістолета нічого не робимо, він витрачається при пострілі
                break;
        }
    }





    public void updateAllBoostCounts() {
        Player player = GameManager.getInstance().getPlayer();
        if (player == null || boostPane == null) {
            return;
        }

        List<ShopItem> shopItems = ShopPane.getItems();
        if (shopItems.size() < 4) return;

        for (int i = 0; i < 4; i++) {
            ShopItem item = shopItems.get(i);
            int quantity = player.getInventory().getOrDefault(item, 0);

            final int index = i;
            boostPane.getChildren().stream()
                    .filter(node -> node instanceof HBox)
                    .map(node -> (HBox) node)
                    .flatMap(hbox -> hbox.getChildren().stream())
                    .filter(node -> node instanceof VBox)
                    .map(node -> (VBox) node)
                    .filter(vbox -> {
                        if (vbox.getChildren().size() > 1 && vbox.getChildren().get(1) instanceof Label) {
                            Label label = (Label) vbox.getChildren().get(1);
                            return label.getUserData() != null && (Integer) label.getUserData() == index;
                        }
                        return false;
                    })
                    .findFirst()
                    .ifPresent(vbox -> {
                        Label label = (Label) vbox.getChildren().get(1);
                        label.setText(String.valueOf(quantity));
                    });
        }
    }

    public void showBoostButton() {
        if (GameManager.getInstance().getGameState() == GameManager.GameState.PLAYING) {
            isBoostButtonVisible = true;
            boostButtonPane.getChildren().clear();
            createBoostButton();
            boostButtonPane.setVisible(true);
            boostButtonPane.setMouseTransparent(false);
            javafx.application.Platform.runLater(() -> {
                boostButton.setLayoutX(20);
                boostButton.setLayoutY(40);
            });
        }
    }

    public void hideBoostButton() {
        isBoostButtonVisible = false;
        boostButtonPane.getChildren().clear();
        boostButtonPane.setVisible(false);
        boostButtonPane.setMouseTransparent(true);
        hideBoostPane();
    }

    public void showBoostPane() {
        if (GameManager.getInstance().getGameState() == GameManager.GameState.PLAYING) {
            isBoostPaneVisible = true;
            javafx.application.Platform.runLater(() -> {
                double boostButtonX = canvas.getWidth() - 120; // Позиція boost кнопки
                double panelX = boostButtonX - boostPane.getPrefWidth() - 10; // Вліво від кнопки з відступом
                double panelY = 40 + 50; // Під кнопкою

                boostPane.setLayoutX(panelX);
                boostPane.setLayoutY(panelY);
            });
            boostPane.setVisible(true);
            boostPane.setMouseTransparent(false);
            System.out.println("Showing boost pane, visible: " + boostPane.isVisible());
            javafx.application.Platform.runLater(() -> boostPane.requestFocus());
        }
    }

    public void hideBoostPane() {
        isBoostPaneVisible = false;
        boostPane.setVisible(false);
        boostPane.setMouseTransparent(true);
        System.out.println("Hiding boost pane, visible: " + boostPane.isVisible());
    }
    public Pane getBoostButtonPane() {
        return boostButtonPane;
    }

    public void showMenuButton() {
        if (GameManager.getInstance().getGameState() == GameManager.GameState.PLAYING) {
            System.out.println("Showing buttons: " + GameManager.getInstance().getGameState());
            isMenuButtonVisible = true;
            isBoostButtonVisible = true;

            // Очищаємо та створюємо заново всі кнопки
            menuButtonPane.getChildren().clear();

            createMenuButton();
            createBoostButton();

            // boostPane вже додана в конструкторі, просто додаємо її знову якщо потрібно
            if (!menuButtonPane.getChildren().contains(boostPane)) {
                menuButtonPane.getChildren().add(boostPane);
            }

            menuButtonPane.setVisible(true);
            menuButtonPane.setMouseTransparent(false);

            updateAllBoostCounts();

              javafx.application.Platform.runLater(() -> {
                menuButton.setLayoutX(canvas.getWidth() - 80);
                menuButton.setLayoutY(40);
                boostButton.setLayoutX(canvas.getWidth() - 120); // Поряд з menu кнопкою
                boostButton.setLayoutY(40);
            });
        }
    }

// Оновіть hideMenuButton():

    public void hideMenuButton() {
        isMenuButtonVisible = false;
        isBoostButtonVisible = false;
        menuButtonPane.getChildren().clear();
        menuButtonPane.setVisible(false);
        menuButtonPane.setMouseTransparent(true);
        hideBoostPane();
    }


    public UIWindow createWindow(WindowType type, JSONObject config) {
        hideMenuButton(); // Це також ховає boostButton
        GameWindow.getInstance().hideTitleBar();
        if (type == WindowType.MENU || type == WindowType.SHOP) {
            if (currentWindow != null) {
                currentWindow.hide();
                currentWindow = null;
            }
            overlayPane.getChildren().clear();
            overlayPane.setVisible(false);
            overlayPane.setMouseTransparent(true);
            isPuzzleUIShown = false;
            isInteractiveUIShown = false;

            switch (type) {
                case MENU:
                    showMenu();
                    return null;
                case SHOP:
                    menuPane.getChildren().clear();
                    currentWindow = new ShopPane();
                    menuPane.getChildren().add(currentWindow.getRoot());
                    currentWindow.show();
                    menuPane.setVisible(true);
                    menuPane.setMouseTransparent(false);
                    menuPane.setFocusTraversable(true);
                    GameManager.getInstance().setGameState(GameManager.GameState.PAUSED);
                    javafx.application.Platform.runLater(() -> {
                        menuPane.requestFocus();
                        canvas.setFocusTraversable(false);
                    });
                    return currentWindow;
            }
        } else {
            InteractiveObjectWindow interactiveWindow = new InteractiveObjectWindow(type, config);
            showInteractiveObjectUI(interactiveWindow.getUI());
            return null;
        }
        return null;
    }

    public void showMenu() {
        if (currentWindow != null) {
            currentWindow.hide();
            currentWindow = null;
        }
        overlayPane.getChildren().clear();
        overlayPane.setVisible(false);
        overlayPane.setMouseTransparent(true);
        isPuzzleUIShown = false;
        isInteractiveUIShown = false;
        hideMenuButton(); // Це також ховає boostButton
        GameWindow.getInstance().hideTitleBar();
        menuPane.getChildren().clear();
        menu = new Menu(new JSONObject());
        menuPane.getChildren().add(menu.getRoot());
        menu.showWithoutSplash();
        menuPane.setVisible(true);
        menuPane.setMouseTransparent(false);
        menuPane.setFocusTraversable(true);
        GameManager.getInstance().setGameState(GameManager.GameState.MENU);
        javafx.application.Platform.runLater(() -> {
            menuPane.requestFocus();
            canvas.setFocusTraversable(false);
        });
    }

    public void clearSceneForMenu() {
        if (overlayPane != null) {
            overlayPane.getChildren().clear();
            overlayPane.setVisible(false);
            overlayPane.setMouseTransparent(true);
            overlayPane.setFocusTraversable(false);
        }
        if (menuPane != null) {
            menuPane.getChildren().clear();
            menuPane.setVisible(false);
            menuPane.setMouseTransparent(true);
            menuPane.setFocusTraversable(false);
        }
        if (menuButtonPane != null) {
            menuButtonPane.getChildren().clear();
            menuButtonPane.setVisible(false);
            menuButtonPane.setMouseTransparent(true);
        }
        // Видаляємо очищення boostButtonPane
        currentWindow = null;
        menu = null;
        isPuzzleUIShown = false;
        isInteractiveUIShown = false;
        isMenuButtonVisible = false;
        isBoostButtonVisible = false;
        isBoostPaneVisible = false;
        interactionPrompts.clear();
        if (interactionLabel != null) {
            interactionLabel.setText("");
        }
    }

    // Решта методів (showInteractiveObjectUI, hideInteractiveObjectUI, showPuzzleUI, hidePuzzleUI, handleInput, showInteractionPrompt, hideInteractionPrompt, getOverlayPane, render, getRenderLayer, isVisible) залишаються без змін
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
            overlayPane.getChildren().clear();
            overlayPane.setVisible(false);
            overlayPane.setMouseTransparent(true);
            isInteractiveUIShown = false;
            GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);
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
        SoundManager.getInstance().stopSoundEffects();
        hideInteractionPrompt();
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
        if (GameManager.getInstance().getGameState() == GameManager.GameState.MENU) {
            if (menu != null && menuPane.isVisible()) {
                menu.handleInput(event);
                event.consume();
            }
            return;
        }
        if (isPuzzleUIShown || isInteractiveUIShown) {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (isPuzzleUIShown) {
                    hidePuzzleUI();
                } else if (isInteractiveUIShown) {
                    hideInteractiveObjectUI();
                }
                event.consume();
            }
            return;
        }
        if (currentWindow != null) {
            event.consume();
            return;
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
            if (!overlayPane.isVisible()) {
                overlayPane.setVisible(true);
            }
            if (!overlayPane.getChildren().contains(interactionLabel)) {
                overlayPane.getChildren().add(interactionLabel);
            }
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
            if (overlayPane.getChildren().isEmpty() && !isPuzzleUIShown && !isInteractiveUIShown) {
                overlayPane.setVisible(false);
            }
        }
    }

    public Pane getMenuPane() {
        return menuPane;
    }

    public void setCurrentWindow(UIWindow window) {
        this.currentWindow = window;
    }

    public UIWindow getCurrentWindow() {
        return currentWindow;
    }

    public void hideCurrentWindowToGame() {
        if (currentWindow != null) {
            currentWindow.hide();
            currentWindow = null;
        }
        if (menu != null) {
            menu.hide();
        }
        menuPane.getChildren().clear();
        menuPane.setVisible(false);
        menuPane.setMouseTransparent(true);
        menuPane.setFocusTraversable(false);
        overlayPane.getChildren().clear();
        overlayPane.setVisible(false);
        overlayPane.setMouseTransparent(true);
        overlayPane.setFocusTraversable(false);
        isPuzzleUIShown = false;
        isInteractiveUIShown = false;
        GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);
        showMenuButton();
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
            menuPane.setFocusTraversable(false);
            showMenuButton();
            GameWindow.getInstance().showTitleBar();
            if (canvas != null) {
                canvas.setFocusTraversable(true);
                javafx.application.Platform.runLater(() -> {
                    canvas.requestFocus();
                });
            }
            GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);
        }
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