package ui;

import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import entities.Player;
import java.util.List;

// Магазин для купівлі предметів, розширює UIWindow
public class Shop implements UIWindow {
    // Поля
    private List<ShopItem> items; // Список предметів у магазині
    private int playerMoney; // Гроші гравця
    private GridPane shopPane; // Контейнер для UI
    private Button[] itemButtons; // Кнопки для предметів
    private Label moneyLabel; // Відображення грошей
    private Button buyButton; // Кнопка "Купити"

    // Конструктор
    // Отримує playerMoney від Player
    public Shop(int playerMoney) {}

    // Обробляє ввід
    // Отримує KeyEvent від UIManager
    public void handleInput(KeyEvent event) {}

    // Купує предмет
    // Додає предмет у Player.inventory, зменшує playerMoney
    public void buyItem(ShopItem item) {}

    // Оновлює гроші
    // Отримує нову суму від Player
    public void updateMoney(int newAmount) {}

    // Створює UI магазину
    // Налаштовує shopPane, додає компоненти
    public void createShopUI() {}

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public Node getRoot() {
        return null;
    }
}