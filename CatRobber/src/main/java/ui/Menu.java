package ui;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.util.Duration;
import managers.FontManager;
import managers.GameManager;
import managers.SoundManager;
import managers.UIManager;
import org.json.JSONObject;
import utils.GameLoader;
import main.GameWindow;

public class Menu implements UIWindow {
    private VBox menuPane;
    private VBox splashPane;
    private VBox levelSelectPane;
    private StackPane rootPane;
    private boolean showingSplash = true;
    private boolean menuVisible = false;
    private boolean levelSelectionVisible = false;
    private ComboBox<String> locationChoice;
    private GameLoader gameLoader = new GameLoader();
    private UIManager uiManager;
    private final SoundManager soundManager = SoundManager.getInstance();

    public Menu(JSONObject defaultData) {
        uiManager = GameWindow.getInstance().getUIManager();
        createSplashScreen();
        createMainMenuUI();
        createLevelSelectUI();
        createRootPane();
        startSplashSequence();
    }

    private void createSplashScreen() {
        splashPane = new VBox();
        splashPane.setAlignment(Pos.CENTER);
        splashPane.setPrefSize(1280, 640);

        Stop[] stops = {
                new Stop(0, Color.web("#3C2F2F")),
                new Stop(0.3, Color.web("#2A2525")),
                new Stop(0.7, Color.web("#3C2F2F")),
                new Stop(1, Color.web("#1E1A1A"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, null, stops);
        splashPane.setBackground(new Background(new BackgroundFill(gradient, null, null)));

        Label gameTitle = new Label("КОТОГРАБІЖНИК");
        gameTitle.setFont(FontManager.getInstance().getFont("Hardpixel", 56));
        gameTitle.setTextFill(Color.web("#EAD9C2"));
        DropShadow titleShadow = new DropShadow();
        titleShadow.setColor(Color.web("#8B5A2B"));
        titleShadow.setOffsetX(3);
        titleShadow.setOffsetY(3);
        titleShadow.setRadius(8);
        gameTitle.setEffect(titleShadow);

        Label catPaw = new Label("🐾");
        catPaw.setFont(FontManager.getInstance().getFont("Hardpixel", 120));
        catPaw.setTextFill(Color.web("#EAD9C2"));
        DropShadow pawShadow = new DropShadow();
        pawShadow.setColor(Color.web("#8B5A2B"));
        pawShadow.setOffsetX(4);
        pawShadow.setOffsetY(4);
        pawShadow.setRadius(10);
        catPaw.setEffect(pawShadow);

        Label subtitle = new Label("СТЕЛС • ГРАБІЖ • ПРИГОДИ");
        subtitle.setFont(FontManager.getInstance().getFont("Hardpixel", 24));
        subtitle.setTextFill(Color.web("#D4A76A"));

        Label pressAnyKey = new Label(">>> НАТИСНІТЬ БУДЬ-ЯКУ КЛАВІШУ <<<");
        pressAnyKey.setFont(FontManager.getInstance().getFont("Hardpixel", 20));
        pressAnyKey.setTextFill(Color.web("#8B5A5A"));

        FadeTransition blinkTransition = new FadeTransition(Duration.seconds(1.2), pressAnyKey);
        blinkTransition.setFromValue(1.0);
        blinkTransition.setToValue(0.5);
        blinkTransition.setCycleCount(FadeTransition.INDEFINITE);
        blinkTransition.setAutoReverse(true);
        blinkTransition.play();

        VBox.setMargin(gameTitle, new Insets(0, 0, 30, 0));
        VBox.setMargin(catPaw, new Insets(0, 0, 30, 0));
        VBox.setMargin(subtitle, new Insets(0, 0, 60, 0));

        splashPane.getChildren().addAll(gameTitle, catPaw, subtitle, pressAnyKey);
        splashPane.setVisible(true);
    }

    private void createMainMenuUI() {
        menuPane = new VBox(30);
        menuPane.setAlignment(Pos.CENTER);
        menuPane.setPrefSize(1280, 640);
        menuPane.setVisible(false);

        Stop[] stops = {
                new Stop(0, Color.web("#3C2F2F")),
                new Stop(0.5, Color.web("#2A2525")),
                new Stop(1, Color.web("#1E1A1A"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, null, stops);
        menuPane.setBackground(new Background(new BackgroundFill(gradient, null, null)));

        Label title = new Label("КОТОГРАБІЖНИК");
        title.setFont(FontManager.getInstance().getFont("Hardpixel", 64));
        title.setTextFill(Color.web("#EAD9C2"));
        DropShadow titleShadow = new DropShadow();
        titleShadow.setColor(Color.web("#8B5A2B"));
        titleShadow.setOffsetX(3);
        titleShadow.setOffsetY(3);
        titleShadow.setRadius(8);
        title.setEffect(titleShadow);

        Label subtitle = new Label("🐾 Ласкаво просимо до штабу котячих злодіїв! 🐾");
        subtitle.setFont(FontManager.getInstance().getFont("Hardpixel", 24));
        subtitle.setTextFill(Color.web("#D4A76A"));

        Button continueButton = createCuteButton("ПРОДОВЖИТИ", Color.web("#4A7043"));
        Button selectLevelButton = createCuteButton("ОБРАТИ ЖЕРТВУ", Color.web("#5A5A5A"));
        Button shopButton = createCuteButton("КРАМНИЦЯ", Color.web("#7B3F3F"));
        Button exitButton = createCuteButton("ЗАЛЯГТИ НА ДНО", Color.web("#3C3C3C"));

        continueButton.setOnAction(e -> {
            soundManager.playSound(SoundManager.SoundType.BUTTON_CLICK);
            continueGame();
        });
        selectLevelButton.setOnAction(e -> {
            soundManager.playSound(SoundManager.SoundType.BUTTON_CLICK);
            showLevelSelect();
        });
        shopButton.setOnAction(e -> {
            soundManager.playSound(SoundManager.SoundType.BUTTON_CLICK);
            openShop();
        });
        exitButton.setOnAction(e -> {
            soundManager.playSound(SoundManager.SoundType.BUTTON_CLICK);
            System.exit(0);
        });

        VBox.setMargin(title, new Insets(0, 0, 20, 0));
        VBox.setMargin(subtitle, new Insets(0, 0, 40, 0));

        menuPane.getChildren().addAll(title, subtitle, continueButton, selectLevelButton, shopButton, exitButton);
    }

    private void createLevelSelectUI() {
        levelSelectPane = new VBox(30);
        levelSelectPane.setAlignment(Pos.CENTER);
        levelSelectPane.setPrefSize(1280, 640);
        levelSelectPane.setVisible(false);

        Stop[] stops = {
                new Stop(0, Color.web("#3C2F2F")),
                new Stop(0.5, Color.web("#2A2525")),
                new Stop(1, Color.web("#1E1A1A"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, null, stops);
        levelSelectPane.setBackground(new Background(new BackgroundFill(gradient, null, null)));

        Label levelTitle = new Label("ВИБІР ЦІЛІ");
        levelTitle.setFont(FontManager.getInstance().getFont("Hardpixel", 42));
        levelTitle.setTextFill(Color.web("#EAD9C2"));
        DropShadow levelTitleShadow = new DropShadow();
        levelTitleShadow.setColor(Color.web("#8B5A2B"));
        levelTitleShadow.setOffsetX(2);
        levelTitleShadow.setOffsetY(2);
        levelTitleShadow.setRadius(6);
        levelTitle.setEffect(levelTitleShadow);

        Label subtitle = new Label("🎯 Обери об'єкт для котячого рейду 🎯");
        subtitle.setFont(FontManager.getInstance().getFont("Hardpixel", 20));
        subtitle.setTextFill(Color.web("#D4A76A"));

        locationChoice = new ComboBox<>();
        JSONObject progress = gameLoader.loadJSON("data/saves/game_progress.json");
        locationChoice.getItems().add("🏠 БУДИНОК — Легко: Почни свою грабіжницьку кар’єру з дрібної крадіжки. Шкарпетки не рахуються");
        locationChoice.getItems().add("🏛️ МУЗЕЙ — Середньо: Увірвися до галереї вночі, щоб таємно повернути кошачий діамант — і зберегти честь гільдії");
        locationChoice.getItems().add("🏦 БАНК — Важко: Проникни в опечатане сховище, де корпорація «Віскас» зберігає стратегічні запаси корму");
        locationChoice.setValue(locationChoice.getItems().get(0));
        locationChoice.setStyle(
                "-fx-background-color: #2F2F2F;" +
                        "-fx-border-color: #D4A76A;" +
                        "-fx-border-width: 3px;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-font-family: 'Hardpixel';" +
                        "-fx-font-size: 18px;" +
                        "-fx-text-fill: #EAD9C2;" +
                        "-fx-pref-width: 550px;" +
                        "-fx-pref-height: 50px;"
        );

        locationChoice.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
                if (!empty) {
                    setStyle(
                            "-fx-background-color: #2F2F2F;" +
                                    "-fx-text-fill: #EAD9C2;" +
                                    "-fx-font-family: 'Hardpixel';" +
                                    "-fx-font-size: 18px;"
                    );
                }
            }
        });

        locationChoice.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
                if (!empty) {
                    setStyle(
                            "-fx-background-color: #2F2F2F;" +
                                    "-fx-text-fill: #EAD9C2;" +
                                    "-fx-font-family: 'Hardpixel';" +
                                    "-fx-font-size: 18px;"
                    );
                }
            }
        });

        Button confirmButton = createCuteButton("ПІДТВЕРДИТИ", Color.web("#4A7043"));
        confirmButton.setOnAction(e -> {
            soundManager.playSound(SoundManager.SoundType.BUTTON_CLICK);
            int selectedLevel = getSelectedLevel();
            startLevel(selectedLevel);
        });

        Button backButton = createCuteButton("ПОВЕРНУТИСЯ", Color.web("#7B3F3F"));
        backButton.setOnAction(e -> {
            soundManager.playSound(SoundManager.SoundType.BUTTON_CLICK);
            showMainMenu();
        });

        VBox.setMargin(levelTitle, new Insets(0, 0, 20, 0));
        VBox.setMargin(subtitle, new Insets(0, 0, 30, 0));
        VBox.setMargin(locationChoice, new Insets(0, 0, 20, 0));
        VBox.setMargin(confirmButton, new Insets(0, 0, 20, 0));

        levelSelectPane.getChildren().addAll(levelTitle, subtitle, locationChoice, confirmButton, backButton);
    }

    private void createRootPane() {
        rootPane = new StackPane();
        rootPane.setPrefSize(1280, 640);
        Stop[] stops = {
                new Stop(0, Color.web("#3C2F2F")),
                new Stop(1, Color.web("#1E1A1A"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, null, stops);
        rootPane.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        rootPane.getChildren().addAll(splashPane, menuPane, levelSelectPane);
    }

    private void startSplashSequence() {
        soundManager.playMusic("menu.mp3");
        Label catPaw = (Label) splashPane.getChildren().get(1);
        ScaleTransition logoScale = new ScaleTransition(Duration.seconds(1.2), catPaw);
        logoScale.setFromX(0.5);
        logoScale.setFromY(0.5);
        logoScale.setToX(1.0);
        logoScale.setToY(1.0);
        logoScale.setDelay(Duration.seconds(0.3));
        logoScale.play();
    }

    private void transitionToMenu() {
        if (!showingSplash) return;
        showingSplash = false;

        FadeTransition splashFadeOut = new FadeTransition(Duration.seconds(0.6), splashPane);
        splashFadeOut.setFromValue(1.0);
        splashFadeOut.setToValue(0.0);

        menuPane.setVisible(true);
        FadeTransition menuFadeIn = new FadeTransition(Duration.seconds(0.6), menuPane);
        menuFadeIn.setFromValue(0.0);
        menuFadeIn.setToValue(1.0);

        SequentialTransition transition = new SequentialTransition(splashFadeOut, menuFadeIn);
        transition.setOnFinished(e -> {
            splashPane.setVisible(false);
            menuVisible = true;
            javafx.application.Platform.runLater(() -> {
                rootPane.requestFocus();
            });
        });
        transition.play();
    }

    private void showLevelSelect() {
        menuPane.setVisible(false);
        menuVisible = false;
        levelSelectPane.setVisible(true);
        levelSelectionVisible = true;

        FadeTransition levelFadeIn = new FadeTransition(Duration.seconds(0.4), levelSelectPane);
        levelFadeIn.setFromValue(0.0);
        levelFadeIn.setToValue(1.0);
        levelFadeIn.setOnFinished(e -> {
            javafx.application.Platform.runLater(() -> {
                rootPane.requestFocus();
            });
        });
        levelFadeIn.play();
    }

    private void showMainMenu() {
        levelSelectPane.setVisible(false);
        levelSelectionVisible = false;
        menuPane.setVisible(true);
        menuVisible = true;

        FadeTransition menuFadeIn = new FadeTransition(Duration.seconds(0.4), menuPane);
        menuFadeIn.setFromValue(0.0);
        menuFadeIn.setToValue(1.0);
        menuFadeIn.setOnFinished(e -> {
            javafx.application.Platform.runLater(() -> {
                rootPane.requestFocus();
            });
        });
        menuFadeIn.play();
    }
    private Button createCuteButton(String text, Color color) {
        Button button = new Button(text);
        button.setFont(FontManager.getInstance().getFont("Hardpixel", 22));
        button.setPrefSize(420, 60);

        String baseStyle = String.format(
                "-fx-background-color: #2A2525;" +
                        "-fx-text-fill: %s;" +
                        "-fx-border-color: %s;" +
                        "-fx-border-width: 3px;" +
                        "-fx-border-radius: 15px;" +
                        "-fx-background-radius: 15px;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-family: 'Hardpixel';" +
                        "-fx-font-size: 22px;",
                toHexString(Color.web("#EAD9C2")), toHexString(color)
        );

        String hoverTextColor = color.equals(Color.web("#4A7043")) ? "#1E1A1A" : "#EAD9C2";
        String hoverStyle = String.format(
                "-fx-background-color: %s;" +
                        "-fx-text-fill: %s;" +
                        "-fx-border-color: #D4A76A;" +
                        "-fx-border-width: 4px;" +
                        "-fx-border-radius: 15px;" +
                        "-fx-background-radius: 15px;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-family: 'Hardpixel';" +
                        "-fx-font-size: 22px;",
                toHexString(color), hoverTextColor
        );

        button.setStyle(baseStyle);
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setColor(Color.web("#8B5A2B"));
        buttonShadow.setOffsetX(2);
        buttonShadow.setOffsetY(2);
        buttonShadow.setRadius(5);
        button.setEffect(buttonShadow);

        button.setOnMouseEntered(e -> {
            button.setStyle(hoverStyle);
            DropShadow hoverShadow = new DropShadow();
            hoverShadow.setColor(Color.web("#D4A76A"));
            hoverShadow.setOffsetX(3);
            hoverShadow.setOffsetY(3);
            hoverShadow.setRadius(8);
            button.setEffect(hoverShadow);
        });

        button.setOnMouseExited(e -> {
            button.setStyle(baseStyle);
            button.setEffect(buttonShadow);
        });

        return button;
    }

    private void continueGame() {
        soundManager.playMusic("game.mp3");
        GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);
        JSONObject progress = gameLoader.loadJSON("data/saves/game_progress.json");
        int currentLevel = progress != null ? progress.optInt("currentLevelId", 1) : 1;

        // Спочатку ховаємо поточне меню
        hide();

        // Очищаємо UI Manager
        UIManager.getInstance().hideMenu();
        if (UIManager.getInstance().getCurrentWindow() != null) {
            UIManager.getInstance().getCurrentWindow().hide();
            UIManager.getInstance().setCurrentWindow(null);
        }

        // Повністю очищаємо всі UI панелі
        UIManager.getInstance().hideCurrentWindowToGame();

        // Завантажуємо рівень
        GameManager.getInstance().loadLevel(currentLevel, false);
    }

    private void startLevel(int levelId) {
        soundManager.playMusic("game.mp3");
        GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);
        // Спочатку ховаємо поточне меню
        hide();

        // Очищаємо UI Manager
        UIManager.getInstance().hideMenu();
        if (UIManager.getInstance().getCurrentWindow() != null) {
            UIManager.getInstance().getCurrentWindow().hide();
            UIManager.getInstance().setCurrentWindow(null);
        }

        // Повністю очищаємо всі UI панелі
        UIManager.getInstance().hideCurrentWindowToGame();

        // Завантажуємо рівень
        GameManager.getInstance().loadLevel(levelId, true);

    }


    private void openShop() {
        GameManager.getInstance().loadProgress();
        uiManager.createWindow(UIManager.WindowType.SHOP, new JSONObject());
        hide();
    }

    private int getSelectedLevel() {
        String selected = locationChoice.getValue();
        if (selected.contains("БУДИНОК")) return 1;
        if (selected.contains("МУЗЕЙ")) return 2;
        if (selected.contains("БАНК")) return 3;
        return 1;
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    // In Menu.java
    public void showWithoutSplash() {
        showingSplash = false;
        splashPane.setVisible(false);
        menuPane.setVisible(true);
        menuVisible = true;
        levelSelectPane.setVisible(false);
        levelSelectionVisible = false;

        if (!uiManager.getMenuPane().getChildren().contains(rootPane)) {
            uiManager.getMenuPane().getChildren().add(rootPane);
        }
        rootPane.setVisible(true);
        rootPane.setFocusTraversable(true);

        rootPane.setOnKeyPressed(this::handleInput);

        javafx.application.Platform.runLater(() -> {
            rootPane.requestFocus();
        });
    }

    @Override
    public void show() {
        // Додаємо до UIManager якщо ще не додано
        if (!uiManager.getMenuPane().getChildren().contains(rootPane)) {
            uiManager.getMenuPane().getChildren().add(rootPane);
        }

        // Відновлюємо всі властивості
        rootPane.setVisible(true);
        rootPane.setMouseTransparent(false); // ❗ ВАЖЛИВО!
        rootPane.setFocusTraversable(true);

        // Відновлюємо обробник подій
        rootPane.setOnKeyPressed(this::handleInput);

        // Налаштовуємо стан панелей
        if (showingSplash) {
            splashPane.setVisible(true);
            menuPane.setVisible(false);
            levelSelectPane.setVisible(false);
        } else {
            splashPane.setVisible(false);
            menuPane.setVisible(true);
            menuVisible = true;
            levelSelectPane.setVisible(false);
            levelSelectionVisible = false;
        }

        // Запитуємо фокус
        javafx.application.Platform.runLater(() -> {
            rootPane.requestFocus();
        });
    }

    @Override
    public void hide() {
        // Ховаємо rootPane
        rootPane.setVisible(false);
        rootPane.setMouseTransparent(true);

        // Очищуємо обробники подій
        rootPane.setOnKeyPressed(null);

        // Скидаємо стани
        showingSplash = false;
        menuVisible = false;
        levelSelectionVisible = false;

        // НЕ очищуємо children - це може пошкодити UI!
    }

    // ДОДАТКОВИЙ МЕТОД ДЛЯ ПОВЕРНЕННЯ З МАГАЗИНУ
    public void returnFromShop() {
        System.out.println("Returning from shop to menu");

        // Повністю скидаємо стан
        showingSplash = false;
        menuVisible = true;
        levelSelectionVisible = false;

        // Показуємо правильні панелі
        splashPane.setVisible(false);
        menuPane.setVisible(true);
        levelSelectPane.setVisible(false);

        // Показуємо меню
        show();
    }


    @Override
    public Node getRoot() {
        return rootPane;
    }
    public void handleInput(KeyEvent event) {

        if (showingSplash) {
            transitionToMenu();
            event.consume();
        } else if (event.getCode() == KeyCode.ESCAPE) {
            if (levelSelectionVisible) {
                showMainMenu();
            } else if (menuVisible) {
                uiManager.hideMenu();
            }
            event.consume();
        }
    }

}