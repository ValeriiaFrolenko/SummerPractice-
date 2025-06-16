package ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextField;
import managers.FontManager;
import managers.GameManager;
import managers.UIManager;
import org.json.JSONObject;
import utils.GameLoader;

import java.util.Random;

public class InteractiveObjectWindow {
    private JSONObject config;
    private Pane root;
    private UIManager.WindowType windowType;

    // Змінні для браузера
    private int currentPage = 0;
    private final int totalPages = 2;
    private Pane browserContentArea;
    private Label pageIndicator;
    private Button prevButton;
    private Button nextButton;
    private String vaultCode;
    private GameLoader gameLoader;

    public InteractiveObjectWindow(UIManager.WindowType windowType, JSONObject config) {
        this.config = config;
        this.windowType = windowType;
        this.vaultCode = config.optString("code", "0000");
        this.root = new Pane();
        this.gameLoader = new GameLoader();
        initializeUI(config);
    }

    private void initializeUI(JSONObject config) {
        // Розмір залежить від типу вікна
        if (windowType == UIManager.WindowType.COMPUTER) {
            root.setPrefSize(800, 600);
            root.setMinSize(800, 600);
            root.setMaxSize(800, 600);
        } else {
            root.setPrefSize(400, 300);
            root.setMinSize(400, 300);
            root.setMaxSize(400, 300);
        }

        root.setMouseTransparent(false);
        root.setFocusTraversable(true);
        root.setPickOnBounds(true);
        root.setDisable(false);

        // Фон залежить від типу вікна
        if (windowType == UIManager.WindowType.COMPUTER) {
            root.setBackground(new Background(new BackgroundFill(
                    Color.rgb(20, 25, 35), CornerRadii.EMPTY, Insets.EMPTY)));
        } else {
            root.setBackground(new Background(new BackgroundFill(
                    Color.rgb(0, 0, 0, 0.8), CornerRadii.EMPTY, Insets.EMPTY)));
        }

        root.setOnMouseClicked(e -> {
            root.requestFocus();
            e.consume();
        });

        createWindowContent();

        javafx.application.Platform.runLater(() -> {
            root.requestFocus();
        });

        root.setOnKeyPressed(this::handleKeyPress);
    }

    private void handleKeyPress(KeyEvent event) {
        if (windowType == UIManager.WindowType.COMPUTER) {
            switch (event.getCode()) {
                case LEFT:
                    navigatePage(-1);
                    event.consume();
                    break;
                case RIGHT:
                    navigatePage(1);
                    event.consume();
                    break;
                case ESCAPE:
                    closeWindow();
                    event.consume();
                    break;
            }
        } else if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
            closeWindow();
            event.consume();
        }
    }

    private void createWindowContent() {
        switch (windowType) {
            case NOTE:
            case PICTURE:
                createNoteContent();
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

    private void createComputerContent() {
        // Заголовок браузера
        Pane browserBar = new Pane();
        browserBar.setBackground(new Background(new BackgroundFill(
                Color.rgb(60, 70, 80), new CornerRadii(5, 5, 0, 0, false), Insets.EMPTY)));
        browserBar.setPrefSize(760, 40);
        browserBar.setLayoutX(20);
        browserBar.setLayoutY(20);

        // Кнопки браузера
        addBrowserButton(browserBar, 10, 10, Color.rgb(255, 95, 86)); // Червона
        addBrowserButton(browserBar, 35, 10, Color.rgb(255, 189, 46)); // Жовта
        addBrowserButton(browserBar, 60, 10, Color.rgb(39, 201, 63)); // Зелена

        // Адресний рядок
        Label addressBar = new Label("🐾 CatBank Browser - Приватний режим");
        addressBar.setFont(FontManager.getInstance().getFont("Hardpixel", 14));
        addressBar.setTextFill(Color.WHITE);
        addressBar.setLayoutX(100);
        addressBar.setLayoutY(12);
        browserBar.getChildren().add(addressBar);

        root.getChildren().add(browserBar);

        // Область контенту браузера
        browserContentArea = new Pane();
        browserContentArea.setBackground(new Background(new BackgroundFill(
                Color.rgb(40, 45, 55), new CornerRadii(0, 0, 5, 5, false), Insets.EMPTY)));
        browserContentArea.setPrefSize(760, 500);
        browserContentArea.setLayoutX(20);
        browserContentArea.setLayoutY(60);

        root.getChildren().add(browserContentArea);

        // Навігаційні кнопки
        createNavigationButtons();

        // Індикатор сторінки
        pageIndicator = new Label();
        pageIndicator.setFont(FontManager.getInstance().getFont("Hardpixel", 14));
        pageIndicator.setTextFill(Color.LIGHTGRAY);
        pageIndicator.setLayoutX(370);
        pageIndicator.setLayoutY(570);
        root.getChildren().add(pageIndicator);

        // Кнопка закриття
        createComputerCloseButton();

        // Оновлюємо контент першої сторінки
        updateBrowserPage();
    }

    private void addBrowserButton(Pane parent, double x, double y, Color color) {
        Pane button = new Pane();
        button.setBackground(new Background(new BackgroundFill(
                color, new CornerRadii(10), Insets.EMPTY)));
        button.setPrefSize(20, 20);
        button.setLayoutX(x);
        button.setLayoutY(y);
        parent.getChildren().add(button);
    }

    private void createNavigationButtons() {
        prevButton = new Button("◀ Назад");
        prevButton.setFont(FontManager.getInstance().getFont("Hardpixel", 14));
        prevButton.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        prevButton.setLayoutX(600);
        prevButton.setLayoutY(570);
        prevButton.setPrefSize(80, 25);

        prevButton.setOnAction(e -> navigatePage(-1));
        prevButton.setOnMouseEntered(e ->
                prevButton.setStyle("-fx-background-color: #5a6578; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;"));
        prevButton.setOnMouseExited(e ->
                prevButton.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;"));

        nextButton = new Button("Вперед ▶");
        nextButton.setFont(FontManager.getInstance().getFont("Hardpixel", 14));
        nextButton.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        nextButton.setLayoutX(690);
        nextButton.setLayoutY(570);
        nextButton.setPrefSize(80, 25);

        nextButton.setOnAction(e -> navigatePage(1));
        nextButton.setOnMouseEntered(e ->
                nextButton.setStyle("-fx-background-color: #5a6578; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;"));
        nextButton.setOnMouseExited(e ->
                nextButton.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;"));

        root.getChildren().addAll(prevButton, nextButton);
    }

    private void createComputerCloseButton() {
        Button closeButton = new Button("✖");
        closeButton.setFont(FontManager.getInstance().getFont("Hardpixel", 18));
        closeButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3;");
        closeButton.setLayoutX(750);
        closeButton.setLayoutY(10);
        closeButton.setPrefSize(30, 30);

        closeButton.setOnAction(e -> closeWindow());
        closeButton.setOnMouseEntered(e ->
                closeButton.setStyle("-fx-background-color: #ff6666; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3;"));
        closeButton.setOnMouseExited(e ->
                closeButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3;"));

        root.getChildren().add(closeButton);
    }

    private void navigatePage(int direction) {
        int newPage = currentPage + direction;
        if (newPage >= 0 && newPage < totalPages) {
            currentPage = newPage;
            updateBrowserPage();
        }
    }

    private void updateBrowserPage() {
        browserContentArea.getChildren().clear();

        switch (currentPage) {
            case 0:
                createGoogleSearchPage();
                break;
            case 1:
                createVaultCodePage();
                break;

        }

        // Оновлюємо індикатор сторінки
        pageIndicator.setText((currentPage + 1) + " / " + totalPages);

        // Оновлюємо стан кнопок
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(currentPage == totalPages - 1);

        if (prevButton.isDisabled()) {
            prevButton.setStyle("-fx-background-color: #2d3748; -fx-text-fill: #666; -fx-border-radius: 5; -fx-background-radius: 5;");
        }
        if (nextButton.isDisabled()) {
            nextButton.setStyle("-fx-background-color: #2d3748; -fx-text-fill: #666; -fx-border-radius: 5; -fx-background-radius: 5;");
        }
    }

    private void createGoogleSearchPage() {
        // Білий фон Google
        Rectangle background = new Rectangle(760, 500);
        background.setFill(Color.WHITE);
        browserContentArea.getChildren().add(background);

        // Логотип Google (стилізований)
        Label googleLogo = new Label("🔍 Google");
        googleLogo.setFont(FontManager.getInstance().getFont("Hardpixel", 34));
        googleLogo.setStyle("-fx-font-weight: bold;");
        googleLogo.setTextFill(Color.rgb(66, 133, 244));
        googleLogo.setLayoutX(320);
        googleLogo.setLayoutY(30);
        browserContentArea.getChildren().add(googleLogo);

        // Пошукове поле
        TextField searchField = new TextField();
        searchField.setFont(FontManager.getInstance().getFont("Hardpixel", 16));
        searchField.setStyle("-fx-background-color: white; -fx-border-color: #dadce0; -fx-border-width: 1; -fx-border-radius: 24; -fx-background-radius: 24;");
        searchField.setPrefSize(400, 40);
        searchField.setLayoutX(180);
        searchField.setLayoutY(90);
        browserContentArea.getChildren().add(searchField);

        // Заголовок історії
        Label historyTitle = new Label("Історія пошуку:");
        historyTitle.setFont(FontManager.getInstance().getFont("Hardpixel", 18));
        historyTitle.setStyle("-fx-font-weight: bold;");
        historyTitle.setTextFill(Color.rgb(51, 51, 51));
        historyTitle.setLayoutX(50);
        historyTitle.setLayoutY(160);
        browserContentArea.getChildren().add(historyTitle);

        // Історія пошуку
        String[] searchHistory = {
                "🔍 Як прибрати шерсть з клавіатури",
                "🔍 Чи можна купити валер'янку оптом",
                "🔍 Як зламати власний сейф, якщо забув код",
                "🔍 Чому господар каже що в мисці є корм коли я бачу дно"
        };

        for (int i = 0; i < searchHistory.length; i++) {
            Rectangle searchItem = new Rectangle(650, 35);
            searchItem.setFill(Color.rgb(248, 249, 250));
            searchItem.setLayoutX(50);
            searchItem.setLayoutY(190 + (i * 45));

            if (i % 2 == 0) {
                searchItem.setOnMouseEntered(e -> searchItem.setFill(Color.rgb(241, 243, 244)));
                searchItem.setOnMouseExited(e -> searchItem.setFill(Color.rgb(248, 249, 250)));
            }

            browserContentArea.getChildren().add(searchItem);

            Label searchText = new Label(searchHistory[i]);
            searchText.setFont(FontManager.getInstance().getFont("Hardpixel", 16));
            searchText.setTextFill(Color.rgb(26, 13, 171));
            searchText.setLayoutX(60);
            searchText.setLayoutY(197 + (i * 45));

            final int index = i;
            searchText.setOnMouseEntered(e -> searchText.setStyle("-fx-underline: true;"));
            searchText.setOnMouseExited(e -> searchText.setStyle("-fx-underline: false;"));

            browserContentArea.getChildren().add(searchText);
        }
    }

    private void createVaultCodePage() {
        // Темно-синій фон для банківської системи
        Rectangle background = new Rectangle(760, 500);
        background.setFill(Color.rgb(0, 32, 64));
        browserContentArea.getChildren().add(background);

        // Заголовок системи
        Label systemTitle = new Label("🏛️ CATBANK SECURITY SYSTEM 🏛️");
        systemTitle.setFont(FontManager.getInstance().getFont("Hardpixel", 22));
        systemTitle.setStyle("-fx-font-weight: bold;");
        systemTitle.setTextFill(Color.rgb(0, 255, 255));
        systemTitle.setLayoutX(180);
        systemTitle.setLayoutY(50);
        browserContentArea.getChildren().add(systemTitle);

        // Рамка для секретної інформації
        Rectangle codeFrame = new Rectangle(500, 200);
        codeFrame.setFill(Color.TRANSPARENT);
        codeFrame.setStroke(Color.rgb(0, 255, 255));
        codeFrame.setStrokeWidth(3);
        codeFrame.setArcWidth(15);
        codeFrame.setArcHeight(15);
        codeFrame.setLayoutX(130);
        codeFrame.setLayoutY(120);
        browserContentArea.getChildren().add(codeFrame);

        // Іконка безпеки
        Label securityIcon = new Label("🔐");
        securityIcon.setFont(FontManager.getInstance().getFont("Hardpixel", 40));
        securityIcon.setLayoutX(150);
        securityIcon.setLayoutY(140);
        browserContentArea.getChildren().add(securityIcon);

        // Заголовок коду
        Label codeTitle = new Label("КОД ВІД ГОЛОВНОГО СХОВИЩА:");
        codeTitle.setFont(FontManager.getInstance().getFont("Hardpixel", 18));
        codeTitle.setStyle("-fx-font-weight: bold;");
        codeTitle.setTextFill(Color.rgb(255, 255, 0));
        codeTitle.setLayoutX(220);
        codeTitle.setLayoutY(150);
        browserContentArea.getChildren().add(codeTitle);

        // Код сховища (велике яскраве відображення)
        Label vaultCodeDisplay = new Label(vaultCode);
        vaultCodeDisplay.setFont(FontManager.getInstance().getFont("Hardpixel", 48));
        vaultCodeDisplay.setStyle("-fx-font-weight: bold;");
        vaultCodeDisplay.setTextFill(Color.rgb(255, 255, 0));
        vaultCodeDisplay.setLayoutX(320);
        vaultCodeDisplay.setLayoutY(180);
        browserContentArea.getChildren().add(vaultCodeDisplay);

        // Розділова лінія
        Rectangle separator = new Rectangle(400, 2);
        separator.setFill(Color.rgb(0, 255, 255));
        separator.setLayoutX(180);
        separator.setLayoutY(260);
        browserContentArea.getChildren().add(separator);

        // Попередження
        Label warningLabel = new Label("⚠️ УВАГА: КОНФІДЕНЦІЙНА ІНФОРМАЦІЯ ⚠️");
        warningLabel.setFont(FontManager.getInstance().getFont("Hardpixel", 16));
        warningLabel.setStyle("-fx-font-weight: bold;");
        warningLabel.setTextFill(Color.rgb(255, 69, 0));
        warningLabel.setLayoutX(200);
        warningLabel.setLayoutY(280);
        browserContentArea.getChildren().add(warningLabel);

        // Інструкції
        Label instructions = new Label("Використовуйте цей код для відкриття головного сховища банку.\nДоступ дозволено лише авторизованому персоналу.");
        instructions.setFont(FontManager.getInstance().getFont("Hardpixel", 14));
        instructions.setTextFill(Color.rgb(200, 200, 200));
        instructions.setLayoutX(180);
        instructions.setLayoutY(350);
        instructions.setWrapText(true);
        instructions.setPrefWidth(400);
        browserContentArea.getChildren().add(instructions);

        // Дата та час доступу
        Label accessTime = new Label("Останній доступ: 15.06.2025 14:32:17");
        accessTime.setFont(FontManager.getInstance().getFont("Hardpixel", 12));
        accessTime.setTextFill(Color.GRAY);
        accessTime.setLayoutX(180);
        accessTime.setLayoutY(450); // Adjusted to avoid frame overlap
        browserContentArea.getChildren().add(accessTime);

        // ID сесії
        Label sessionId = new Label("Session ID: CAT-" + new Random().nextInt(100000));
        sessionId.setFont(FontManager.getInstance().getFont("Hardpixel", 12));
        sessionId.setTextFill(Color.GRAY);
        sessionId.setLayoutX(180);
        sessionId.setLayoutY(470); // Adjusted to avoid frame overlap
        browserContentArea.getChildren().add(sessionId);

        // Статус безпеки
        Rectangle statusBar = new Rectangle(200, 25);
        statusBar.setFill(Color.rgb(34, 139, 34));
        statusBar.setArcWidth(5);
        statusBar.setArcHeight(5);
        statusBar.setLayoutX(450);
        statusBar.setLayoutY(450); // Adjusted to avoid frame overlap
        browserContentArea.getChildren().add(statusBar);

        Label statusText = new Label("🔒 SECURE CONNECTION");
        statusText.setFont(FontManager.getInstance().getFont("Hardpixel", 13));
        statusText.setStyle("-fx-font-weight: bold;");
        statusText.setTextFill(Color.WHITE);
        statusText.setLayoutX(460);
        statusText.setLayoutY(450); // Adjusted to avoid frame overlap
        browserContentArea.getChildren().add(statusText);

        // Анімований індикатор активності
        Label activityIndicator = new Label("●");
        activityIndicator.setFont(FontManager.getInstance().getFont("Hardpixel", 20));
        activityIndicator.setTextFill(Color.rgb(0, 255, 0));
        activityIndicator.setLayoutX(600);
        activityIndicator.setLayoutY(130);
        browserContentArea.getChildren().add(activityIndicator);

        // Збереження коду в GameManager для використання в грі
        GameManager.getInstance().setCode(vaultCode);
    }

    private void createNoteContent() {
        // Set background image
        root.setBackground(new Background(new BackgroundImage(
                gameLoader.loadImage("UI/note.png"),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false)
        )));

        Label noteContent = new Label("Код: " + vaultCode);
        noteContent.setFont(FontManager.getInstance().getFont("EpsilonCTT", 20));
        noteContent.setTextFill(Color.BLACK);
        noteContent.setTextAlignment(TextAlignment.CENTER);
        noteContent.setAlignment(Pos.CENTER);
        noteContent.setPrefWidth(400); // Full width of pane for horizontal centering
        noteContent.setWrapText(true);
        noteContent.setLayoutX(0);
        noteContent.setLayoutY(140); // Vertically centered below title
        root.getChildren().add(noteContent);

        createCloseButton();
    }

    private void createPictureContent() {
        Label pictureTitle = new Label(config.optString("title", "Зображення"));
        pictureTitle.setFont(FontManager.getInstance().getFont("Hardpixel", 20));
        pictureTitle.setStyle("-fx-font-weight: bold;");
        pictureTitle.setTextFill(Color.WHITE);
        pictureTitle.setLayoutX(20);
        pictureTitle.setLayoutY(20);
        root.getChildren().add(pictureTitle);

        // Заміна зображення на емодзі
        String pictureEmoji = config.optString("emoji", "🖼️");
        Label pictureDisplay = new Label(pictureEmoji);
        pictureDisplay.setFont(FontManager.getInstance().getFont("Hardpixel", 80));
        pictureDisplay.setLayoutX(160);
        pictureDisplay.setLayoutY(100);
        root.getChildren().add(pictureDisplay);

        String description = config.optString("description", "");
        if (!description.isEmpty()) {
            Label pictureDescription = new Label(description);
            pictureDescription.setFont(FontManager.getInstance().getFont("Hardpixel", 14));
            pictureDescription.setTextFill(Color.LIGHTGRAY);
            pictureDescription.setLayoutX(20);
            pictureDescription.setLayoutY(220);
            pictureDescription.setWrapText(true);
            pictureDescription.setPrefWidth(360);
            root.getChildren().add(pictureDescription);
        }

        createCloseButton();
    }

    private void createEndGameContent() {
        boolean isVictory = (windowType == UIManager.WindowType.VICTORY);

        Label title = new Label(isVictory ? "🎉 ПЕРЕМОГА! 🎉" : "💀 ПОРАЗКА 💀");
        title.setFont(FontManager.getInstance().getFont("Hardpixel", 26));
        title.setStyle("-fx-font-weight: bold;");
        title.setTextFill(isVictory ? Color.GOLD : Color.RED);
        title.setLayoutX(100);
        title.setLayoutY(50);
        root.getChildren().add(title);

        String message = config.optString("message",
                isVictory ? "Ви успішно завершили гру!" : "Спробуйте ще раз!");

        Label messageLabel = new Label(message);
        messageLabel.setFont(FontManager.getInstance().getFont("Hardpixel", 18));
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setLayoutX(50);
        messageLabel.setLayoutY(120);
        messageLabel.setWrapText(true);
        messageLabel.setPrefWidth(300);
        root.getChildren().add(messageLabel);

        Button restartButton = new Button("Почати заново");
        restartButton.setFont(FontManager.getInstance().getFont("Hardpixel", 16));
        restartButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        restartButton.setLayoutX(150);
        restartButton.setLayoutY(200);
        restartButton.setPrefSize(120, 35);

        restartButton.setOnAction(e -> {
            //GameManager.getInstance().restartGame();
            closeWindow();
        });

        root.getChildren().add(restartButton);

        createCloseButton();
    }

    private void createCloseButton() {
        Button closeButton = new Button("✖");
        closeButton.setFont(FontManager.getInstance().getFont("Hardpixel", 14));
        closeButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3;");

        if (windowType == UIManager.WindowType.COMPUTER) {
            return; // Для комп'ютера кнопка закриття вже створена
        }

        closeButton.setLayoutX(320); // Top-right corner
        closeButton.setLayoutY(10); // Moved to top
        closeButton.setPrefSize(60, 30);


        closeButton.setOnAction(e -> closeWindow());
        closeButton.setOnMouseEntered(e ->
                closeButton.setStyle("-fx-background-color: #ff6666; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3;"));
        closeButton.setOnMouseExited(e ->
                closeButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3;"));

        root.getChildren().add(closeButton);
    }

    public Node getUI() {
        return root;
    }

    private void closeWindow() {
        try {
            UIManager.getInstance().hideInteractiveObjectUI();
        } catch (Exception ex) {
            System.out.println("Error calling hideInteractiveObjectUI(): " + ex.getMessage());
        }

        if (root.getParent() instanceof Pane) {
            Pane parent = (Pane) root.getParent();
            parent.getChildren().remove(root);
            GameManager.getInstance().setGameState(GameManager.GameState.PLAYING);
        }
    }
}