package entities;

import interfaces.*;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.json.JSONObject;
import utils.GameLoader;
import utils.Vector2D;
import java.util.HashMap;
import java.util.Map;

// Представляє двері, які можуть бути відкриті, закриті, замкнені або вести до іншої кімнати/поверху
public class Door implements GameObject, Interactable {
    // Поля
    private Vector2D position; // Позиція дверей, з JSON
    private boolean isOpen; // Чи відкриті двері, з JSON
    private boolean isLocked; // Чи замкнені двері, з JSON
    private boolean isLaser; // Чи лазерні двері, з JSON
    private boolean isRoomLink; // Чи ведуть до іншої кімнати, з JSON
    private boolean isFloorLink; // Чи ведуть до іншого поверху, з JSON
    private String direction; // Напрям дверей ("left", "right", "up", "down"), з JSON
    private Map<String, Image> sprites; // Спрайти для різних станів, завантажені через GameLoader
    private String[] spritePaths; // Шляхи до спрайтів
    private final String path = "background/doors/"; // Базовий шлях до спрайтів

    // Конструктор: ініціалізує двері з JSON-даними
    public Door(Vector2D vector2D, JSONObject defaultData) {
        this.position = vector2D;
        this.isOpen = defaultData.optBoolean("isOpen", false);
        this.isLocked = defaultData.optBoolean("isLocked", false);
        this.isLaser = defaultData.optBoolean("isLaser", false);
        this.isRoomLink = defaultData.optBoolean("isRoomLink", false);
        this.isFloorLink = defaultData.optBoolean("isFloorLink", false);
        this.direction = defaultData.optString("direction", "right");
        this.spritePaths = new String[]{
                path + "close.png", path + "closeLeft.png", path + "closeRight.png",
                path + "leftLock.png", path + "openLeft.png", path + "openRight.png",
                path + "rightLock.png", path + "stairsUp.png", path + "stairsDown.png",
                path + "withLock.png"
        };
        GameLoader loader = new GameLoader();
        this.sprites = new HashMap<>();
        sprites.put("stairsClosed", loader.loadImage(spritePaths[0]));
        sprites.put("closedLeft", loader.loadImage(spritePaths[1]));
        sprites.put("closedRight", loader.loadImage(spritePaths[2]));
        sprites.put("lockedLeft", loader.loadImage(spritePaths[3]));
        sprites.put("openLeft", loader.loadImage(spritePaths[4]));
        sprites.put("openRight", loader.loadImage(spritePaths[5]));
        sprites.put("lockedRight", loader.loadImage(spritePaths[6]));
        sprites.put("stairsUp", loader.loadImage(spritePaths[7]));
        sprites.put("stairsDown", loader.loadImage(spritePaths[8]));
        sprites.put("stairsLocked", loader.loadImage(spritePaths[9]));
    }

    // --- Ініціалізація та оновлення ---

    // Взаємодія з гравцем (викликається з GameManager.checkInteractions())
    @Override
    public void interact(Player player) {
        // TODO: Реалізувати взаємодію
        // Якщо !isLocked, змінити isOpen, можливо, викликати player.teleportToRoom() або teleportToFloor()
    }

    // Перевіряє, чи можлива взаємодія
    @Override
    public boolean canInteract(Player player) {
        // TODO: Реалізувати перевірку
        // Перевірити відстань до гравця через getInteractionRange()
        return false;
    }

    // --- Рендеринг ---

    // Рендерить двері на canvas (викликається з GameManager.render())
    @Override
    public void render(GraphicsContext gc) {
        Image sprite = null;
        if (isFloorLink) {
            if (isLocked) {
                sprite = sprites.get("stairsLocked");
            } else {
                if (isOpen) {
                    if (direction.equals("up")) {
                        sprite = sprites.get("stairsUp");
                    } else if (direction.equals("down")) {
                        sprite = sprites.get("stairsDown");
                    }
                } else {
                    sprite = sprites.get("stairsClosed");
                }
            }
        } else {
            if (isLocked) {
                if (direction.equals("left")) {
                    sprite = sprites.get("lockedLeft");
                } else if (direction.equals("right")) {
                    sprite = sprites.get("lockedRight");
                }
            } else {
                if (isOpen) {
                    if (direction.equals("left")) {
                        sprite = sprites.get("openLeft");
                    } else if (direction.equals("right")) {
                        sprite = sprites.get("openRight");
                    }
                } else {
                    if (direction.equals("left")) {
                        sprite = sprites.get("closedLeft");
                    } else if (direction.equals("right")) {
                        sprite = sprites.get("closedRight");
                    }
                }
            }
        }
        if (sprite != null) {
            // Малюємо спрайт за позицією
        }
    }

    // --- Серіалізація ---

    // Повертає JSON для збереження (викликається з SaveManager.saveInteractables())
    @Override
    public JSONObject getSerializableData() {
        // TODO: Реалізувати серіалізацію
        // Створити JSONObject з position, isOpen, isLocked, direction тощо
        return null;
    }

    // Відновлює стан із JSON (викликається з SaveManager.loadGame())
    @Override
    public void setFromData(JSONObject data) {
        // TODO: Реалізувати десеріалізацію
        // Оновити position, isOpen, isLocked, direction із data
    }

    // --- Геттери/Сеттери ---

    // Повертає тип об’єкта
    @Override
    public String getType() {
        return "Door";
    }

    // Повертає позицію дверей
    @Override
    public Vector2D getPosition() {
        return new Vector2D(position.x, position.y);
    }

    // Повертає уявну позицію
    @Override
    public Vector2D getImaginePosition() {
        return getPosition();
    }

    // Встановлює позицію дверей
    @Override
    public void setPosition(Vector2D position) {
        this.position = position;
    }

    // Встановлює уявну позицію
    @Override
    public void setImaginePosition(Vector2D position) {
        this.position = position;
    }

    // Повертає межі для колізій
    @Override
    public Bounds getBounds() {
        return new BoundingBox(position.x, position.y, 32, 32); // Фіксовані розміри
    }

    // Повертає межі для рендерингу
    @Override
    public Bounds getImagineBounds() {
        return getBounds();
    }

    // Повертає діапазон взаємодії
    @Override
    public double getInteractionRange() {
        return 50.0; // Фіксована відстань взаємодії
    }

    // Повертає підказку для UI
    @Override
    public String getInteractionPrompt() {
        return isLocked ? "Locked Door" : "Open/Close Door";
    }

    // Повертає шар рендерингу
    @Override
    public int getRenderLayer() {
        return 1; // Двері рендеряться на шарі 1
    }

    // Перевіряє видимість
    @Override
    public boolean isVisible() {
        return true; // Двері завжди видимі
    }

    // Повертає стан відкритості
    public boolean isOpen() {
        return isOpen;
    }
}