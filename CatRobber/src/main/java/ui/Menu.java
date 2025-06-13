package ui;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import managers.FontManager;
import managers.GameManager;
import managers.SaveManager;
import managers.UIManager;
import org.json.JSONObject;

public class Menu implements UIWindow {
    // Основні поля
    private VBox menuPane;
    private VBox splashPane;
    private StackPane rootPane;
    private Button startHeistButton;
    private Button selectLocationButton;
    private Button exitButton;
    private Button confirmLevelButton;
    private ComboBox<String> locationChoice;
    private boolean showingSplash = true;
    private boolean menuVisible = false;
    private boolean levelSelectionVisible = false;
    private int selectedLevel = 1;

    // Анімації та ефекти
    private FadeTransition splashFade;
    private ScaleTransition logoScale;

    public Menu(JSONObject defaultData) {
        createSplashScreen();
        createMenuUI();
        createRootPane();
        startSplashSequence();
    }

    private void createSplashScreen() {
        splashPane = new VBox();
        splashPane.setAlignment(Pos.CENTER);
        splashPane.setPrefSize(1280, 640);

        // Зістарений теплий фон (імітація старого паперу)
        Stop[] stops = {
                new Stop(0, Color.web("#3C2F2F")), // Темний коричневий
                new Stop(0.3, Color.web("#2A2525")), // Приглушений коричневий
                new Stop(0.7, Color.web("#3C2F2F")), // Зістарений відтінок
                new Stop(1, Color.web("#1E1A1A"))  // Темний знизу
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, null, stops);
        Background splashBg = new Background(new BackgroundFill(gradient, null, null));
        splashPane.setBackground(splashBg);

        // Заголовок гри в зістареному стилі
        Label gameTitle = new Label("КОТОГРАБІЖНИК");
        gameTitle.setFont(FontManager.getInstance().getFont("Hardpixel", 56));
        gameTitle.setTextFill(Color.web("#EAD9C2")); // Кремовий

        // Тінь для заголовка
        DropShadow titleShadow = new DropShadow();
        titleShadow.setColor(Color.web("#8B5A2B"));
        titleShadow.setOffsetX(3);
        titleShadow.setOffsetY(3);
        titleShadow.setRadius(8);
        gameTitle.setEffect(titleShadow);

        // Смайлик лапи в зістареному стилі
        Label catPaw = new Label("🐾");
        catPaw.setFont(FontManager.getInstance().getFont("Hardpixel", 120));
        catPaw.setTextFill(Color.web("#EAD9C2"));

        // Тінь для лапи
        DropShadow pawShadow = new DropShadow();
        pawShadow.setColor(Color.web("#8B5A2B"));
        pawShadow.setOffsetX(4);
        pawShadow.setOffsetY(4);
        pawShadow.setRadius(10);
        catPaw.setEffect(pawShadow);

        // Підпис у теплих тонах
        Label subtitle = new Label("СТЕЛС • ГРАБІЖ • ПРИГОДИ");
        subtitle.setFont(FontManager.getInstance().getFont("Hardpixel", 24));
        subtitle.setTextFill(Color.web("#D4A76A")); // Світло-коричневий

        // Підказка в зістареному стилі
        Label pressAnyKey = new Label(">>> НАТИСНІТЬ БУДЬ-ЯКУ КЛАВІШУ <<<");
        pressAnyKey.setFont(FontManager.getInstance().getFont("Hardpixel", 20));
        pressAnyKey.setTextFill(Color.web("#8B5A5A")); // Приглушений червоний

        // Блимання підказки
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
    }

    private Pane createCuteCatIcon() {
        Pane catPane = new Pane();
        catPane.setPrefSize(150, 150);

        // Тіло кота (овал)
        Circle catBody = new Circle(60, 80, 45);
        catBody.setFill(Color.web("#4A4A4A"));
        catBody.setStroke(Color.web("#FFE066"));
        catBody.setStrokeWidth(2);

        // Голова кота
        Circle catHead = new Circle(60, 35, 30);
        catHead.setFill(Color.web("#5A5A5A"));
        catHead.setStroke(Color.web("#FFE066"));
        catHead.setStrokeWidth(2);

        // Вушка (трикутники)
        Polygon leftEar = new Polygon();
        leftEar.getPoints().addAll(new Double[]{
                40.0, 20.0,  // верх
                25.0, 5.0,   // ліво
                40.0, 5.0    // право
        });
        leftEar.setFill(Color.web("#5A5A5A"));
        leftEar.setStroke(Color.web("#FFE066"));
        leftEar.setStrokeWidth(2);

        Polygon rightEar = new Polygon();
        rightEar.getPoints().addAll(new Double[]{
                80.0, 20.0,  // верх
                80.0, 5.0,   // ліво
                95.0, 5.0    // право
        });
        rightEar.setFill(Color.web("#5A5A5A"));
        rightEar.setStroke(Color.web("#FFE066"));
        rightEar.setStrokeWidth(2);

        // Очі (зелені, яскраві)
        Circle leftEye = new Circle(50, 30, 6);
        leftEye.setFill(Color.web("#00FF88"));
        Circle rightEye = new Circle(70, 30, 6);
        rightEye.setFill(Color.web("#00FF88"));

        // Зіниці
        Circle leftPupil = new Circle(50, 30, 3);
        leftPupil.setFill(Color.BLACK);
        Circle rightPupil = new Circle(70, 30, 3);
        rightPupil.setFill(Color.BLACK);

        // Ніс (трикутник)
        Polygon nose = new Polygon();
        nose.getPoints().addAll(new Double[]{
                60.0, 38.0,  // верх
                55.0, 45.0,  // ліво
                65.0, 45.0   // право
        });
        nose.setFill(Color.web("#FF6B9D"));

        // Маска грабіжника
        Rectangle mask = new Rectangle(35, 25, 50, 15);
        mask.setFill(Color.web("#1A1A1A"));
        mask.setArcWidth(10);
        mask.setArcHeight(10);

        // Хвіст
        Circle tail = new Circle(10, 70, 8);
        tail.setFill(Color.web("#4A4A4A"));
        tail.setStroke(Color.web("#FFE066"));
        tail.setStrokeWidth(2);

        // Лапки
        Circle leftPaw = new Circle(35, 110, 12);
        leftPaw.setFill(Color.web("#4A4A4A"));
        leftPaw.setStroke(Color.web("#FFE066"));
        leftPaw.setStrokeWidth(2);

        Circle rightPaw = new Circle(85, 110, 12);
        rightPaw.setFill(Color.web("#4A4A4A"));
        rightPaw.setStroke(Color.web("#FFE066"));
        rightPaw.setStrokeWidth(2);

        catPane.getChildren().addAll(tail, catBody, catHead, leftEar, rightEar,
                leftEye, rightEye, leftPupil, rightPupil,
                mask, nose, leftPaw, rightPaw);

        return catPane;
    }

    private void createMenuUI() {
        menuPane = new VBox(25);
        menuPane.setAlignment(Pos.CENTER);
        menuPane.setPrefSize(1280, 640);
        menuPane.setVisible(false);

        // Зістарений фон меню
        Stop[] menuStops = new Stop[] {
                new Stop(0, Color.web("#3C2F2F")),
                new Stop(0.5, Color.web("#2A2525")),
                new Stop(1, Color.web("#1E1A1A"))
        };
        LinearGradient menuGradient = new LinearGradient(0, 0, 0, 1, true, null, menuStops);
        Background menuBg = new Background(new BackgroundFill(menuGradient, null, null));
        menuPane.setBackground(menuBg);

        // Заголовок меню
        Label menuTitle = new Label("ВИБІР МІСІЇ");
        menuTitle.setFont(FontManager.getInstance().getFont("Hardpixel", 42));
        menuTitle.setTextFill(Color.web("#EAD9C2"));

        DropShadow menuTitleShadow = new DropShadow();
        menuTitleShadow.setColor(Color.web("#8B5A2B"));
        menuTitleShadow.setOffsetX(2);
        menuTitleShadow.setOffsetY(2);
        menuTitleShadow.setRadius(6);
        menuTitle.setEffect(menuTitleShadow);

        // Кнопки в зістареному стилі
        startHeistButton = createCuteButton("ПОЧАТИ ГРАБІЖ", Color.web("#4A7043"));
        selectLocationButton = createCuteButton("ВИБРАТИ ЛОКАЦІЮ", Color.web("#8B5A5A"));
        exitButton = createCuteButton("ВИЙТИ З ГРИ", Color.web("#7B3F3F"));
        confirmLevelButton = createCuteButton("ПІДТВЕРДИТИ ВИБІР", Color.web("#5C4B6A"));

        // Вибір локації в зістареному стилі
        locationChoice = new ComboBox<>();
        locationChoice.getItems().addAll(
                "БУДИНОК — Легкий: Тестовий злом для котів-новачків",
                "МУЗЕЙ — Середній: Перехитри охорону і викради артефакт МяуРа",
                "БАНК — Важкий: Проникни в банк корму «Whiskas & Co.»"

        );

// Стиль як у кнопки
        locationChoice.setStyle(
                "-fx-background-color: #2F2F2F;" +
                        "-fx-border-color: #D4A76A;" +
                        "-fx-border-width: 3px;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-font-family: 'Hardpixel';" +
                        "-fx-font-size: 18px;" +
                        "-fx-text-fill: #EAD9C2;" +
                        "-fx-pref-width: 500px;" +
                        "-fx-pref-height: 50px;"
        );

// Обробка вибору
        locationChoice.setOnAction(e -> {
            String selected = locationChoice.getValue();
            updateSelectedLevel();
        });

        locationChoice.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
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
            };
            return cell;
        });

// СТИЛІ ДЛЯ ОБРАНОГО ЕЛЕМЕНТА
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

        // Кнопка підтвердження вибору рівня
        confirmLevelButton.setVisible(false);

        // Контейнер для кнопок
        VBox buttonContainer = new VBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(
                startHeistButton,
                selectLocationButton,
                locationChoice,
                confirmLevelButton,
                exitButton
        );

        VBox.setMargin(menuTitle, new Insets(0, 0, 40, 0));

        menuPane.getChildren().addAll(menuTitle, buttonContainer);

        setupButtonActions();
    }

    private Button createCuteButton(String text, Color color) {
        Button button = new Button(text);
        button.setFont(FontManager.getInstance().getFont("Hardpixel", 22));
        button.setPrefSize(420, 60);

        // Зістарений стиль кнопки
        String baseStyle = String.format(
                "-fx-background-color: #2A2525;" +
                        "-fx-text-fill: %s;" +
                        "-fx-border-color: %s;" +
                        "-fx-border-width: 3px;" +
                        "-fx-border-radius: 15px;" +
                        "-fx-background-radius: 15px;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: 'Hardpixel';" +
                        "-fx-font-size: 22px;",
                toHexString(Color.web("#EAD9C2")), toHexString(color)
        );

        // Стиль при наведенні
        String hoverTextColor = color.equals(Color.web("#4A7043")) ? "#1E1A1A" : "#EAD9C2";
        String hoverStyle = String.format(
                "-fx-background-color: %s;" +
                        "-fx-text-fill: %s;" +
                        "-fx-border-color: #D4A76A;" +
                        "-fx-border-width: 4px;" +
                        "-fx-border-radius: 15px;" +
                        "-fx-background-radius: 15px;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: 'Hardpixel';" +
                        "-fx-font-size: 22px;",
                toHexString(color), hoverTextColor
        );

        button.setStyle(baseStyle);

        // Тінь для кнопок
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setColor(Color.web("#8B5A2B"));
        buttonShadow.setOffsetX(2);
        buttonShadow.setOffsetY(2);
        buttonShadow.setRadius(5);
        button.setEffect(buttonShadow);

        // Ефекти при наведенні
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

    private void createRootPane() {
        rootPane = new StackPane();
        rootPane.setPrefSize(1280, 640);
        // Зістарений фон
        Stop[] rootStops = new Stop[] {
                new Stop(0, Color.web("#3C2F2F")),
                new Stop(1, Color.web("#1E1A1A"))
        };
        LinearGradient rootGradient = new LinearGradient(0, 0, 0, 1, true, null, rootStops);
        Background rootBg = new Background(new BackgroundFill(rootGradient, null, null));
        rootPane.setBackground(rootBg);
        rootPane.getChildren().addAll(splashPane, menuPane);
    }

    private void startSplashSequence() {
        // Анімація появи лапи
        Label catPaw = (Label) splashPane.getChildren().get(1);
        logoScale = new ScaleTransition(Duration.seconds(1.2), catPaw);
        logoScale.setFromX(0.5);
        logoScale.setFromY(0.5);
        logoScale.setToX(1.0);
        logoScale.setToY(1.0);

        // Затримка перед можливістю пропуску
        logoScale.setDelay(Duration.seconds(0.3));
        logoScale.play();
    }

    private void transitionToMenu() {
        if (!showingSplash) return;

        showingSplash = false;

        // Зникнення заставки
        FadeTransition splashFadeOut = new FadeTransition(Duration.seconds(0.6), splashPane);
        splashFadeOut.setFromValue(1.0);
        splashFadeOut.setToValue(0.0);

        // Поява меню
        menuPane.setVisible(true);
        FadeTransition menuFadeIn = new FadeTransition(Duration.seconds(0.6), menuPane);
        menuFadeIn.setFromValue(0.0);
        menuFadeIn.setToValue(1.0);

        SequentialTransition transition = new SequentialTransition(splashFadeOut, menuFadeIn);
        transition.setOnFinished(e -> {
            splashPane.setVisible(false);
            menuVisible = true;
        });

        transition.play();
    }

    private void setupButtonActions() {
        startHeistButton.setOnAction(e -> startHeist());
        selectLocationButton.setOnAction(e -> toggleLocationSelection());
        confirmLevelButton.setOnAction(e -> confirmLocationChoice());
        exitButton.setOnAction(e -> System.exit(0));

        locationChoice.setOnAction(e -> updateSelectedLevel());
    }

    private void toggleLocationSelection() {
        levelSelectionVisible = !levelSelectionVisible;
        locationChoice.setVisible(levelSelectionVisible);
        confirmLevelButton.setVisible(levelSelectionVisible);

        if (levelSelectionVisible) {
            selectLocationButton.setText("ПРИХОВАТИ ЛОКАЦІЇ");
        } else {
            selectLocationButton.setText("ВИБРАТИ ЛОКАЦІЮ");
        }
    }

    private void updateSelectedLevel() {
        String selected = locationChoice.getValue();
        if (selected.contains("БУДИНОК")) {
            selectedLevel = 1;
        } else if (selected.contains("МУЗЕЙ")) {
            selectedLevel = 2;
        } else if (selected.contains("БАНК")) {
            selectedLevel = 3;
        }
    }

    private void confirmLocationChoice() {
        startLevel(selectedLevel);
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public void handleInput(KeyEvent event) {
        if (showingSplash) {
            // Будь-яка клавіша пропускає заставку
            transitionToMenu();
        } else if (menuVisible) {
            // Навігація в меню
            if (event.getCode() == KeyCode.ESCAPE) {
                System.exit(0);
            }
        }
    }

    public void startHeist() {
        // Почати з збереженого місця або з першого рівня
        continueGame();
    }

    public void startLevel(int levelId) {
        System.out.println("Починаємо рівень: " + levelId);
        GameManager.getInstance().loadLevel(levelId, true);
        hide();
    }

    public void continueGame() {
        System.out.println("Продовжуємо грабіж...");
        // Завантажуємо збережену гру або починаємо з першого рівня
        GameManager.getInstance().loadLevel(1, false);
        hide();
    }

    @Override
    public void show() {
        if (rootPane != null) {
            rootPane.setVisible(true);
            // Фокус для обробки клавіш
            rootPane.setFocusTraversable(true);
            rootPane.requestFocus();

            // Встановлюємо обробник клавіш
            rootPane.setOnKeyPressed(this::handleInput);
        }
    }

    @Override
    public void hide() {
        if (rootPane != null) {
            rootPane.setVisible(false);
        }
        // Повертаємо фокус грі і знімаємо паузу
        GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);
    }

    @Override
    public Node getRoot() {
        return rootPane;
    }
}