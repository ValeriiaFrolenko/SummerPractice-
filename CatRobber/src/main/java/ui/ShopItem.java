package ui;

// Представляє предмет у магазині
public class ShopItem {
    // Поля
    private final String name; // Назва предмета
    private final int price; // Ціна предмета
    private final String description; // Опис предмета
    private final ItemType itemType; // Тип предмета
    private final String spritePath; // Шлях до зображення (/assets/images/)

    // Енум для типів предметів
    public enum ItemType { KEY, GUN, SPEED_BOOST, INVISIBILITY }

    // Конструктор
    // Ініціалізує предмет
    public ShopItem(String name, int price, String description, ItemType itemType, String spritePath) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Назва предмета не може бути null або порожньою");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Ціна предмета не може бути від'ємною");
        }
        if (description == null) {
            throw new IllegalArgumentException("Опис предмета не може бути null");
        }
        if (itemType == null) {
            throw new IllegalArgumentException("Тип предмета не може бути null");
        }
        if (spritePath == null || spritePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Шлях до зображення не може бути null або порожнім");
        }

        this.name = name.trim();
        this.price = price;
        this.description = description;
        this.itemType = itemType;
        this.spritePath = spritePath.trim();
    }

    // Повертає назву предмета
    // Використовується в Shop.createShopUI
    public String getName() {
        return name;
    }

    // Повертає ціну предмета
    // Використовується в Shop.buyItem
    public int getPrice() {
        return price;
    }

    // Повертає опис предмета
    // Використовується в Shop.createShopUI
    public String getDescription() {
        return description;
    }

    // Повертає тип предмета
    // Використовується в Player.inventory
    public ItemType getItemType() {
        return itemType;
    }

    // Повертає шлях до зображення
    // Використовується в Shop.createShopUI
    public String getSpritePath() {
        return spritePath;
    }
}
