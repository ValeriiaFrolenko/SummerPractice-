package ui;

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
    private int playerMoney;
    private Button[] itemButtons;
    private Label moneyLabel;
    private Button backButton;
    private GameLoader gameLoader;

    public ShopPane() {
        gameLoader = new GameLoader();
        this.playerMoney = GameManager.getInstance().getTotalMoney();
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
        items.add(new ShopItem("Невидимість", 100, "Стаєш невидимим на 10 секунд", ShopItem.ItemType.INVISIBILITY, "UI/invisibility.png"));
        items.add(new ShopItem("Буст швидкості", 80, "Збільшує швидкість на 50% на 15 секунд", ShopItem.ItemType.SPEED_BOOST, "UI/speedBoost.png"));
        items.add(new ShopItem("Універсальний ключ", 120, "Відчиняє будь-які двері", ShopItem.ItemType.KEY, "UI/key.png"));
        items.add(new ShopItem("Пістолет", 150, "Дозволяє стріляти по ворогах", ShopItem.ItemType.GUN, "UI/gun.png"));
    }

    private void createShopUI() {
        shopPane = new GridPane();
        shopPane.setAlignment(Pos.CENTER);
        shopPane.setHgap(20);
        shopPane.setVgap(10);
        shopPane.setPadding(new Insets(20));
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
        shopTitle.setFont(FontManager.getInstance().getFont("Hardpixel", 42));
        shopTitle.setTextFill(Color.web("#EAD9C2"));
        DropShadow titleShadow = new DropShadow();
        titleShadow.setColor(Color.web("#8B5A2B"));
        titleShadow.setOffsetX(2);
        titleShadow.setOffsetY(2);
        titleShadow.setRadius(6);
        shopTitle.setEffect(titleShadow);

        moneyLabel = new Label("Гроші: " + playerMoney + " монет");
        moneyLabel.setFont(FontManager.getInstance().getFont("Hardpixel", 24));
        moneyLabel.setTextFill(Color.web("#D4A76A"));

        backButton = createCuteButton("ПОВЕРНУТИСЯ ДО МЕНЮ", Color.web("#7B3F3F"));

        VBox topContainer = new VBox(10, shopTitle, moneyLabel);
        topContainer.setAlignment(Pos.CENTER);
        GridPane.setMargin(topContainer, new Insets(0, 0, 30, 0));
        shopPane.add(topContainer, 0, 0, 2, 1);

        itemButtons = new Button[items.size()];
        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            Button button = createCuteButton("Купити за " + item.getPrice() + " монет", Color.web("#4A7043"));
            button.setUserData(item);

            ImageView imageView = new ImageView(gameLoader.loadImage(item.getSpritePath()));
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
            DropShadow imageShadow = new DropShadow();
            imageShadow.setColor(Color.web("#8B5A2B"));
            imageShadow.setRadius(5);
            imageView.setEffect(imageShadow);

            Label description = new Label(item.getName() + ": " + item.getDescription());
            description.setFont(FontManager.getInstance().getFont("Hardpixel", 16));
            description.setTextFill(Color.web("#EAD9C2"));
            description.setWrapText(true);
            description.setMaxWidth(200);

            VBox itemContainer = new VBox(10, imageView, description, button);
            itemContainer.setAlignment(Pos.CENTER);
            shopPane.add(itemContainer, i % 2, 1 + (i / 2));
            itemButtons[i] = button;
            button.setOnAction(e -> {
                SoundManager.getInstance().playSound(SoundManager.SoundType.BUTTON_CLICK);
                buyItem((ShopItem) button.getUserData());
            });
        }

        HBox backButtonContainer = new HBox();
        backButtonContainer.setAlignment(Pos.CENTER);
        backButtonContainer.getChildren().add(backButton);
        GridPane.setMargin(backButtonContainer, new Insets(20, 0, 0, 0));
        shopPane.add(backButtonContainer, 0, 3, 2, 1);

        backButton.setOnAction(e -> {
            hide();
            SoundManager.getInstance().playSound(SoundManager.SoundType.BUTTON_CLICK);
            UIManager.getInstance().setCurrentWindow(null); // Явно очищаємо currentWindow
            UIManager.getInstance().hideCurrentWindowToMenu();
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
        playerMoney = newAmount;
        moneyLabel.setText("Гроші: " + playerMoney + " монет");
    }

    @Override
    public void show() {
        rootPane.setVisible(true);
        rootPane.setFocusTraversable(true);
        updateMoney(GameManager.getInstance().getTotalMoney());

        javafx.application.Platform.runLater(() -> {
            rootPane.requestFocus();
        });
    }
    @Override
    public void hide() {
        rootPane.setVisible(false);
        rootPane.setMouseTransparent(true);
        rootPane.setOnKeyPressed(null);
        rootPane.getChildren().clear();
        shopPane.getChildren().clear();
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