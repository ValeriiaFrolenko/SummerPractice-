package ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.TextAlignment;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.TextField;
import managers.FontManager;
import managers.GameManager;
import managers.SoundManager;
import managers.UIManager;
import org.json.JSONObject;
import utils.GameLoader;

import java.util.Random;

/**
 * Клас, який представляє вікно з інтерактивним об'єктом у грі.
 * Відповідає за створення UI, обробку подій та навігацію вмістом.
 */
public class InteractiveObjectWindow {
    private JSONObject config; //об’єкт конфігурації у форматі JSON, що містить налаштування вікна або браузера
    private Pane root; //кореневий контейнер цього інтерфейсного вікна
    private UIManager.WindowType windowType; //тип вікна, що визначається через UIManager.WindowType (наприклад, TERMINAL, COMPUTER тощо)

    // Змінні для браузера
    private int currentPage = 0; //поточна сторінка, яка відображається в браузері
    private final int totalPages = 2; //загальна кількість сторінок у браузері
    private Pane browserContentArea; //область, у якій відображається вміст поточної сторінки браузера
    private Label pageIndicator; //позначка індикації номера сторінки (наприклад, "1 з 2")
    private Button prevButton; //кнопка для переходу на попередню сторінку
    private Button nextButton; //кнопка для переходу на наступну сторінку
    private String vaultCode; //код сейфу, який може бути зчитаний або введений користувачем
    private GameLoader gameLoader; //завантажувач ресурсів
    private final SoundManager soundManager = SoundManager.getInstance(); //менеджер звуків, що відповідає за відтворення аудіо-ефектів

    /**
     * Конструктор створює вікно заданого типу з налаштуваннями з JSON-конфігурації
     * @param windowType тип вікна
     * @param config конфігурація у форматі JSON
     */
    public InteractiveObjectWindow(UIManager.WindowType windowType, JSONObject config) {
        this.config = config;
        this.windowType = windowType;
        this.vaultCode = GameManager.getInstance().getCode();
        this.root = new Pane();
        this.gameLoader = new GameLoader();
        initializeUI(config);
    }

    /**
     * Ініціалізує графічний інтерфейс вікна залежно від типу вікна та конфігурації
     * @param config JSON-об'єкт з налаштуваннями для вікна
     */
    private void initializeUI(JSONObject config) {
        if (windowType == UIManager.WindowType.COMPUTER) {
            root.setPrefSize(800, 600);
            root.setMinSize(800, 600);
            root.setMaxSize(800, 600);
        } else if (windowType == UIManager.WindowType.VICTORY || windowType == UIManager.WindowType.GAME_OVER) {
            // Збільшуємо розмір для вікон кінця гри
            root.setPrefSize(450, 400);
            root.setMinSize(450, 400);
            root.setMaxSize(450, 400);
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

    /**
     * Обробляє натискання клавіш у вікні
     * @param event подія натискання клавіші
     */
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

    /**
     * Створює вміст вікна залежно від його типу
     * Викликає відповідний метод для формування контенту:
     * - для NOTE і PICTURE — створює нотатки або зображення,
     * - для COMPUTER — створює інтерфейс комп’ютера,
     * - для VICTORY і GAME_OVER — створює вміст кінця гри.
     */
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

    /**
     * Створює інтерфейс вмісту вікна типу COMPUTER
     * Додає панель браузера з кнопками керування, адресний рядок,
     * область відображення контенту, кнопки навігації, індикатор сторінки та кнопку закриття вікна.
     */
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

    /**
     * Додає кольорову кнопку до панелі браузера
     * @param parent Панель, до якої додається кнопка
     * @param x Горизонтальна позиція кнопки в панелі
     * @param y Вертикальна позиція кнопки в панелі
     * @param color Колір кнопки.
     */
    private void addBrowserButton(Pane parent, double x, double y, Color color) {
        Pane button = new Pane();
        button.setBackground(new Background(new BackgroundFill(
                color, new CornerRadii(10), Insets.EMPTY)));
        button.setPrefSize(20, 20);
        button.setLayoutX(x);
        button.setLayoutY(y);
        parent.getChildren().add(button);
    }

    /**
     * Створює кнопки навігації "Назад" і "Вперед" для браузера
     * Кнопки розташовуються внизу вікна, мають стиль і обробники подій для переходу між сторінками
     * При наведенні кнопки змінюють колір, а при натисканні відтворюють звук і викликають навігацію
     */
    private void createNavigationButtons() {
        prevButton = new Button("◀ Назад");
        prevButton.setFont(FontManager.getInstance().getFont("Hardpixel", 14));
        prevButton.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        prevButton.setLayoutX(600);
        prevButton.setLayoutY(570);
        prevButton.setPrefSize(80, 25);

        prevButton.setOnAction(e -> {
            soundManager.playSound(SoundManager.SoundType.KEYBOARD_TYPING);
            navigatePage(-1);
        });
        prevButton.setOnMouseEntered(e -> {
                prevButton.setStyle("-fx-background-color: #5a6578; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");});

        prevButton.setOnMouseExited(e -> {
                prevButton.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");});

        nextButton = new Button("Вперед ▶");
        nextButton.setFont(FontManager.getInstance().getFont("Hardpixel", 14));
        nextButton.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        nextButton.setLayoutX(690);
        nextButton.setLayoutY(570);
        nextButton.setPrefSize(80, 25);

        nextButton.setOnAction(e -> {
            soundManager.playSound(SoundManager.SoundType.KEYBOARD_TYPING);
            navigatePage(1);
        });
        nextButton.setOnMouseEntered(e -> {
                nextButton.setStyle("-fx-background-color: #5a6578; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");});
        nextButton.setOnMouseExited(e -> {
                nextButton.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");});

        root.getChildren().addAll(prevButton, nextButton);
    }

    /**
     * Створює кнопку закриття вікна комп'ютера
     */
    private void createComputerCloseButton() {
        Button closeButton = new Button("✖");
        closeButton.setFont(FontManager.getInstance().getFont("Hardpixel", 18));
        closeButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3;");
        closeButton.setLayoutX(750);
        closeButton.setLayoutY(10);
        closeButton.setPrefSize(30, 30);

        closeButton.setOnAction(e -> {
            soundManager.playSound(SoundManager.SoundType.KEYBOARD_TYPING);
            closeWindow();
        });
        closeButton.setOnMouseEntered(e -> {
                closeButton.setStyle("-fx-background-color: #ff6666; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3;");});
        closeButton.setOnMouseExited(e -> {
                soundManager.playSound(SoundManager.SoundType.KEYBOARD_TYPING);
                closeButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3;");});

        root.getChildren().add(closeButton);
    }

    /**
     * Змінює поточну сторінку браузера на основі напрямку навігації
     * @param direction Напрямок навігації: -1 для попередньої сторінки, +1 для наступної
     */
    private void navigatePage(int direction) {
        int newPage = currentPage + direction;
        if (newPage >= 0 && newPage < totalPages) {
            currentPage = newPage;
            updateBrowserPage();
        }
    }

    /**
     * Оновлює контент браузера залежно від поточної сторінки, оновлює індикатор сторінки та активність кнопок навігації
     * Відповідає за візуальне відображення сторінок і керування кнопками "Назад" та "Вперед"
     */
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

    /**
     * Створює контент сторінки з імітацією пошукової сторінки Google
     * Елементи додаються у контейнер browserContentArea
     */
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

    /**
     * Створює інтерфейс сторінки "Vault Code" — банківської системи безпеки
     * Всі елементи додаються до browserContentArea
     * Код сховища також зберігається в GameManager для подальшого використання
     */
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

    /**
     * Створює вміст нотатки з фоновим зображенням і відображенням коду сховища
     */
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

    /**
     * Створює вікно кінця гри з інтерфейсом для перемоги або поразки
     * Відображає відповідні повідомлення, кнопки та запускає звуки
     */
    private void createEndGameContent() {
        boolean isVictory = (windowType == UIManager.WindowType.VICTORY);

        // Встановлюємо градієнтний фон як в меню
        if (isVictory) {
            Stop[] stops = {
                    new Stop(0, Color.web("#3C2F2F")),
                    new Stop(0.3, Color.web("#2A2525")),
                    new Stop(0.7, Color.web("#3C2F2F")),
                    new Stop(1, Color.web("#1E1A1A"))
            };
            LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, null, stops);
            root.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        } else {
            // Для поразки залишаємо темний фон
            root.setBackground(new Background(new BackgroundFill(
                    Color.rgb(0, 0, 0, 0.9), CornerRadii.EMPTY, Insets.EMPTY)));
        }

        if (isVictory) {
            soundManager.playSound(SoundManager.SoundType.VICTORY_GAME);
            // Заголовок перемоги
            Label title = new Label("🎉 МІСІЯ ВИКОНАНА! 🎉");
            title.setFont(FontManager.getInstance().getFont("Hardpixel", 32));
            title.setStyle("-fx-font-weight: bold;");
            title.setTextFill(Color.web("#EAD9C2"));

            // Додаємо тінь до заголовка
            DropShadow titleShadow = new DropShadow();
            titleShadow.setColor(Color.web("#8B5A2B"));
            titleShadow.setOffsetX(3);
            titleShadow.setOffsetY(3);
            titleShadow.setRadius(8);
            title.setEffect(titleShadow);

            title.setLayoutX(70);
            title.setLayoutY(50);
            root.getChildren().add(title);

            // Котяча іконка
            Label catIcon = new Label("🐾");
            catIcon.setFont(FontManager.getInstance().getFont("Hardpixel", 60));
            catIcon.setTextFill(Color.web("#D4A76A"));
            catIcon.setLayoutX(200);
            catIcon.setLayoutY(110);
            root.getChildren().add(catIcon);

            // Повідомлення про перемогу
            String message = config.optString("message", "Відмінна робота! Ви успішно завершили місію!");
            Label messageLabel = new Label(message);
            messageLabel.setFont(FontManager.getInstance().getFont("Hardpixel", 16));
            messageLabel.setTextFill(Color.web("#D4A76A"));
            messageLabel.setLayoutX(75);
            messageLabel.setLayoutY(200);
            messageLabel.setWrapText(true);
            messageLabel.setPrefWidth(300);
            messageLabel.setTextAlignment(TextAlignment.CENTER);
            messageLabel.setAlignment(Pos.CENTER);
            root.getChildren().add(messageLabel);

            // Кнопка "Продовжити" (наступний рівень)
            Button continueButton = createStyledButton("ПРОДОВЖИТИ", Color.web("#4A7043"));
            continueButton.setLayoutX(115);
            continueButton.setLayoutY(260);
            continueButton.setOnAction(e -> {
                // Переходимо до наступного рівня
                int currentLevel = GameManager.getInstance().getCurrentLevelId();
                int nextLevel = currentLevel + 1;

                // Перевіряємо чи існує наступний рівень (максимум 3 рівні)
                if (nextLevel <= 3) {
                    closeWindow();
                    soundManager.stopSoundEffects();
                    GameManager.getInstance().addMoney(GameManager.getInstance().getTemporaryMoney());
                    GameManager.getInstance().saveProgress();
                    GameManager.getInstance().saveGame();
                    GameManager.getInstance().completeLevel(currentLevel);
                    GameManager.getInstance().loadLevel(nextLevel, true);
                } else {
                    GameManager.getInstance().completeLevel(currentLevel);
                    showAllLevelsCompleted();
                }
            });
            root.getChildren().add(continueButton);

            // Кнопка "Вийти в меню"
            Button menuButton = createStyledButton("В МЕНЮ", Color.web("#7B3F3F"));
            menuButton.setLayoutX(115);
            menuButton.setLayoutY(320);
            menuButton.setOnAction(e -> {
                closeWindow();
                GameManager.getInstance().addMoney(GameManager.getInstance().getTemporaryMoney());
                GameManager.getInstance().saveProgress();
                GameManager.getInstance().saveGame();
                GameManager.getInstance().stopGameAndGoToMenu();
                UIManager.getInstance().hideMenuButton();
            });
            root.getChildren().add(menuButton);

        } else {
            soundManager.playSound(SoundManager.SoundType.FAIL_GAME);
            GameManager.getInstance().gameOver();
            // Контент для поразки
            Label title = new Label("💀 МІСІЯ ПРОВАЛЕНА 💀");
            title.setFont(FontManager.getInstance().getFont("Hardpixel", 28));
            title.setStyle("-fx-font-weight: bold;");
            title.setTextFill(Color.RED);
            title.setLayoutX(80);
            title.setLayoutY(60);
            root.getChildren().add(title);

            String message = config.optString("message", "Не засмучуйтесь! Спробуйте ще раз!");
            Label messageLabel = new Label(message);
            messageLabel.setFont(FontManager.getInstance().getFont("Hardpixel", 16));
            messageLabel.setTextFill(Color.WHITE);
            messageLabel.setLayoutX(75);
            messageLabel.setLayoutY(140);
            messageLabel.setWrapText(true);
            messageLabel.setPrefWidth(300);
            root.getChildren().add(messageLabel);

            // Кнопка "Спробувати знову"
            Button retryButton = createStyledButton("СПРОБУВАТИ ЗНОВУ", Color.web("#4A7043"));
            retryButton.setLayoutX(115);
            retryButton.setLayoutY(210);
            retryButton.setOnAction(e -> {
                closeWindow();
                soundManager.stopSoundEffects();
                GameManager.getInstance().restartCurrentLevel();
            });
            root.getChildren().add(retryButton);

            // Кнопка "Вийти в меню"
            Button menuButton = createStyledButton("В МЕНЮ", Color.web("#7B3F3F"));
            menuButton.setLayoutX(115);
            menuButton.setLayoutY(270);
            menuButton.setOnAction(e -> {
                closeWindow();
                GameManager.getInstance().saveProgress();
                GameManager.getInstance().saveGame();
                GameManager.getInstance().stopGameAndGoToMenu();
                UIManager.getInstance().hideMenuButton();

            });
            root.getChildren().add(menuButton);
        }
    }

    /**
     * Відображає повідомлення про проходження всіх рівнів та кнопку повернення в меню
     * Очищає попередній контент і налаштовує інтерфейс фінального екрану
     */
    private void showAllLevelsCompleted() {
        Label completedLabel = new Label("🏆 ВСІ РІВНІ ПРОЙДЕНО! 🏆");
        completedLabel.setFont(FontManager.getInstance().getFont("Hardpixel", 24));
        completedLabel.setStyle("-fx-font-weight: bold;");
        completedLabel.setTextFill(Color.GOLD);
        completedLabel.setLayoutX(50);
        completedLabel.setLayoutY(100);

        Label congratsLabel = new Label("Вітаємо! Ви майстер котячого грабежу!");
        congratsLabel.setFont(FontManager.getInstance().getFont("Hardpixel", 16));
        congratsLabel.setTextFill(Color.web("#D4A76A"));
        congratsLabel.setLayoutX(50);
        congratsLabel.setLayoutY(150);
        congratsLabel.setPrefWidth(300);
        congratsLabel.setWrapText(true);

        // Очищаємо попередній контент
        root.getChildren().clear();

        // Додаємо новий контент
        root.getChildren().addAll(completedLabel, congratsLabel);

        // Кнопка повернення в меню
        Button menuButton = createStyledButton("ПОВЕРНУТИСЯ В МЕНЮ", Color.web("#4A7043"));
        menuButton.setLayoutX(90);
        menuButton.setLayoutY(250);
        menuButton.setOnAction(e -> {
            closeWindow();
            soundManager.stopSoundEffects();
            GameManager.getInstance().addMoney(GameManager.getInstance().getTemporaryMoney());
            GameManager.getInstance().saveProgress();
            GameManager.getInstance().saveGame();
            GameManager.getInstance().stopGameAndGoToMenu();
            UIManager.getInstance().hideMenuButton();


        });
        root.getChildren().add(menuButton);
    }

    /**
     * Конвертує колір у шістнадцятковий рядок у форматі #RRGGBB
     * @param color Колір для конвертації
     * @return Шістнадцятковий рядок кольору
     */
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    /**
     * Створює стилізовану кнопку з заданим текстом і кольором рамки
     * @param text  Текст кнопки
     * @param color Колір рамки та фону при наведенні
     * @return Стилізована кнопка з ефектами наведення
     */
    private Button createStyledButton(String text, Color color) {
        Button button = new Button(text);
        button.setFont(FontManager.getInstance().getFont("Hardpixel", 16));
        button.setPrefSize(220, 40);

        String baseStyle = String.format(
                "-fx-background-color: #2A2525;" +
                        "-fx-text-fill: %s;" +
                        "-fx-border-color: %s;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-family: 'Hardpixel';" +
                        "-fx-font-size: 16px;",
                toHexString(Color.web("#EAD9C2")), toHexString(color)
        );

        String hoverTextColor = color.equals(Color.web("#4A7043")) ? "#1E1A1A" : "#EAD9C2";
        String hoverStyle = String.format(
                "-fx-background-color: %s;" +
                        "-fx-text-fill: %s;" +
                        "-fx-border-color: #D4A76A;" +
                        "-fx-border-width: 3px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-family: 'Hardpixel';" +
                        "-fx-font-size: 16px;",
                toHexString(color), hoverTextColor
        );

        button.setStyle(baseStyle);

        // Додаємо тінь до кнопки
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

    /**
     * Створює кнопку закриття вікна
     * Кнопка з'являється у верхньому правому куті та закриває вікно при натисканні
     */
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

    /**
     * Повертає кореневий вузол інтерфейсу
     * @return кореневий вузол (root) типу Node
     */
    public Node getUI() {
        return root;
    }

    /**
     * Закриває поточне вікно UI, прибираючи його з батьківського контейнера,
     * та відновлює стан гри до PLAYING. Також викликає метод UIManager для приховування UI
     */
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