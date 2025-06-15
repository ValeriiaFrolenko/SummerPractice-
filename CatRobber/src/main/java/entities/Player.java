package entities;

import interfaces.*;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.json.JSONObject;
import ui.ShopItem;
import utils.GameLoader;
import utils.Vector2D;

import java.util.HashMap;
import java.util.Map;

// Представляє гравця, який може рухатися, атакувати, взаємодіяти з об’єктами
public class Player implements Animatable, GameObject, Interactable {
    private boolean isAttacking;
    private String attackAnimationType;
    private double attackAnimationDuration;

    // Поля
    private double imageX;
    private double imageY;
    private double collX;
    private double collY;
    private double imageWidth;
    private double imageHeight;
    private double collWidth;
    private double collHeight;
    private Direction direction;
    private PlayerState state;
    private double speed;
    private boolean isVisible;
    private String currentAnimation;
    private int animationFrame;
    private double animationTime;
    private Map<String, Image[]> animations;
    private String[] spritePaths;
    private boolean canMove;
    private int detectionCount;
    private Map<ShopItem, Integer> inventory; // Інвентар: предмет -> кількість
    private Map<ShopItem, Boolean> itemUsage; // Стан використання: предмет -> чи використовується
    private JSONObject mapData; // Дані карти для синхронізації

    // Напрями та стани гравця
    public enum Direction { LEFT, RIGHT, UP, DOWN }
    public enum PlayerState { IDLE, RUN, HIT, CLIMB, INVISIBLE, SHOOT }

    // Конструктор: ініціалізує гравця з JSON-даними
    public Player(Vector2D position, JSONObject defaultData) {
        // Ініціалізація позиції та розмірів із JSON
        double jsonImageX = position.getX();
        double jsonImageY = position.getY();
        double jsonCollX = defaultData.optDouble("collX", jsonImageX);
        double jsonCollY = defaultData.optDouble("collY", jsonImageY - defaultData.optDouble("hightColl", 20.0));
        this.imageWidth = defaultData.getDouble("width");
        this.imageHeight = defaultData.getDouble("height");
        this.collWidth = defaultData.getDouble("widthColl");
        this.collHeight = defaultData.getDouble("hightColl");
        // Конвертація в верхній лівий кут
        this.imageX = jsonImageX;
        this.imageY = jsonImageY - imageHeight;
        this.collX = jsonCollX;
        this.collY = jsonCollY;
        this.detectionCount = defaultData.optInt("detectionCount", 0);
        this.inventory = new HashMap<>();
        this.itemUsage = new HashMap<>();
        this.mapData = defaultData; // Зберігаємо дані карти
        // Ініціалізація інвентарю з mapData
        initializeInventory();
        // Ініціалізація інших параметрів
        this.canMove = defaultData.optBoolean("canMove", true);
        String directionStr = defaultData.optString("direction", "RIGHT").toUpperCase();
        try {
            this.direction = Direction.valueOf(directionStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Невірне значення direction: " + directionStr + ". Встановлюю RIGHT.");
            this.direction = Direction.RIGHT;
        }
        this.state = PlayerState.IDLE;
        this.speed = 70.0;
        this.isVisible = true;
        this.currentAnimation = "idle";
        this.animationFrame = 0;
        this.animationTime = 0;
        this.isAttacking = false;
        this.attackAnimationDuration = 0.0;
        this.attackAnimationType = null;
        // Завантаження анімацій через GameLoader
        this.animations = new HashMap<>();
        this.spritePaths = new String[]{"player/idle.png", "player/run.png", "player/beating.png", "player/shoot.png"};
        GameLoader loader = new GameLoader();
        animations.put("idle", loader.splitSpriteSheet(spritePaths[0], 8));
        animations.put("run", loader.splitSpriteSheet(spritePaths[1], 10));
        animations.put("hit", loader.splitSpriteSheet(spritePaths[2], 13));
        animations.put("shoot", loader.splitSpriteSheet(spritePaths[3], 2));
    }

    // Ініціалізує інвентар із mapData
    private void initializeInventory() {
        // Перевіряємо наявність предметів у mapData
        for (String key : new String[]{"invisibility", "key", "speedBoost", "gun"}) {
            int quantity = mapData.optInt(key, 0);
            if (quantity > 0) {
                // Шукаємо існуючий ShopItem у inventory
                ShopItem item = findItemByMapKey(key);
                if (item != null) {
                    inventory.put(item, quantity);
                    itemUsage.put(item, false); // За замовчуванням не використовується
                }
            }
        }
    }

    // Знаходить ShopItem за ключем mapData
    private ShopItem findItemByMapKey(String mapKey) {
        for (ShopItem item : inventory.keySet()) {
            String itemMapKey = getMapKeyForItem(item);
            if (mapKey.equals(itemMapKey)) {
                return item;
            }
        }
        return null;
    }

    // --- Логіка покупок ---

    public boolean buyItem(ShopItem item) {
        inventory.put(item, inventory.getOrDefault(item, 0) + 1);
        itemUsage.putIfAbsent(item, false);
        updateMapData(item);
        System.out.println("Гравець отримав: " + item.getName());
        return true;
    }

    private void updateMapData(ShopItem item) {
        String mapKey = getMapKeyForItem(item);
        if (mapKey != null) {
            int currentQuantity = inventory.getOrDefault(item, 0);
            mapData.put(mapKey, currentQuantity);
        }
    }

    private String getMapKeyForItem(ShopItem item) {
        switch (item.getName()) {
            case "Невидимість": return "invisibility";
            case "Універсальний ключ": return "key";
            case "Буст швидкості": return "speedBoost";
            case "Пістолет": return "gun";
            default: return null;
        }
    }


    // Повертає інвентар
    public Map<ShopItem, Integer> getInventory() {
        return inventory;
    }

    // Встановлює стан використання предмета
    public void setItemUsage(ShopItem item, boolean isUsed) {
        if (inventory.containsKey(item)) {
            itemUsage.put(item, isUsed);
            System.out.println("Предмет " + item.getName() + " тепер " + (isUsed ? "використовується" : "не використовується"));
        } else {
            System.out.println("Предмет " + item.getName() + " не знайдено в інвентарі");
        }
    }

    // Перевіряє, чи використовується предмет
    public boolean isItemUsed(ShopItem item) {
        return itemUsage.getOrDefault(item, false);
    }

    // --- Ініціалізація та оновлення ---

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    // Рухає гравця в заданому напрямку
    public void move(Direction direction, double deltaTime) {
        if (canMove) {
            this.direction = direction;
            setAnimationState("run");
            double movement = speed * deltaTime;
            double deltaX = 0;
            if (direction == Direction.LEFT) {
                deltaX = -movement;
            } else if (direction == Direction.RIGHT) {
                deltaX = movement;
            }
            collX += deltaX;
            imageX += deltaX;
        }
    }

    // Зупиняє рух гравця
    public void stopMovement() {
        setCanMove(false);
        setAnimationState("idle");
    }

    // Дозволяє рух гравця
    public void allowMovement() {
        setCanMove(true);
    }

    // Оновлює анімацію гравця
    @Override
    public void updateAnimation(double deltaTime) {
        animationTime += deltaTime;
        Image[] frames = animations.getOrDefault(currentAnimation, animations.get("idle"));
        if (frames == null || frames.length == 0) return;

        if (isAttacking) {
            attackAnimationDuration -= deltaTime;
            double frameDuration = 0.2;
            int frameCount = frames.length;
            animationFrame = (int) (animationTime / frameDuration) % frameCount;
            if (attackAnimationDuration <= 0) {
                isAttacking = false;
                setAnimationState("idle");
            }
        } else {
            double frameDuration = 0.2;
            int frameCount = frames.length;
            animationFrame = (int) (animationTime / frameDuration) % frameCount;
        }
    }

    // Виконує атаку
    public void attack(boolean isRanged) {
        if (!isAttacking) {
            isAttacking = true;
            setAnimationState(isRanged ? "shoot" : "hit");
            Image[] frames = animations.get(currentAnimation);
            if (frames != null) {
                attackAnimationDuration = frames.length * 0.2;
            }
        }
    }

    // Піднімається по драбині
    public void climb(InteractiveObject ladder) {
        setAnimationState("climb");
    }

    // Збільшує рівень виявлення
    public void increaseDetection() {
        // Реалізація відсутня
    }

    public void teleportToRoom(Door door) {
        Direction currentDirection = this.getDirection();
        if (!door.isLaser()) {
            adjustPlayerPosition(140.0, currentDirection);
        }
        System.out.println("Teleported to room: x=" + getPosition().x + ", y=" + getPosition().y);
    }

    public void teleportToFloor(Door door, int levelId) {
        String doorDirection = door.direction;
        Direction teleportDirection;
        if (doorDirection.equals("up")) {
            teleportDirection = Direction.UP;
        } else if (doorDirection.equals("down")) {
            teleportDirection = Direction.DOWN;
        } else {
            return;
        }
        stopMovement();
        setAnimationState("idle");
        if (levelId==1){
        adjustPlayerPosition(120, teleportDirection);
        } else {
            adjustPlayerPosition(113, teleportDirection);
        }
        System.out.println("Teleported to floor: x=" + getPosition().x + ", y=" + getPosition().y);
    }

    public void adjustPlayerPosition(double offset, Direction direction) {
        Vector2D currentPosition = getPosition();
        Vector2D currentImaginePosition = getImagePosition();
        double adjustmentX = 0;
        double adjustmentY = 0;
        double backOffDistance = offset;
        if (direction == Direction.LEFT) {
            adjustmentX = -backOffDistance;
        } else if (direction == Direction.RIGHT) {
            adjustmentX = backOffDistance;
        } else if (direction == Direction.DOWN) {
            adjustmentY = backOffDistance;
        } else if (direction == Direction.UP) {
            adjustmentY = -backOffDistance;
        }
        Vector2D newPosition = new Vector2D(currentPosition.x + adjustmentX, currentPosition.y + adjustmentY);
        setPosition(newPosition);
        Vector2D newImaginePosition = new Vector2D(currentImaginePosition.x + adjustmentX, currentImaginePosition.y + adjustmentY);
        setImagePosition(newImaginePosition);
    }

    // --- Рендеринг ---

    @Override
    public void render(GraphicsContext gc) {
        Image frame = getCurrentFrame();
        if (frame != null && isVisible) {
            gc.setImageSmoothing(false);
            Bounds bounds = getImageBounds();
            double renderX = bounds.getMinX();
            double renderY = bounds.getMinY();
            double renderWidth = imageWidth;
            double renderHeight = imageHeight;
            if (direction == Direction.LEFT) {
                gc.save();
                gc.translate(renderX + renderWidth, renderY);
                gc.scale(-1, 1);
                gc.drawImage(frame, 0, 0, renderWidth, renderHeight);
                gc.restore();
            } else {
                gc.drawImage(frame, renderX, renderY, renderWidth, renderHeight);
            }
            Bounds collBounds = getBounds();
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.strokeRect(collBounds.getMinX(), collBounds.getMinY(), collBounds.getWidth(), collBounds.getHeight());
        }
    }

    @Override
    public Image getCurrentFrame() {
        Image[] frames = animations.getOrDefault(currentAnimation, animations.get("idle"));
        if (frames == null || frames.length == 0) {
            System.err.println("Немає кадрів для анімації: " + currentAnimation);
            return null;
        }
        return frames[animationFrame];
    }

    // --- Взаємодія ---

    @Override
    public void setAnimationState(String state) {
        if (!isAttacking || state.equals("hit") || state.equals("shoot")) {
            if (animations.containsKey(state) && !state.equals(currentAnimation)) {
                currentAnimation = state;
                animationFrame = 0;
                animationTime = 0;
            }
        }
    }

    @Override
    public void interact(Player player) {
        // Порожня реалізація
    }

    @Override
    public boolean canInteract(Player player) {
        return false;
    }

    // --- Серіалізація ---

    @Override
    public JSONObject getSerializableData() {
        JSONObject data = new JSONObject();
        data.put("x", imageX);
        data.put("y", imageY + imageHeight);
        data.put("collX", collX);
        data.put("collY", collY);
        data.put("width", imageWidth);
        data.put("height", imageHeight);
        data.put("widthColl", collWidth);
        data.put("hightColl", collHeight);
        data.put("direction", direction.toString());
        data.put("state", state.toString());
        data.put("currentAnimation", currentAnimation);
        data.put("canMove", canMove);
        data.put("detectionCount", detectionCount);
        // Синхронізація інвентарю з mapData
        for (Map.Entry<ShopItem, Integer> entry : inventory.entrySet()) {
            String mapKey = getMapKeyForItem(entry.getKey());
            if (mapKey != null) {
                data.put(mapKey, entry.getValue());
            }
        }
        // Серіалізація стану використання
        JSONObject usageData = new JSONObject();
        for (Map.Entry<ShopItem, Boolean> entry : itemUsage.entrySet()) {
            usageData.put(entry.getKey().getName(), entry.getValue());
        }
        data.put("itemUsage", usageData);
        data.put("type", "Player");
        data.put("type", "Player");
        return data;
    }

    @Override
    public void setFromData(JSONObject data) {
        double jsonImageX = data.optDouble("x", imageX);
        double jsonImageY = data.optDouble("y", imageY + imageHeight);
        double jsonCollX = data.optDouble("collX", collX);
        double jsonCollY = data.optDouble("collY", collY);
        this.imageX = jsonImageX;
        this.imageY = jsonImageY - imageHeight;
        this.collX = jsonCollX;
        this.collY = jsonCollY;
        this.imageWidth = data.optDouble("width", imageWidth);
        this.imageHeight = data.optDouble("height", imageHeight);
        this.collWidth = data.optDouble("widthColl", collWidth);
        this.collHeight = data.optDouble("hightColl", collHeight);
        this.canMove = data.optBoolean("canMove", true);
        this.detectionCount = data.optInt("detectionCount", 0);
        this.mapData = data;
        // Оновлення інвентарю
        initializeInventory();
        // Відновлення itemUsage
        JSONObject usageData = data.optJSONObject("itemUsage");
        if (usageData != null) {
            for (ShopItem item : inventory.keySet()) {
                String name = item.getName();
                if (usageData.has(name)) {
                    itemUsage.put(item, usageData.getBoolean(name));
                }
            }
        }
        String directionStr = data.optString("direction", direction.toString()).toUpperCase();
        try {
            this.direction = Direction.valueOf(directionStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Невірне значення direction: " + directionStr + ". Залишаю поточний.");
        }
    }

    // --- Гетери/Сетери ---

    @Override
    public String getType() {
        return "Player";
    }

    @Override
    public Vector2D getPosition() {
        return new Vector2D(collX, collY);
    }

    @Override
    public Vector2D getImagePosition() {
        return new Vector2D(imageX, imageY);
    }

    @Override
    public void setPosition(Vector2D position) {
        this.collX = position.getX();
        this.collY = position.getY();
    }

    @Override
    public void setImagePosition(Vector2D position) {
        this.imageX = position.getX();
        this.imageY = position.getY();
    }

    @Override
    public Bounds getBounds() {
        return new BoundingBox(collX, collY, collWidth, collHeight);
    }

    @Override
    public Bounds getImageBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    @Override
    public double getInteractionRange() {
        return 0.0;
    }

    @Override
    public String getInteractionPrompt() {
        return null;
    }

    @Override
    public int getRenderLayer() {
        return 2;
    }

    @Override
    public boolean isVisible() {
        return state != PlayerState.INVISIBLE;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isCanMove() {
        return canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    public boolean isAttacking() {
        return isAttacking;
    }
}