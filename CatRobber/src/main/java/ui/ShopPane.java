package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.effect.DropShadow;
import managers.FontManager;
import managers.GameManager;
import managers.SoundManager;
import managers.UIManager;
import utils.GameLoader;

import java.util.ArrayList;
import java.util.List;

public class ShopPane implements UIWindow {
    private StackPane rootPane;
    private GridPane shopPane;
    private static List<ShopItem> items;
    private int money;
    private Button[] itemButtons;
    private Label moneyLabel;
    private Button backButton;
    private GameLoader gameLoader;

    public ShopPane() {
        gameLoader = new GameLoader();
        this.money = GameManager.getInstance().getTotalMoney();
        initializeItems();
        createShopUI();
        createRootPane();
    }

    public static List<ShopItem> getItems() {
        if (items == null) {
            ShopPane shop = new ShopPane();
            shop.initializeItems();
        }
        return new ArrayList<>(items);
    }

    private void initializeItems() {
        items = new ArrayList<>();
        items.add(new ShopItem("Невидимість", 100, "Стаєш невидимим на 10 секунд.\nЗастосовується автоматично після купівлі.", ShopItem.ItemType.INVISIBILITY, "UI/invisibility.png"));
        items.add(new ShopItem("Буст швидкості", 80, "Збільшує швидкість на 50% на 15 секунд.\nЗастосовується автоматично після купівлі.", ShopItem.ItemType.SPEED_BOOST, "UI/speedBoost.png"));
        items.add(new ShopItem("Універсальний ключ", 120, "Відчиняє будь-які двері.\nЗастосовується до перших дверей,\nзакритих звичайним замком.", ShopItem.ItemType.KEY, "UI/key.png"));
        items.add(new ShopItem("Пістолет", 150, "Дозволяє стріляти по ворогах.\nВикористовується кнопкою F.", ShopItem.ItemType.GUN, "UI/gun.png"));
    }

    private void createShopUI() {
        shopPane = new GridPane();
        shopPane.setAlignment(Pos.CENTER);
        shopPane.setPadding(new Insets(15));
        shopPane.setPrefSize(1280, 640);

        Stop[] stops = {
                new Stop(0, Color.web("#3C2F2F")),
                new Stop(0.5, Color.web("#2A2525")),
                new Stop(1, Color.web("#1E1A1A"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, null, stops);
        Background bg = new Background(new BackgroundFill(gradient, null, null));
        shopPane.setBackground(bg);

        Label shopTitle = new Label("КРАМНИЦЯ КОТОГРАБІЖНИКА");
        shopTitle.setFont(FontManager.getInstance().getFont("Hardpixel", 32));
        shopTitle.setTextFill(Color.web("#EAD9C2"));
        DropShadow titleShadow = new DropShadow();
        titleShadow.setColor(Color.web("#8B5A2B"));
        titleShadow.setOffsetX(2);
        titleShadow.setOffsetY(2);
        titleShadow.setRadius(6);
        shopTitle.setEffect(titleShadow);

        moneyLabel = new Label("Гроші: " + money + " монет");
        moneyLabel.setFont(FontManager.getInstance().getFont("Hardpixel", 20));
        moneyLabel.setTextFill(Color.web("#D4A76A"));

        VBox topContainer = new VBox(5, shopTitle, moneyLabel);
        topContainer.setAlignment(Pos.CENTER);
        shopPane.add(topContainer, 0, 0, 4, 1);
        GridPane.setMargin(topContainer, new Insets(0, 0, 15, 0));

        // Створюємо горизонтальний контейнер для всіх товарів
        HBox itemsContainer = new HBox(15);
        itemsContainer.setAlignment(Pos.CENTER);
        itemsContainer.setPrefWidth(1250);

        itemButtons = new Button[items.size()];
        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            Button button = createCuteButton("Купити за " + item.getPrice(), Color.web("#4A7043"));
            button.setUserData(item);

            ImageView imageView = new ImageView(gameLoader.loadImage(item.getSpritePath()));
            imageView.setFitWidth(70);
            imageView.setFitHeight(70);
            DropShadow imageShadow = new DropShadow();
            imageShadow.setColor(Color.web("#8B5A2B"));
            imageShadow.setRadius(5);
            imageView.setEffect(imageShadow);

            Label itemName = new Label(item.getName());
            itemName.setFont(FontManager.getInstance().getFont("Hardpixel", 16));
            itemName.setTextFill(Color.web("#D4A76A"));
            itemName.setWrapText(true);
            itemName.setMaxWidth(280);
            itemName.setAlignment(Pos.CENTER);

            Label description = new Label(item.getDescription());
            description.setFont(FontManager.getInstance().getFont("Hardpixel", 12));
            description.setTextFill(Color.web("#EAD9C2"));
            description.setWrapText(true);
            description.setMaxWidth(280);
            description.setAlignment(Pos.CENTER);
            description.setPrefHeight(75);

            VBox itemContainer = new VBox(6, imageView, itemName, description, button);
            itemContainer.setAlignment(Pos.CENTER);
            itemContainer.setPrefWidth(285);
            itemContainer.setPrefHeight(320);

            // Додаємо фон для кожного товару
            itemContainer.setStyle("-fx-background-color: rgba(42, 37, 37, 0.8); " +
                    "-fx-background-radius: 12; " +
                    "-fx-border-color: #8B5A2B; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 12;");
            itemContainer.setPadding(new Insets(12));

            itemsContainer.getChildren().add(itemContainer);
            itemButtons[i] = button;
            button.setOnAction(e -> {
                SoundManager.getInstance().playSound(SoundManager.SoundType.BUTTON_CLICK);
                buyItem((ShopItem) button.getUserData());
            });
        }

        shopPane.add(itemsContainer, 0, 1, 4, 1);
        GridPane.setMargin(itemsContainer, new Insets(10, 0, 15, 0));

        backButton = createCuteButton("ПОВЕРНУТИСЯ ДО МЕНЮ", Color.web("#7B3F3F"));
        HBox backButtonContainer = new HBox();
        backButtonContainer.setAlignment(Pos.CENTER);
        backButtonContainer.getChildren().add(backButton);
        shopPane.add(backButtonContainer, 0, 2, 4, 1);

        backButton.setOnAction(e -> {
            System.out.println("Back button clicked");
            SoundManager.getInstance().playSound(SoundManager.SoundType.BUTTON_CLICK);

            // Ховаємо магазин
            hide();

            // Повертаємося до меню через UIManager
            UIManager.getInstance().setCurrentWindow(null);
            UIManager.getInstance().showMenu();
        });
    }

    private void createRootPane() {
        rootPane = new StackPane();
        rootPane.setPrefSize(1280, 640);
        rootPane.getChildren().add(shopPane);
        rootPane.setFocusTraversable(true);
        rootPane.setOnKeyPressed(this::handleInput);
    }

    private Button createCuteButton(String text, Color color) {
        Button button = new Button(text);
        button.setFont(FontManager.getInstance().getFont("Hardpixel", 14));
        button.setPrefSize(250, 35);

        String baseStyle = String.format(
                "-fx-background-color: #2A2525;" +
                        "-fx-text-fill: %s;" +
                        "-fx-border-color: %s;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-background-radius: 12px;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-family: 'Hardpixel';" +
                        "-fx-font-size: 14px;",
                toHexString(Color.web("#EAD9C2")), toHexString(color)
        );

        String hoverTextColor = color.equals(Color.web("#4A7043")) ? "#1E1A1A" : "#EAD9C2";
        String hoverStyle = String.format(
                "-fx-background-color: %s;" +
                        "-fx-text-fill: %s;" +
                        "-fx-border-color: #D4A76A;" +
                        "-fx-border-width: 3px;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-background-radius: 12px;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-family: 'Hardpixel';" +
                        "-fx-font-size: 14px;",
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

    private void handleInput(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            UIManager.getInstance().hideCurrentWindowToMenu();
            event.consume();
        }
    }

    private void buyItem(ShopItem item) {
        if (GameManager.getInstance().buyItem(item)) {
            updateMoney(GameManager.getInstance().getTotalMoney());
        }
    }

    private void updateMoney(int newAmount) {
        money = newAmount;
        moneyLabel.setText("Гроші: " + money + " монет");
    }

    @Override
    public void show() {
        // Обов'язково відновлюємо всі властивості
        rootPane.setVisible(true);
        rootPane.setMouseTransparent(false); // ❗ ВАЖЛИВО!
        rootPane.setFocusTraversable(true);

        // Відновлюємо обробник клавіш
        rootPane.setOnKeyPressed(this::handleInput);

        // Оновлюємо гроші
        updateMoney(GameManager.getInstance().getTotalMoney());

        // Запитуємо фокус через Platform.runLater
        javafx.application.Platform.runLater(() -> {
            rootPane.requestFocus();
        });
    }

    @Override
    public void hide() {
        // НЕ очищуємо children - це руйнує кнопки!
        rootPane.setVisible(false);
        rootPane.setMouseTransparent(true);

        // Очищуємо тільки обробник подій
        rootPane.setOnKeyPressed(null);

        // НЕ робимо clear() - це головна проблема:
        // rootPane.getChildren().clear(); // ❌ НЕ РОБИТИ!
        // shopPane.getChildren().clear(); // ❌ НЕ РОБИТИ!
    }



    @Override
    public Node getRoot() {
        return rootPane;
    }

    private String toHexString(Color color) {
        return String.format(
                "#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
        );
    }
}