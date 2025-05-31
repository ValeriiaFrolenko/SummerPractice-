package ui;


// Представляє предмет у магазині
public class ShopItem {
    // Поля
    private String name; // Назва предмета
    private int price; // Ціна предмета
    private String description; // Опис предмета
    private ItemType itemType; // Тип предмета
    private String spritePath; // Шлях до зображення (/assets/images/)

    // Енум для типів предметів
    public enum ItemType { KEY, TOOL, BONUS }

    // Конструктор
    // Ініціалізує предмет
    public ShopItem(String name, int price, String description, ItemType itemType, String spritePath) {}

    // Повертає назву предмета
    // Використовується в Shop.createShopUI
    public String getName() { return null; }

    // Повертає ціну предмета
    // Використовується в Shop.buyItem
    public int getPrice() { return 0; }

    // Повертає опис предмета
    // Використовується в Shop.createShopUI
    public String getDescription() { return null; }

    // Повертає тип предмета
    // Використовується в Player.inventory
    public ItemType getItemType() { return null; }

    // Повертає шлях до зображення
    // Використовується в Shop.createShopUI
    public String getSpritePath() { return null; }

}