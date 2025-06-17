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
import javafx.animation.FadeTransition;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Клас для управління інтерфейсом користувача в грі. Реалізує патерн Singleton та інтерфейс Renderable.
 */
public class UIManager implements Renderable {
    /** Єдиний екземпляр класу UIManager (патерн Singleton). */
    private static UIManager instance;

    /** Canvas для рендерингу гри. */
    private static Canvas canvas;

    /** Поточне активне вікно інтерфейсу. */
    private UIWindow currentWindow;

    /** Список підказок для взаємодії з об’єктами. */
    private List<String> interactionPrompts;

    /** Панель для відображення елементів інтерфейсу поверх гри. */
    private Pane overlayPane;

    /** Мітка для відображення підказок взаємодії. */
    private Label interactionLabel;

    /** Панель для головного меню. */
    private Pane menuPane;

    /** Об’єкт головного меню. */
    private Menu menu;

    /** Прапорець, що вказує, чи відображається інтерфейс головоломки. */
    private boolean isPuzzleUIShown = false;

    /** Прапорець, що вказує, чи відображається інтерфейс інтерактивного об’єкта. */
    private boolean isInteractiveUIShown = false;

    /** Панель для кнопок меню та інших ігрових елементів. */
    private Pane menuButtonPane;

    /** Кнопка для виклику меню. */
    private Button menuButton;

    /** Прапорець, що вказує, чи видима кнопка меню. */
    private boolean isMenuButtonVisible = false;

    /** Панель для кнопки бусту. */
    private Pane boostButtonPane;

    /** Кнопка для активації бустів. */
    private Button boostButton;

    /** Прапорець, що вказує, чи видима кнопка бусту. */
    private boolean isBoostButtonVisible = false;

    /** Панель для відображення доступних бустів. */
    private Pane boostPane;

    /** Прапорець, що вказує, чи видима панель бустів. */
    private boolean isBoostPaneVisible = false;

    /** Завантажувач ресурсів гри. */
    private GameLoader gameLoader;

    /** Індикатор сирени для сповіщення про тривогу. */
    private ImageView sirenIndicator;

    /** Мітка для відображення кількості грошей гравця. */
    private Label moneyLabel;

    /**
     * Перелік типів вікон інтерфейсу.
     */
    public enum WindowType { MENU, SETTINGS, SHOP, NOTE, PICTURE, COMPUTER, VICTORY, GAME_OVER }

    /**
     * Повертає єдиний екземпляр класу UIManager.
     *
     * @return екземпляр UIManager
     */
    public static UIManager getInstance() {
        if (instance == null) {
            instance = new UIManager(canvas);
        }
        return instance;
    }

    /**
     * Повертає панель для накладання елементів інтерфейсу.
     *
     * @return панель overlayPane
     */
    public Pane getOverlayPane() {
        return overlayPane;
    }

    /**
     * Повертає панель для кнопок меню.
     *
     * @return панель menuButtonPane
     */
    public Pane getMenuButtonPane() {
        return menuButtonPane;
    }

    /**
     * Повертає панель для бустів.
     *
     * @return панель boostPane
     */
    public Pane getBoostPane() {
        return boostPane;
    }

    /**
     * Конструктор для ініціалізації інтерфейсу користувача.
     *
     * @param canvas canvas для рендерингу гри
     */
    public UIManager(Canvas canvas) {
        this.gameLoader = new GameLoader();
        this.canvas = canvas;
        this.overlayPane = new Pane();
        this.menuPane = new Pane();
        this.overlayPane.setStyle("-fx-background-color: transparent;");
        this.overlayPane.setMouseTransparent(false);
        this.overlayPane.setFocusTraversable(true);
        this.overlayPane.setPickOnBounds(false);
        this.menuPane.setStyle("-fx-background-color: transparent;");
        this.menuPane.setMouseTransparent(false);
        this.menuPane.setFocusTraversable(true);
        this.menuPane.setPickOnBounds(false);
        this.interactionLabel = new Label();
        this.interactionPrompts = new ArrayList<>();
        menuPane.setOnKeyPressed(this::handleInput);
        menuPane.setOnMouseClicked(e -> {
            menuPane.requestFocus();
            e.consume();
        });
        this.menuButtonPane = new Pane();
        this.menuButtonPane.setStyle("-fx-background-color: transparent;");
        this.menuButtonPane.setMouseTransparent(false);
        this.menuButtonPane.setFocusTraversable(false);
        this.menuButtonPane.setPickOnBounds(false);
        this.menuButtonPane.setVisible(false);
        this.boostPane = new Pane();
        this.boostPane.setStyle("-fx-background-color: rgba(101, 67, 33, 0.9); -fx-background-radius: 8px; -fx-border-color: rgba(139, 90, 43, 0.8); -fx-border-width: 2px; -fx-border-radius: 8px;");
        this.boostPane.setPrefSize(200, 80);
        this.boostPane.setLayoutX(20);
        this.boostPane.setLayoutY(100);
        this.boostPane.setVisible(false);
        this.boostPane.setMouseTransparent(true);
        createMenuButton();
        createBoostButton();
        createBoostPane();
        createMoneyPanel();
        this.menuButtonPane.getChildren().add(boostPane);
        setupSirenIndicator();
    }

    /**
     * Створює панель для відображення кількості грошей гравця.
     */
    private void createMoneyPanel() {
        moneyLabel = new Label("$0");
        moneyLabel.setStyle(
                "-fx-background-color: rgba(101, 67, 33, 0.9); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 8px 12px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-border-color: rgba(139, 90, 43, 0.8); " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 8px;"
        );
        moneyLabel.setLayoutX(canvas.getWidth() - 200);
        moneyLabel.setLayoutY(40);
        moneyLabel.setFocusTraversable(false);
        menuButtonPane.getChildren().add(moneyLabel);
    }

    /**
     * Налаштовує індикатор сирени для сповіщення про тривогу.
     */
    private void setupSirenIndicator() {
        Image sirenImage = gameLoader.loadImage("UI/Siren.png");
        if (sirenImage != null) {
            sirenIndicator = new ImageView(sirenImage);
            sirenIndicator.setFitWidth(120);
            sirenIndicator.setFitHeight(120);
            sirenIndicator.setVisible(false);
            if (!menuButtonPane.getChildren().contains(sirenIndicator)) {
                menuButtonPane.getChildren().add(sirenIndicator);
            }
        }
    }

    /**
     * Показує анімацію сирени для сповіщення про тривогу.
     */
    public void showSirenAlert() {
        if (sirenIndicator != null) {
            sirenIndicator.setLayoutX(canvas.getWidth() - 200);
            sirenIndicator.setLayoutY(40);
            sirenIndicator.setOpacity(1.0);
            sirenIndicator.setVisible(true);
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(2.0), sirenIndicator);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> sirenIndicator.setVisible(false));
            fadeOut.play();
        }
    }

    /**
     * Створює кнопку для виклику меню.
     */
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
            GameManager.getInstance().addMoney(GameManager.getInstance().getTemporaryMoney());
            GameManager.getInstance().stopGameAndGoToMenu();
            e.consume();
        });
        menuButton.setLayoutX(canvas.getWidth() - 80);
        menuButton.setLayoutY(40);
        menuButton.setFocusTraversable(false);
        menuButtonPane.getChildren().add(menuButton);
    }

    /**
     * Створює кнопку для активації бустів.
     */
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
        boostButton.setLayoutX(canvas.getWidth() - 120);
        boostButton.setLayoutY(40);
        boostButton.setFocusTraversable(false);
        menuButtonPane.getChildren().add(boostButton);
    }

    /**
     * Створює панель для відображення доступних бустів.
     */
    private void createBoostPane() {
        HBox boostButtons = new HBox(8);
        boostButtons.setLayoutX(8);
        boostButtons.setLayoutY(8);
        List<ShopItem> shopItems = ShopPane.getItems();
        Player player = GameManager.getInstance().getPlayer();
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
                                "-fx-border-width: 1px; " +
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
                SoundManager.getInstance().playSound(SoundManager.SoundType.BUTTON_CLICK);
                activateBoost(index);
                e.consume();
            });
            boostButton.setFocusTraversable(false);
            int initialCount = (player != null) ? player.getInventory().getOrDefault(item, 0) : 0;
            Label countLabel = new Label(String.valueOf(initialCount));
            countLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
            countLabel.setUserData(index);
            boostContainer.getChildren().addAll(boostButton, countLabel);
            boostButtons.getChildren().add(boostContainer);
        }
        boostPane.getChildren().clear();
        boostPane.getChildren().add(boostButtons);
    }

    /**
     * Активує буст за індексом.
     *
     * @param index індекс бусту в списку предметів магазину
     */
    private void activateBoost(int index) {
        Player player = GameManager.getInstance().getPlayer();
        if (player == null) return;
        List<ShopItem> shopItems = ShopPane.getItems();
        if (index >= shopItems.size()) return;
        ShopItem itemToUse = shopItems.get(index);
        if (itemToUse.getItemType() == ShopItem.ItemType.SPEED_BOOST ||
                itemToUse.getItemType() == ShopItem.ItemType.INVISIBILITY ||
                itemToUse.getItemType() == ShopItem.ItemType.KEY) {
            if (!player.useItem(itemToUse)) {
                return;
            }
        }
        updateAllBoostCounts();
        switch (itemToUse.getItemType()) {
            case INVISIBILITY:
                player.applyInvisibility(10.0);
                break;
            case SPEED_BOOST:
                player.applySpeedBoost(8.0);
                break;
            case KEY:
                player.giveUniversalKey();
                break;
            case GUN:
                break;
        }
    }

    /**
     * Оновлює відображення кількості всіх бустів у панелі.
     */
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

    /**
     * Показує кнопку бусту, якщо гра в стані PLAYING.
     */
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

    /**
     * Ховає кнопку бусту та панель бустів.
     */
    public void hideBoostButton() {
        isBoostButtonVisible = false;
        boostButtonPane.getChildren().clear();
        boostButtonPane.setVisible(false);
        boostButtonPane.setMouseTransparent(true);
        hideBoostPane();
    }

    /**
     * Показує панель бустів, якщо гра в стані PLAYING.
     */
    public void showBoostPane() {
        if (GameManager.getInstance().getGameState() == GameManager.GameState.PLAYING) {
            isBoostPaneVisible = true;
            javafx.application.Platform.runLater(() -> {
                double boostButtonX = canvas.getWidth() - 120;
                double panelX = boostButtonX - boostPane.getPrefWidth() - 10;
                double panelY = 40 + 50;
                boostPane.setLayoutX(panelX);
                boostPane.setLayoutY(panelY);
            });
            boostPane.setVisible(true);
            boostPane.setMouseTransparent(false);
            javafx.application.Platform.runLater(() -> boostPane.requestFocus());
        }
    }

    /**
     * Ховає панель бустів.
     */
    public void hideBoostPane() {
        isBoostPaneVisible = false;
        boostPane.setVisible(false);
        boostPane.setMouseTransparent(true);
    }

    /**
     * Повертає панель для кнопки бусту.
     *
     * @return панель boostButtonPane
     */
    public Pane getBoostButtonPane() {
        return boostButtonPane;
    }

    /**
     * Показує кнопки меню та бусту, якщо гра в стані PLAYING.
     */
    public void showMenuButton() {
        if (GameManager.getInstance().getGameState() == GameManager.GameState.PLAYING) {
            isMenuButtonVisible = true;
            isBoostButtonVisible = true;
            menuButtonPane.getChildren().clear();
            createMenuButton();
            createBoostButton();
            if (!menuButtonPane.getChildren().contains(moneyLabel)) {
                createMoneyPanel();
            }
            if (!menuButtonPane.getChildren().contains(boostPane)) {
                menuButtonPane.getChildren().add(boostPane);
            }
            if (sirenIndicator != null && !menuButtonPane.getChildren().contains(sirenIndicator)) {
                menuButtonPane.getChildren().add(sirenIndicator);
            }
            menuButtonPane.setVisible(true);
            menuButtonPane.setMouseTransparent(false);
            updateAllBoostCounts();
            updateMoneyDisplay();
            javafx.application.Platform.runLater(() -> {
                menuButton.setLayoutX(canvas.getWidth() - 80);
                menuButton.setLayoutY(40);
                boostButton.setLayoutX(canvas.getWidth() - 120);
                boostButton.setLayoutY(40);
                moneyLabel.setLayoutX(canvas.getWidth() - 200);
                moneyLabel.setLayoutY(40);
            });
        }
    }

    /**
     * Оновлює відображення кількості грошей гравця.
     */
    public void updateMoneyDisplay() {
        if (moneyLabel != null) {
            moneyLabel.setText("$" + (GameManager.getInstance().getTemporaryMoney() + GameManager.getInstance().getTotalMoney()));
        }
    }

    /**
     * Ховає кнопки меню та бусту, а також панель бустів.
     */
    public void hideMenuButton() {
        isMenuButtonVisible = false;
        isBoostButtonVisible = false;
        menuButtonPane.getChildren().clear();
        menuButtonPane.setVisible(false);
        menuButtonPane.setMouseTransparent(true);
        hideBoostPane();
    }

    /**
     * Створює та показує вікно інтерфейсу за типом.
     *
     * @param type тип вікна
     * @param config конфігурація для вікна
     * @return створене вікно або null
     */
    public UIWindow createWindow(WindowType type, JSONObject config) {
        hideMenuButton();
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

    /**
     * Показує головне меню гри.
     */
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
        hideMenuButton();
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

    /**
     * Очищає сцену для переходу до головного меню.
     */
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

    /**
     * Показує інтерфейс інтерактивного об’єкта.
     *
     * @param uiNode вузол інтерфейсу для відображення
     */
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

    /**
     * Ховає інтерфейс інтерактивного об’єкта.
     */
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
            }
        }
    }

    /**
     * Примусово ховає інтерфейс інтерактивного об’єкта.
     */
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

    /**
     * Показує інтерфейс головоломки.
     *
     * @param uiNode вузол інтерфейсу головоломки
     */
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

    /**
     * Ховає інтерфейс головоломки.
     */
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

    /**
     * Обробляє ввід користувача (клавіші).
     *
     * @param event подія натискання клавіші
     */
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

    /**
     * Показує підказку для взаємодії з об’єктом.
     *
     * @param prompt текст підказки
     */
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

    /**
     * Ховає підказку для взаємодії.
     */
    public void hideInteractionPrompt() {
        if (overlayPane != null && interactionLabel != null) {
            overlayPane.getChildren().remove(interactionLabel);
            if (overlayPane.getChildren().isEmpty() && !isPuzzleUIShown && !isInteractiveUIShown) {
                overlayPane.setVisible(false);
            }
        }
    }

    /**
     * Повертає панель головного меню.
     *
     * @return панель menuPane
     */
    public Pane getMenuPane() {
        return menuPane;
    }

    /**
     * Встановлює поточне вікно інтерфейсу.
     *
     * @param window нове вікно
     */
    public void setCurrentWindow(UIWindow window) {
        this.currentWindow = window;
    }

    /**
     * Повертає поточне вікно інтерфейсу.
     *
     * @return поточне вікно
     */
    public UIWindow getCurrentWindow() {
        return currentWindow;
    }

    /**
     * Ховає поточне вікно та повертає гру до стану PLAYING.
     */
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

    /**
     * Ховає поточне вікно та показує головне меню.
     */
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

    /**
     * Ховає головне меню та повертає гру до стану PLAYING.
     */
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

    /**
     * Рендерить елементи інтерфейсу.
     *
     * @param gc контекст для рендерингу
     */
    @Override
    public void render(GraphicsContext gc) {
        if (isMenuButtonVisible && moneyLabel != null) {
            updateMoneyDisplay();
        }
    }

    /**
     * Повертає шар рендерингу для інтерфейсу.
     *
     * @return шар рендерингу (2)
     */
    @Override
    public int getRenderLayer() {
        return 2;
    }

    /**
     * Повертає видимість інтерфейсу.
     *
     * @return true, інтерфейс завжди видимий
     */
    @Override
    public boolean isVisible() {
        return true;
    }
}