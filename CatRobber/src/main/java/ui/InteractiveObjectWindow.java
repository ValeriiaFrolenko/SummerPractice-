package ui;

import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
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

public class InteractiveObjectWindow {
    private JSONObject config;
    private Pane root;
    private UIManager.WindowType windowType;

    public InteractiveObjectWindow(UIManager.WindowType windowType, JSONObject config) {
        this.config = config;
        this.windowType = windowType;
        this.root = new Pane();
        initializeUI(config);
    }

    private void initializeUI(JSONObject config) {
        root.setPrefSize(400, 300);
        root.setMinSize(400, 300);
        root.setMaxSize(400, 300);

        // Налаштування rootPane - ВИПРАВЛЕНО
        root.setMouseTransparent(false);  // Дозволяємо реакцію на мишку
        root.setFocusTraversable(true);
        root.setPickOnBounds(true);       // Дозволяємо кліки по всій області
        root.setDisable(false);           // Переконуємось що компонент активний

        // Фон з напівпрозорістю
        root.setBackground(new Background(new BackgroundFill(
                javafx.scene.paint.Color.rgb(0, 0, 0, 0.8), CornerRadii.EMPTY, Insets.EMPTY)));

        // Обробник кліку для root - ВИПРАВЛЕНО
        root.setOnMouseClicked(e -> {
            root.requestFocus();
            e.consume(); // Споживаємо подію
        });

        Label title = new Label(getWindowTitle());
        title.setFont(FontManager.getInstance().getFont("EpsilonCTT", 20));
        title.setTextFill(javafx.scene.paint.Color.WHITE);
        title.setLayoutX(20);
        title.setLayoutY(20);
        title.setMouseTransparent(false); // Дозволяємо взаємодію
        title.setDisable(false);          // Переконуємось що активний

        title.setOnMouseClicked(e -> {
            e.consume();
        });

        // Додаємо hover ефект для заголовка
        title.setOnMouseEntered(e -> {
            title.setStyle("-fx-text-fill: #CCCCCC;");
        });

        title.setOnMouseExited(e -> {
            title.setStyle("-fx-text-fill: white;");
        });

        root.getChildren().add(title);

        // Кнопка закриття - ВИПРАВЛЕНО
        Button closeButton = new Button("✖");
        closeButton.setFont(FontManager.getInstance().getFont("EpsilonCTT", 16));
        closeButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3;");
        closeButton.setLayoutX(360);
        closeButton.setLayoutY(10);
        closeButton.setPrefSize(30, 30);
        closeButton.setMouseTransparent(false); // Дозволяємо взаємодію
        closeButton.setDisable(false);          // Переконуємось що активна

        closeButton.setOnAction(e -> {

            // Спершу пробуємо стандартний спосіб
            try {
                UIManager.getInstance().hideInteractiveObjectUI();
            } catch (Exception ex) {
                System.out.println("Error calling hideInteractiveObjectUI(): " + ex.getMessage());
            }

            // Потім пробуємо прямий спосіб
            try {
                closeWindow();
            } catch (Exception ex) {
                System.out.println("Error in direct closeWindow(): " + ex.getMessage());
            }
        });

        closeButton.setOnMouseClicked(e -> {
            // Прямий спосіб закриття
            try {
                closeWindow();
            } catch (Exception ex) {
            }
            e.consume();
        });

        // Hover ефект для кнопки закриття
        closeButton.setOnMouseEntered(e -> {
            closeButton.setStyle("-fx-background-color: #ff6666; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3;");
        });

        closeButton.setOnMouseExited(e -> {
            closeButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3;");
        });

        root.getChildren().add(closeButton);

        // Контент залежно від типу вікна
        createWindowContent();

        // Запитуємо фокус після створення - ВИПРАВЛЕНО
        javafx.application.Platform.runLater(() -> {
            root.requestFocus();
        });

        // Додаємо обробник клавіші ESC для закриття
        root.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                try {
                    UIManager.getInstance().hideInteractiveObjectUI();
                } catch (Exception ex) {
                    System.out.println("Error closing with ESC: " + ex.getMessage());
                    try {
                        UIManager.getInstance().forceHideInteractiveObjectUI();
                    } catch (Exception ex2) {
                        System.out.println("Force close with ESC failed: " + ex2.getMessage());
                    }
                }
                e.consume();
            }
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

        // Отримуємо код із properties
        String noteCode = config.optString("noteCode", "0000"); // Значення за замовчуванням, якщо код відсутній
        GameManager.getInstance().setNoteCode(noteCode);
        Label codeLabel = new Label("Код:");
        codeLabel.setFont(FontManager.getInstance().getFont("EpsilonCTT", 18));
        codeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C1810;");
        codeLabel.setLayoutX(20);
        codeLabel.setLayoutY(60);
        codeLabel.setMouseTransparent(false);
        codeLabel.setDisable(false);
        root.getChildren().add(codeLabel);

        Label noteContent = new Label(noteCode);
        noteContent.setFont(FontManager.getInstance().getFont("EpsilonCTT", 36));
        noteContent.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C1810;");
        noteContent.setLayoutX(125);
        noteContent.setLayoutY(132);
        noteContent.setMouseTransparent(false);
        noteContent.setDisable(false);

        noteContent.setOnMouseClicked(e -> {
            e.consume();
        });

        // Hover ефект для коду
        noteContent.setOnMouseEntered(e -> {
            noteContent.setStyle("-fx-font-weight: bold; -fx-text-fill: #1A0C06; -fx-background-color: rgba(255,255,255,0.3);");
        });

        noteContent.setOnMouseExited(e -> {
            noteContent.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C1810;");
        });

        root.getChildren().add(noteContent);
    }

    private void createPictureContent() {
        Label pictureContent = new Label("Це красива картина...\nМожливо, щось приховує?");
        pictureContent.setFont(FontManager.getInstance().getFont("EpsilonCTT", 16));
        pictureContent.setTextFill(javafx.scene.paint.Color.WHITE);
        pictureContent.setLayoutX(20);
        pictureContent.setLayoutY(60);
        pictureContent.setMouseTransparent(false); // ВИПРАВЛЕНО
        pictureContent.setDisable(false);

        pictureContent.setOnMouseClicked(e -> {
            e.consume();
        });

        // Hover ефект
        pictureContent.setOnMouseEntered(e -> {
            pictureContent.setStyle("-fx-text-fill: #CCCCCC;");
        });

        pictureContent.setOnMouseExited(e -> {
            pictureContent.setStyle("-fx-text-fill: white;");
        });

        root.getChildren().add(pictureContent);
    }

    private void createComputerContent() {
        Label computerContent = new Label("Комп'ютер активний.\nМожна знайти корисну інформацію.");
        computerContent.setFont(FontManager.getInstance().getFont("EpsilonCTT", 16));
        computerContent.setTextFill(javafx.scene.paint.Color.WHITE);
        computerContent.setLayoutX(20);
        computerContent.setLayoutY(60);
        computerContent.setMouseTransparent(false); // ВИПРАВЛЕНО
        computerContent.setDisable(false);

        computerContent.setOnMouseClicked(e -> {
            e.consume();
        });

        // Hover ефект
        computerContent.setOnMouseEntered(e -> {
            computerContent.setStyle("-fx-text-fill: #CCCCCC;");
        });

        computerContent.setOnMouseExited(e -> {
            computerContent.setStyle("-fx-text-fill: white;");
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
        endGameContent.setMouseTransparent(false); // ВИПРАВЛЕНО
        endGameContent.setDisable(false);

        endGameContent.setOnMouseClicked(e -> {
            e.consume();
        });

        root.getChildren().add(endGameContent);

        // Додаткова кнопка для перезапуску або виходу - ВИПРАВЛЕНО
        Button actionButton = new Button(windowType == UIManager.WindowType.VICTORY ? "Наступний рівень" : "Спробувати знову");
        actionButton.setFont(FontManager.getInstance().getFont("EpsilonCTT", 14));
        actionButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        actionButton.setLayoutX(20);
        actionButton.setLayoutY(200);
        actionButton.setPrefSize(150, 30);
        actionButton.setMouseTransparent(false); // ВИПРАВЛЕНО
        actionButton.setDisable(false);

        actionButton.setOnAction(e -> {
            // Тут можна додати логіку для перезапуску або переходу на наступний рівень
            UIManager.getInstance().hideInteractiveObjectUI();
        });

        actionButton.setOnMouseClicked(e -> {
            e.consume();
        });

        // Hover ефект для action кнопки
        actionButton.setOnMouseEntered(e -> {
            actionButton.setStyle("-fx-background-color: #5CBF60; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        });

        actionButton.setOnMouseExited(e -> {
            actionButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        });

        root.getChildren().add(actionButton);
    }

    private String generateRandomCode() {
        Random random = new Random();
        int code = random.nextInt(9000) + 1000;
        return String.valueOf(code);
    }

    public Node getUI() {
        return root;
    }


    // Метод для прямого закриття вікна
    public void closeWindow() {

        if (root.getParent() instanceof Pane) {
            Pane parent = (Pane) root.getParent();

            parent.getChildren().remove(root);

            // Встановлюємо стан гри
            GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);

        } else {
            System.out.println("Cannot remove - parent is not a Pane: " +
                    (root.getParent() != null ? root.getParent().getClass().getSimpleName() : "null"));
        }
    }
}