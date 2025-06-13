package ui;

import entities.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.effect.DropShadow;
import managers.FontManager;
import managers.GameManager;

import java.util.ArrayList;
import java.util.List;

// Магазин для купів предметів, розширює UIWindow
public class Shop implements UIWindow {
    // Поля
    private List<ShopItem> items;
    private int playerMoney;
    private GridPane shopPane;
    private Button[] itemButtons;
    private Label moneyLabel;
    private Button backButton;
    private Player player;

    // Конструктор
    public Shop(Player player) {
        this.player = player;
        this.playerMoney = player.getMoney();
        initializeItems();
        createShopUI();
    }

    // Ініціалізація предметів магазину
    private void initializeItems() {
        items = new ArrayList<>();
        items.add(new ShopItem("Невидимість", 100, "Стаєш невидимим на 10 секунд", ShopItem.ItemType.BONUS, "UI/invisibility.png"));
        items.add(new ShopItem("Буст швидкості", 80, "Збільшує швидкість на 50% на 15 секунд", ShopItem.ItemType.BONUS, "UI/speedBoost.png"));
        items.add(new ShopItem("Універсальний ключ", 120, "Відчиняє будь-які двері", ShopItem.ItemType.KEY, "UI/key.png"));
        items.add(new ShopItem("Пістолет", 150, "Дозволяє стріляти по ворогах", ShopItem.ItemType.TOOL, "UI/gun.png"));
    }

    // Повертає предмети магазину (статичний метод)
    public List<ShopItem> getShopItems() {
        List<ShopItem> shopItems = new ArrayList<>();
        shopItems = items;
        return shopItems;
    }

    // Створює UI магазину
    public void createShopUI() {
        shopPane = new GridPane();
        shopPane.setAlignment(Pos.CENTER);
        shopPane.setHgap(20);
        shopPane.setVgap(10);
        shopPane.setPadding(new Insets(20));
        shopPane.setPrefSize(1280, 640);

        // Зістарений фон
        Stop[] stops = {
                new Stop(0, Color.web("#3C2F2F")),
                new Stop(0.5, Color.web("#2A2525")),
                new Stop(1, Color.web("#1E1A1A"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, null, stops);
        Background bg = new Background(new BackgroundFill(gradient, null, null));
        shopPane.setBackground(bg);

        // Заголовок магазину
        Label shopTitle = new Label("КРАМНИЦЯ КОТОГРАБІЖНИКА");
        shopTitle.setFont(FontManager.getInstance().getFont("Hardpixel", 42));
        shopTitle.setTextFill(Color.web("#EAD9C2"));
        DropShadow titleShadow = new DropShadow();
        titleShadow.setColor(Color.web("#8B5A2B"));
        titleShadow.setOffsetX(2);
        titleShadow.setOffsetY(2);
        titleShadow.setRadius(6);
        shopTitle.setEffect(titleShadow);

        // Гроші гравця
        moneyLabel = new Label("Гроші: " + playerMoney + " монет");
        moneyLabel.setFont(FontManager.getInstance().getFont("Hardpixel", 24));
        moneyLabel.setTextFill(Color.web("#D4A76A"));

        // Кнопка "Назад"
        backButton = createCuteButton("ПОВЕРНУТИСЯ ДО МЕНЮ", Color.web("#7B3F3F"));

        // Контейнер для заголовка та грошей
        VBox topContainer = new VBox(10, shopTitle, moneyLabel);
        topContainer.setAlignment(Pos.CENTER);
        GridPane.setMargin(topContainer, new Insets(0, 0, 30, 0));
        shopPane.add(topContainer, 0, 0, 2, 1);

        // Кнопки для предметів
        itemButtons = new Button[items.size()];
        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            Button button = createCuteButton("Купити за " + item.getPrice() + " монет", Color.web("#4A7043"));
            button.setUserData(item);

            // Зображення предмета
            ImageView imageView = new ImageView(new Image(item.getSpritePath()));
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
            DropShadow imageShadow = new DropShadow();
            imageShadow.setColor(Color.web("#8B5A2B"));
            imageShadow.setRadius(5);
            imageView.setEffect(imageShadow);

            // Опис предмета
            Label description = new Label(item.getName() + ": " + item.getDescription());
            description.setFont(FontManager.getInstance().getFont("Hardpixel", 16));
            description.setTextFill(Color.web("#EAD9C2"));
            description.setWrapText(true);
            description.setMaxWidth(200);

            VBox itemContainer = new VBox(10, imageView, description, button);
            itemContainer.setAlignment(Pos.CENTER);
            shopPane.add(itemContainer, i % 2, 1 + (i / 2));
            itemButtons[i] = button;
            button.setOnAction(e -> buyItem((ShopItem) button.getUserData()));
        }

        // Вирівнювання кнопки "Назад" через HBox
        HBox backButtonContainer = new HBox();
        backButtonContainer.setAlignment(Pos.CENTER);
        backButtonContainer.getChildren().add(backButton);
        GridPane.setMargin(backButtonContainer, new Insets(20, 0, 0, 0));
        shopPane.add(backButtonContainer, 0, 3, 2, 1);

        // Налаштування дії для кнопки "Назад"
        backButton.setOnAction(e -> hide());
    }

    // Створює стилізовану кнопку
    private Button createCuteButton(String text, Color color) {
        Button button = new Button(text);
        button.setFont(FontManager.getInstance().getFont("Hardpixel", 22));
        button.setPrefSize(220, 50);

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
        buttonShadow.setColor(Color.web("#8B5A2A2B"));
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

    // Обробляє ввід
    public void handleInput(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            hide();
        }
    }

    // Купує предмет
    public void buyItem(ShopItem item) {
        if (player.buyItem(item)) {
            updateMoney(player.getMoney());
        }
    }

    // Оновлює відображення грошей
    public void updateMoney(int newAmount) {
        playerMoney = newAmount;
        moneyLabel.setText("Гроші: " + playerMoney + " монет");
    }

    @Override
    public void show() {
        if (shopPane != null) {
            shopPane.setVisible(true);
            shopPane.setFocusTraversable(true);
            shopPane.requestFocus();
            shopPane.setOnKeyPressed(this::handleInput);
            GameManager.getInstance().setGameState(GameManager.GameState.PAUSED);
            updateMoney(player.getMoney());
        }
    }

    @Override
    public void hide() {
        if (shopPane != null) {
            shopPane.setVisible(false);
        }
        GameManager.getInstance().setGameState(GameManager.GameState.MENU);
    }

    @Override
    public Node getRoot() {
        return shopPane;
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