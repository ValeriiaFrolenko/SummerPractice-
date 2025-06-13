package ui;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
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
        root.setPrefSize(400, 300);

        // Налаштування rootPane
        root.setMouseTransparent(false);
        root.setFocusTraversable(true);
        root.setPickOnBounds(true); // ВАЖЛИВО: дозволяє кліки по всій області

        // Фон з напівпрозорістю
        root.setBackground(new Background(new BackgroundFill(
                javafx.scene.paint.Color.rgb(0, 0, 0, 0.8), CornerRadii.EMPTY, Insets.EMPTY)));

        // Обробник кліку для root - споживає події, щоб вони не проходили далі
        root.setOnMouseClicked(e -> {
            System.out.println("Mouse clicked on InteractiveObjectWindow root: " + windowType + " at (" + e.getX() + ", " + e.getY() + ")");
            root.requestFocus();
            e.consume(); // Споживаємо подію
        });

        // Заголовок
        Label title = new Label(getWindowTitle());
        title.setFont(FontManager.getInstance().getFont("EpsilonCTT", 20));
        title.setTextFill(javafx.scene.paint.Color.WHITE);
        title.setLayoutX(20);
        title.setLayoutY(20);
        title.setMouseTransparent(false);
        title.setOnMouseClicked(e -> {
            System.out.println("Mouse clicked on title Label: " + windowType);
            e.consume();
        });
        root.getChildren().add(title);

        // Кнопка закриття
        Button closeButton = new Button("✖");
        closeButton.setFont(FontManager.getInstance().getFont("EpsilonCTT", 16));
        closeButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3;");
        closeButton.setLayoutX(360);
        closeButton.setLayoutY(10);
        closeButton.setPrefSize(30, 30);
        closeButton.setOnAction(e -> {
            System.out.println("Close button clicked: " + windowType);
            uiManager.hideCurrentWindow();
        });
        closeButton.setOnMouseClicked(e -> {
            System.out.println("Close button mouse clicked: " + windowType);
            e.consume();
        });
        root.getChildren().add(closeButton);

        // Контент залежно від типу вікна
        createWindowContent();

        // Запитуємо фокус після створення
        javafx.application.Platform.runLater(() -> {
            root.requestFocus();
        });
    }

    private String getWindowTitle() {
        switch (windowType) {
            case NOTE: return "Нотатка";
            case PICTURE: return "Картина";
            case COMPUTER: return "Комп'ютер";
            case VICTORY: return "Перемога!";
            case GAME_OVER: return "Кінець гри";
            default: return windowType.toString();
        }
    }

    private void createWindowContent() {
        switch (windowType) {
            case NOTE:
                createNoteContent();
                break;
            case PICTURE:
                createPictureContent();
                break;
            case COMPUTER:
                createComputerContent();
                break;
            case VICTORY:
            case GAME_OVER:
                createEndGameContent();
                break;
        }
    }

    private void createNoteContent() {
        // Стилізація для нотатки
        root.setStyle("-fx-background-color: #F5E050; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 3, 3); " +
                "-fx-border-color: #D4B82A; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5;");

        String randomCode = generateRandomCode();
        GameManager.getInstance().setNoteCode(randomCode);

        Label codeLabel = new Label("Код:");
        codeLabel.setFont(FontManager.getInstance().getFont("EpsilonCTT", 18));
        codeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C1810;");
        codeLabel.setLayoutX(20);
        codeLabel.setLayoutY(60);
        root.getChildren().add(codeLabel);

        Label noteContent = new Label(randomCode);
        noteContent.setFont(FontManager.getInstance().getFont("EpsilonCTT", 36));
        noteContent.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C1810;");
        noteContent.setLayoutX(125);
        noteContent.setLayoutY(132);
        noteContent.setMouseTransparent(false);
        noteContent.setOnMouseClicked(e -> {
            System.out.println("Mouse clicked on note content: " + randomCode);
            e.consume();
        });
        root.getChildren().add(noteContent);
    }

    private void createPictureContent() {
        Label pictureContent = new Label("Це красива картина...\nМожливо, щось приховує?");
        pictureContent.setFont(FontManager.getInstance().getFont("EpsilonCTT", 16));
        pictureContent.setTextFill(javafx.scene.paint.Color.WHITE);
        pictureContent.setLayoutX(20);
        pictureContent.setLayoutY(60);
        pictureContent.setMouseTransparent(false);
        pictureContent.setOnMouseClicked(e -> {
            System.out.println("Mouse clicked on picture content");
            e.consume();
        });
        root.getChildren().add(pictureContent);
    }

    private void createComputerContent() {
        Label computerContent = new Label("Комп'ютер активний.\nМожна знайти корисну інформацію.");
        computerContent.setFont(FontManager.getInstance().getFont("EpsilonCTT", 16));
        computerContent.setTextFill(javafx.scene.paint.Color.WHITE);
        computerContent.setLayoutX(20);
        computerContent.setLayoutY(60);
        computerContent.setMouseTransparent(false);
        computerContent.setOnMouseClicked(e -> {
            System.out.println("Mouse clicked on computer content");
            e.consume();
        });
        root.getChildren().add(computerContent);
    }

    private void createEndGameContent() {
        String message = windowType == UIManager.WindowType.VICTORY ? "Вітаємо! Ви перемогли!" : "Гра закінчена. Спробуйте ще раз!";

        Label endGameContent = new Label(message);
        endGameContent.setFont(FontManager.getInstance().getFont("EpsilonCTT", 20));
        endGameContent.setTextFill(javafx.scene.paint.Color.WHITE);
        endGameContent.setLayoutX(20);
        endGameContent.setLayoutY(100);
        endGameContent.setMouseTransparent(false);
        endGameContent.setOnMouseClicked(e -> {
            System.out.println("Mouse clicked on end game content: " + windowType);
            e.consume();
        });
        root.getChildren().add(endGameContent);

        // Додаткова кнопка для перезапуску або виходу
        Button actionButton = new Button(windowType == UIManager.WindowType.VICTORY ? "Наступний рівень" : "Спробувати знову");
        actionButton.setFont(FontManager.getInstance().getFont("EpsilonCTT", 14));
        actionButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        actionButton.setLayoutX(20);
        actionButton.setLayoutY(200);
        actionButton.setPrefSize(150, 30);
        actionButton.setOnAction(e -> {
            System.out.println("Action button clicked: " + windowType);
            // Тут можна додати логіку для перезапуску або переходу на наступний рівень
            uiManager.hideCurrentWindow();
        });
        actionButton.setOnMouseClicked(e -> {
            System.out.println("Action button mouse clicked: " + windowType);
            e.consume();
        });
        root.getChildren().add(actionButton);
    }

    private String generateRandomCode() {
        Random random = new Random();
        int code = random.nextInt(9000) + 1000;
        return String.valueOf(code);
    }

    @Override
    public void show() {
        uiManager.getOverlayPane().getChildren().add(root);

        // Центрування вікна
        javafx.application.Platform.runLater(() -> {
            double centerX = (uiManager.getOverlayPane().getWidth() - root.getPrefWidth()) / 2;
            double centerY = (uiManager.getOverlayPane().getHeight() - root.getPrefHeight()) / 2;
            root.setLayoutX(centerX);
            root.setLayoutY(centerY);
            root.requestFocus();
            System.out.println("Window positioned at: " + centerX + ", " + centerY);
        });

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