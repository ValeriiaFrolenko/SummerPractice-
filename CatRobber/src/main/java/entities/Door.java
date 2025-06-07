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
    private double imageX; // Верхній лівий кут зображення по X
    private double imageY; // Верхній лівий кут зображення по Y
    private double imageWidth; // Ширина зображення, з JSON
    private double imageHeight; // Висота зображення, з JSON
    private boolean isOpen; // Чи відкриті двері, з JSON
    private boolean isLocked; // Чи замкнені двері, з JSON
    private boolean isLaser; // Чи лазерні двері, з JSON
    private boolean isRoomLink; // Чи ведуть до іншої кімнати, з JSON
    private boolean isFloorLink; // Чи ведуть до іншого поверху, з JSON
    private String direction; // Напрям дверей ("left", "right", "up", "down"), з JSON
    private Map<String, Image> sprites; // Спрайти для різних станів, завантажені через GameLoader
    private String[] spritePaths; // Шляхи до спрайтів
    private final String path = "background/doors/"; // Базовий шлях до спрайтів
    private int sharedId; // Унікальний ID для зв’язку між дверима

    // Конструктор: ініціалізує двері з JSON-даними
    public Door(Vector2D vector2D, JSONObject defaultData) {
        double jsonImageX = vector2D.getX();
        double jsonImageY = vector2D.getY();
        this.imageWidth = defaultData.getDouble("width");
        this.imageHeight = defaultData.getDouble("height");
        this.imageX = jsonImageX;
        this.imageY = jsonImageY - imageHeight;
        this.sharedId = defaultData.getInt("sharedId");
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
        if (isLocked) {
            return;
        } else {
            if (isOpen) {
                Player.Direction direction = player.getDirection();
                if ((direction.equals(Player.Direction.LEFT) || direction.equals(Player.Direction.RIGHT)) && isRoomLink) {
                    player.teleportToRoom(this);
                } else {
                    player.teleportToFloor(this);
                }
            } else {
                return;
            }
        }
    }

    // Перевіряє, чи можлива взаємодія
    @Override
    public boolean canInteract(Player player) {
        Bounds playerBounds = player.getBounds();
        Bounds doorBounds = this.getBounds();
        double offset = 10;
        Bounds newBounds = new BoundingBox(playerBounds.getMinX(), playerBounds.getMinY(), playerBounds.getWidth() + offset, playerBounds.getHeight());
        // Перевіряємо перекриття bounds'ів
        boolean hasOverlap = newBounds.intersects(doorBounds);

        if (!hasOverlap) {
            return false;
        }

        Player.Direction playerDirection = player.getDirection();
        String doorDirection = this.direction;

        // Для дверей між кімнатами - гравець рухається ПРОТИЛЕЖНО до напрямку дверей
        if (isRoomLink) {
            if (playerDirection == Player.Direction.RIGHT && doorDirection.equals("left")) {
                return true; // Гравець іде праворуч до лівих дверей
            } else if (playerDirection == Player.Direction.LEFT && doorDirection.equals("right")) {
                return true; // Гравець іде ліворуч до правих дверей
            }
        }

        // Для дверей між поверхами - однакові напрямки
        if (isFloorLink) {
            if (playerDirection == Player.Direction.UP && doorDirection.equals("up")) {
                return true; // Гравець іде вгору до верхніх дверей
            } else if (playerDirection == Player.Direction.DOWN && doorDirection.equals("down")) {
                return true; // Гравець іде вниз до нижніх дверей
            }
        }

        return false;
    }

    @Override
    public double getInteractionRange() {
        return 0;
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
            gc.setImageSmoothing(false);
            Bounds bounds = getImageBounds();
            double renderX = bounds.getMinX();
            double renderY = bounds.getMinY();
            double renderWidth = imageWidth;
            double renderHeight = imageHeight;
            gc.drawImage(sprite, renderX, renderY, renderWidth, renderHeight);
        }
    }

    // --- Серіалізація ---

    // Повертає JSON для збереження (викликається з SaveManager.saveInteractables())
    @Override
    public JSONObject getSerializableData() {
        JSONObject data = new JSONObject();
        data.put("type", getType());
        data.put("x", imageX);
        data.put("y", imageY + imageHeight); // Зберігаємо як нижній лівий кут
        data.put("width", imageWidth);
        data.put("height", imageHeight);
        data.put("sharedId", sharedId);
        data.put("isOpen", isOpen);
        data.put("isLocked", isLocked);
        data.put("isLaser", isLaser);
        data.put("isRoomLink", isRoomLink);
        data.put("isFloorLink", isFloorLink);
        data.put("direction", direction);
        return data;
    }

    // Відновлює стан із JSON (викликається з SaveManager.loadGame())
    @Override
    public void setFromData(JSONObject data) {
        this.imageX = data.optDouble("x", imageX);
        this.imageY = data.optDouble("y", imageY + imageHeight) - imageHeight; // Конвертуємо назад у верхній лівий кут
        this.imageWidth = data.optDouble("width", imageWidth);
        this.imageHeight = data.optDouble("height", imageHeight);
        this.sharedId = data.optInt("sharedId", sharedId);
        this.isOpen = data.optBoolean("isOpen", isOpen);
        this.isLocked = data.optBoolean("isLocked", isLocked);
        this.isLaser = data.optBoolean("isLaser", isLaser);
        this.isRoomLink = data.optBoolean("isRoomLink", isRoomLink);
        this.isFloorLink = data.optBoolean("isFloorLink", isFloorLink);
        this.direction = data.optString("direction", direction);
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
        return new Vector2D(imageX, imageY);
    }

    // Повертає уявну позицію
    @Override
    public Vector2D getImagePosition() {
        return new Vector2D(imageX, imageY);
    }

    // Встановлює позицію дверей
    @Override
    public void setPosition(Vector2D position) {
        this.imageX = position.getX();
        this.imageY = position.getY();
    }

    // Встановлює позицію зображення
    @Override
    public void setImagePosition(Vector2D position) {
        this.imageX = position.getX();
        this.imageY = position.getY();
    }

    // Повертає межі для колізій
    @Override
    public Bounds getBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    // Повертає межі для рендерингу
    @Override
    public Bounds getImageBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    // Повертає підказку для UI
    @Override
    public String getInteractionPrompt() {
        return isLocked ? "Locked Door" : "Open Door";
    }

    // Повертає шар рендерингу
    @Override
    public int getRenderLayer() {
        return 0; // Двері рендеряться на шарі 0
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

    // Відкриває двері
    public void open(Player player) {
        if (!isLocked) {
            this.isOpen = true;
        }
    }

    // Повертає унікальний ID для зв’язку між дверима
    public int getSharedId() {
        return sharedId;
    }
}